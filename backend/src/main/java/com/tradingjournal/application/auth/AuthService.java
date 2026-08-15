package com.tradingjournal.application.auth;

import com.tradingjournal.domain.entity.RefreshToken;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.RefreshTokenRepository;
import com.tradingjournal.infrastructure.repository.UserRepository;
import com.tradingjournal.infrastructure.security.JwtProvider;
import com.tradingjournal.presentation.auth.AuthResponse;
import com.tradingjournal.presentation.auth.ChangePasswordRequest;
import com.tradingjournal.presentation.auth.LoginRequest;
import com.tradingjournal.presentation.auth.RegisterRequest;
import com.tradingjournal.presentation.auth.UpdateProfileRequest;
import com.tradingjournal.presentation.auth.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.beans.factory.annotation.Value;
import com.tradingjournal.presentation.auth.WebhookResponseDTO;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private static final long REFRESH_TOKEN_DAYS = 30;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getFullName(), request.getEmail(), encodedPassword);
        User savedUser = userRepository.save(user);

        RefreshToken refreshToken = createRefreshToken(savedUser);
        return buildAuthResponse(savedUser, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        RefreshToken refreshToken = createRefreshToken(user);
        return buildAuthResponse(user, refreshToken);
    }

    @Transactional
    public AuthResponse refresh(String tokenValue) {
        RefreshToken existingToken = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (existingToken.isRevoked() || existingToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        existingToken.setRevoked(true);
        refreshTokenRepository.save(existingToken);

        User user = existingToken.getUser();
        RefreshToken newRefreshToken = createRefreshToken(user);
        return buildAuthResponse(user, newRefreshToken);
    }

    @Transactional
    public void logout(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public UserResponse updateProfile(User user, UpdateProfileRequest request) {
        userRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
                });

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser.getId(), savedUser.getFullName(), savedUser.getEmail());
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public WebhookResponseDTO getOrCreateWebhookUrl(User user) {
        String token = user.getWebhookToken();
        if (token == null || token.isBlank()) {
            token = UUID.randomUUID().toString();
            user.setWebhookToken(token);
            userRepository.save(user);
        }
        String fullUrl = buildWebhookUrl(token);
        return new WebhookResponseDTO(fullUrl);
    }

    @Transactional
    public WebhookResponseDTO regenerateWebhookUrl(User user) {
        String newToken = UUID.randomUUID().toString();
        user.setWebhookToken(newToken);
        userRepository.save(user);
        String fullUrl = buildWebhookUrl(newToken);
        return new WebhookResponseDTO(fullUrl);
    }

    private String buildWebhookUrl(String token) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + "/api/v1/webhooks/tradingview/" + token;
    }

    private RefreshToken createRefreshToken(User user) {
        String token = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(REFRESH_TOKEN_DAYS, ChronoUnit.DAYS);
        RefreshToken refreshToken = new RefreshToken(token, user, expiresAt);
        return refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse buildAuthResponse(User user, RefreshToken refreshToken) {
        String accessToken = jwtProvider.generateToken(user.getId(), user.getEmail());
        UserResponse userResponse = new UserResponse(user.getId(), user.getFullName(), user.getEmail());
        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                jwtProvider.getExpirationMs() / 1000,
                userResponse);
    }
}

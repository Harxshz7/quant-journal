package com.tradingjournal.presentation.auth;

import com.tradingjournal.application.auth.AuthService;
import com.tradingjournal.domain.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse response = new UserResponse(user.getId(), user.getFullName(), user.getEmail());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse response = authService.updateProfile(user, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        authService.changePassword(user, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/settings")
    public ResponseEntity<UserSettingsDTO> getSettings(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.getSettings(user));
    }

    @PutMapping("/me/settings")
    public ResponseEntity<UserSettingsDTO> updateSettings(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateUserSettingsRequest request) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.updateSettings(user, request));
    }

    @GetMapping("/me/webhook")
    public ResponseEntity<WebhookResponseDTO> getWebhookUrl(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        WebhookResponseDTO response = authService.getOrCreateWebhookUrl(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/me/webhook/regenerate")
    public ResponseEntity<WebhookResponseDTO> regenerateWebhookUrl(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        WebhookResponseDTO response = authService.regenerateWebhookUrl(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/share")
    public ResponseEntity<ShareStatusDTO> getShareStatus(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.getShareStatus(user));
    }

    @PostMapping("/me/share/enable")
    public ResponseEntity<ShareStatusDTO> enableShare(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.enableShare(user));
    }

    @PostMapping("/me/share/disable")
    public ResponseEntity<ShareStatusDTO> disableShare(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.disableShare(user));
    }
}

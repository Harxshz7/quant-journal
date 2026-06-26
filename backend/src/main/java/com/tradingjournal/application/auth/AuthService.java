package com.tradingjournal.application.auth;

import com.tradingjournal.presentation.auth.AuthRequest;
import com.tradingjournal.presentation.auth.AuthResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public AuthResponse login(AuthRequest request) {
        return AuthResponse.builder()
                .accessToken("dummy-token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .build();
    }
}

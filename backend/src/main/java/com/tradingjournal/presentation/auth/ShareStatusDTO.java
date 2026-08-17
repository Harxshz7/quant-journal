package com.tradingjournal.presentation.auth;

public record ShareStatusDTO(
        String shareToken,
        boolean shareEnabled
) {
}
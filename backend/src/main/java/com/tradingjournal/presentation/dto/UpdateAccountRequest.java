package com.tradingjournal.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAccountRequest(
        @NotBlank(message = "Account name is required")
        @Size(max = 100, message = "Account name must be at most 100 characters")
        String name
) {
}

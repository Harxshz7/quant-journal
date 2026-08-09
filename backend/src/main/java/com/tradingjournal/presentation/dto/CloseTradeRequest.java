package com.tradingjournal.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CloseTradeRequest(
    @NotNull(message = "Exit price is required")
    @Positive(message = "Exit price must be positive")
    BigDecimal exitPrice
) {}

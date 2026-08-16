package com.tradingjournal.presentation.auth;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateUserSettingsRequest(
        @DecimalMin(value = "0.0", message = "Account size must be zero or positive")
        BigDecimal accountSize,

        @DecimalMin(value = "0.0", message = "Daily loss limit must be zero or positive")
        BigDecimal dailyLossLimitAmount,

        BigDecimal monthlyGoalPnl
) {
}

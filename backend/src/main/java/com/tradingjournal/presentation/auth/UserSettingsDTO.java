package com.tradingjournal.presentation.auth;

import java.math.BigDecimal;

public record UserSettingsDTO(
        BigDecimal accountSize,
        BigDecimal dailyLossLimitAmount,
        BigDecimal monthlyGoalPnl
) {
}

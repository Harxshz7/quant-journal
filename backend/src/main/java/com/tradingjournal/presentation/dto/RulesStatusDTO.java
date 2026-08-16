package com.tradingjournal.presentation.dto;

import java.math.BigDecimal;

public record RulesStatusDTO(
        BigDecimal dailyPnl,
        BigDecimal dailyLossLimitAmount,
        boolean dailyLimitHit,
        BigDecimal monthlyPnl,
        BigDecimal monthlyGoalPnl,
        BigDecimal monthlyGoalProgressPercent
) {
}

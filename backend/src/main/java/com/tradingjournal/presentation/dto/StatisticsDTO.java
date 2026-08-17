package com.tradingjournal.presentation.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StatisticsDTO(
        UUID id,
        int totalTrades,
        int winCount,
        int lossCount,
        int breakEvenCount,
        BigDecimal winRate,
        BigDecimal profitFactor,
        BigDecimal avgWin,
        BigDecimal avgLoss,
        BigDecimal largestWin,
        BigDecimal largestLoss,
        int maxConsecutiveWins,
        int maxConsecutiveLosses,
        BigDecimal avgRiskReward,
        BigDecimal expectancy,
        BigDecimal riskOfRuin,
        Instant updatedAt
) {
}

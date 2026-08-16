package com.tradingjournal.presentation.dto;

import java.math.BigDecimal;

public record BreakdownEntryDTO(
        String group,
        int tradeCount,
        BigDecimal winRate,
        BigDecimal netPnl,
        BigDecimal profitFactor
) {}

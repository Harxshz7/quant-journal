package com.tradingjournal.presentation.dto;

import java.math.BigDecimal;

public record TimeBreakdownDTO(
        String period,
        int tradeCount,
        BigDecimal netPnl,
        BigDecimal winRate
) {}

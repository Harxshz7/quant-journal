package com.tradingjournal.presentation.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Deliberately narrow, read-only performance snapshot exposed to the public
 * share link. Never includes trade notes, tickers, account size, or balances.
 */
public record PublicShareDTO(
        BigDecimal winRate,
        BigDecimal profitFactor,
        List<EquityPointDTO> equityCurve,
        List<TimeBreakdownDTO> monthly
) {
}
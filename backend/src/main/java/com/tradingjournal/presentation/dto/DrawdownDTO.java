package com.tradingjournal.presentation.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DrawdownDTO(
        BigDecimal maxDrawdown,
        BigDecimal maxDrawdownPercent,
        Instant peakDate,
        Instant troughDate
) {}

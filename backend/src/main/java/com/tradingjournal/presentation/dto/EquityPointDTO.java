package com.tradingjournal.presentation.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record EquityPointDTO(Instant date, BigDecimal cumulativePnl) {}

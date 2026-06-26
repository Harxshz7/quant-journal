package com.tradingjournal.presentation.trade;

import com.tradingjournal.domain.entity.PositionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTradeRequest(
        @NotBlank(message = "Ticker is required") String ticker,
        @NotNull(message = "Position type is required") PositionType positionType,
        @NotNull(message = "Entry price is required") @Positive(message = "Entry price must be positive") BigDecimal entryPrice,
        @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be positive") BigDecimal quantity) {
}

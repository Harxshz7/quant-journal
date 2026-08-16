package com.tradingjournal.presentation.dto;

import com.tradingjournal.domain.entity.PositionType;
import com.tradingjournal.domain.entity.MistakeTag;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

public record UpdateTradeRequest(
    @NotBlank(message = "Ticker is required")
    String ticker,

    @NotNull(message = "Position type is required")
    PositionType positionType,

    @NotNull(message = "Entry price is required")
    @Positive(message = "Entry price must be positive")
    BigDecimal entryPrice,

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    BigDecimal quantity,

    @Positive(message = "Stop loss must be positive")
    BigDecimal stopLoss,

    String strategy,

    String postTradeReflection,

    Set<MistakeTag> mistakeTags,

    @Min(1) @Max(5)
    Integer setupQuality
) {}

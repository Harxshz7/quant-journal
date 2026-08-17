package com.tradingjournal.presentation.dto;

import com.tradingjournal.domain.entity.PositionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateTradeRequest(
    @NotNull(message = "Journal entry ID is required")
    UUID journalEntryId,

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

    List<UUID> checklistItemIds
) {}

package com.tradingjournal.presentation.dto;

import com.tradingjournal.domain.entity.PositionType;
import com.tradingjournal.domain.entity.MistakeTag;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
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

    List<UUID> checklistItemIds
) {}

package com.tradingjournal.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateJournalEntryRequest(
    @NotNull(message = "Notes cannot be null")
    String notes
) {}

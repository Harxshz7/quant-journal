package com.tradingjournal.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateJournalEntryRequest(
    @NotNull(message = "Entry date is required")
    LocalDate entryDate,

    String notes
) {}

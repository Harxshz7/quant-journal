package com.tradingjournal.presentation.dto;

import com.tradingjournal.domain.entity.Mood;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateJournalEntryRequest(
    @NotNull(message = "Entry date is required")
    LocalDate entryDate,

    String notes,

    Mood mood,

    @Min(1) @Max(5)
    Integer energy,

    String marketBias,

    String dailyGoal,

    @Min(1) @Max(5)
    Integer dayRating
) {}

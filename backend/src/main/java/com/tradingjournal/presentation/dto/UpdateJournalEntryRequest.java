package com.tradingjournal.presentation.dto;

import com.tradingjournal.domain.entity.Mood;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateJournalEntryRequest(
    String notes,

    Mood mood,

    @Min(1) @Max(5)
    Integer energy,

    String marketBias,

    String dailyGoal,

    @Min(1) @Max(5)
    Integer dayRating
) {}

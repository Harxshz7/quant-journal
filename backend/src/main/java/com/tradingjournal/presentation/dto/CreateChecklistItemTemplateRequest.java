package com.tradingjournal.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Set;
import java.util.UUID;

public record CreateChecklistItemTemplateRequest(
    @NotBlank(message = "Text is required")
    String text,

    Integer sortOrder
) {}

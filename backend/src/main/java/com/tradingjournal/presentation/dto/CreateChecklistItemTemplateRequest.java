package com.tradingjournal.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateChecklistItemTemplateRequest(
    @NotBlank(message = "Text is required")
    String text,

    Integer sortOrder
) {}

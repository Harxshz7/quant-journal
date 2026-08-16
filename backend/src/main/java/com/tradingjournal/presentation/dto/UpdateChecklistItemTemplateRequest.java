package com.tradingjournal.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateChecklistItemTemplateRequest(
    @NotBlank(message = "Text is required")
    String text,

    Integer sortOrder,

    Boolean active
) {}

package com.tradingjournal.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import java.util.UUID;

public record CreateLessonRequest(
    @NotBlank(message = "Content is required")
    String content,

    Set<String> tags,

    UUID sourceTradeId
) {}

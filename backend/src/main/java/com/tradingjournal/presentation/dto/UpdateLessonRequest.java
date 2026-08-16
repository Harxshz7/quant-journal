package com.tradingjournal.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record UpdateLessonRequest(
    @NotBlank(message = "Content is required")
    String content,

    Set<String> tags
) {}

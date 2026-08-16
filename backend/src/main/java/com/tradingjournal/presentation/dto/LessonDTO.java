package com.tradingjournal.presentation.dto;

import java.util.Set;
import java.util.UUID;

public record LessonDTO(
    UUID id,
    String content,
    Set<String> tags,
    UUID sourceTradeId,
    String sourceTradeTicker,
    String createdAt
) {}

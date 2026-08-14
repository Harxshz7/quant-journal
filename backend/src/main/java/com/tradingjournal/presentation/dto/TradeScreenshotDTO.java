package com.tradingjournal.presentation.dto;

import com.tradingjournal.domain.entity.TradeScreenshot;

import java.time.Instant;
import java.util.UUID;

public record TradeScreenshotDTO(
        UUID id,
        String originalFileName,
        String contentType,
        long fileSizeBytes,
        Instant uploadedAt) {
    public static TradeScreenshotDTO fromEntity(TradeScreenshot screenshot) {
        return new TradeScreenshotDTO(
                screenshot.getId(),
                screenshot.getOriginalFileName(),
                screenshot.getContentType(),
                screenshot.getFileSizeBytes(),
                screenshot.getUploadedAt());
    }
}

package com.tradingjournal.presentation.journal;

import com.tradingjournal.application.journal.TradeScreenshotService;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.presentation.dto.TradeScreenshotDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
public class TradeScreenshotController {

    private final TradeScreenshotService tradeScreenshotService;

    public TradeScreenshotController(TradeScreenshotService tradeScreenshotService) {
        this.tradeScreenshotService = tradeScreenshotService;
    }

    /**
     * Upload a screenshot for a trade.
     * POST /api/v1/trades/{tradeId}/screenshots
     */
    @PostMapping(path = "/api/v1/trades/{tradeId}/screenshots", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TradeScreenshotDTO> uploadScreenshot(
            @AuthenticationPrincipal User user,
            @PathVariable UUID tradeId,
            @RequestParam("file") MultipartFile file) {
        TradeScreenshotDTO screenshot = tradeScreenshotService.uploadScreenshot(user, tradeId, file);
        return ResponseEntity.status(201).body(screenshot);
    }

    /**
     * Get all screenshots for a trade.
     * GET /api/v1/trades/{tradeId}/screenshots
     */
    @GetMapping("/api/v1/trades/{tradeId}/screenshots")
    public ResponseEntity<List<TradeScreenshotDTO>> getScreenshotsForTrade(
            @AuthenticationPrincipal User user,
            @PathVariable UUID tradeId) {
        List<TradeScreenshotDTO> screenshots = tradeScreenshotService.getScreenshotsForTrade(user, tradeId);
        return ResponseEntity.ok(screenshots);
    }

    /**
     * Get screenshot file (raw image bytes).
     * GET /api/v1/screenshots/{id}/file
     */
    @GetMapping("/api/v1/screenshots/{id}/file")
    public ResponseEntity<byte[]> getScreenshotFile(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        TradeScreenshotService.ScreenshotFile file = tradeScreenshotService.getScreenshotFile(user, id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.originalFileName + "\"")
                .body(file.fileBytes);
    }

    /**
     * Delete a screenshot.
     * DELETE /api/v1/screenshots/{id}
     */
    @DeleteMapping("/api/v1/screenshots/{id}")
    public ResponseEntity<Void> deleteScreenshot(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id) {
        tradeScreenshotService.deleteScreenshot(user, id);
        return ResponseEntity.noContent().build();
    }
}

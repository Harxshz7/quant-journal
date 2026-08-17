package com.tradingjournal.application.journal;

import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.TradeScreenshot;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import com.tradingjournal.infrastructure.repository.TradeScreenshotRepository;
import com.tradingjournal.presentation.dto.TradeScreenshotDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class TradeScreenshotService {

    private final TradeScreenshotRepository tradeScreenshotRepository;
    private final TradeRepository tradeRepository;

    @Value("${app.upload.dir:./uploads/screenshots}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_CONTENT_TYPES = { "image/jpeg", "image/png", "image/webp" };

    public TradeScreenshotService(
            TradeScreenshotRepository tradeScreenshotRepository,
            TradeRepository tradeRepository) {
        this.tradeScreenshotRepository = tradeScreenshotRepository;
        this.tradeRepository = tradeRepository;
    }

    /**
     * Upload a screenshot for a trade.
     */
    public TradeScreenshotDTO uploadScreenshot(User user, UUID tradeId, MultipartFile file) {
        // Verify trade exists and belongs to user
        Trade trade = findOwnedTradeOrThrow(user, tradeId);

        // Validate file size (server-side check, don't trust multipart config alone)
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds 10MB limit");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (!isAllowedContentType(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must be an image (JPEG, PNG, or WebP)");
        }

        // Ensure upload directory exists
        Path uploadPath = Paths.get(uploadDir, tradeId.toString());
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create upload directory");
        }

        // Generate unique stored filename using UUID to avoid collisions and path
        // traversal
        String storedFileName = UUID.randomUUID().toString();
        Path filePath = uploadPath.resolve(storedFileName);

        // Write file to disk
        try {
            Files.write(filePath, file.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file");
        }

        // Save screenshot record to database
        TradeScreenshot screenshot = new TradeScreenshot(
                trade,
                file.getOriginalFilename(),
                storedFileName,
                contentType,
                file.getSize());

        TradeScreenshot saved = tradeScreenshotRepository.save(screenshot);
        return TradeScreenshotDTO.fromEntity(saved);
    }

    /**
     * Get screenshot file as raw bytes.
     */
    @Transactional(readOnly = true)
    public ScreenshotFile getScreenshotFile(User user, UUID screenshotId) {
        TradeScreenshot screenshot = tradeScreenshotRepository.findById(screenshotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screenshot not found"));

        // Verify ownership via trade.journalEntry.user
        if (screenshot.getTrade() == null
                || screenshot.getTrade().getJournalEntry() == null
                || !Objects.requireNonNull(screenshot.getTrade().getJournalEntry().getUser(), "Journal entry must have an owner").getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Screenshot not found");
        }

        // Build file path
        Path filePath = Paths.get(uploadDir, Objects.requireNonNull(screenshot.getTrade()).getId().toString(), screenshot.getStoredFileName());

        // Read file from disk
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Screenshot file not found");
        }

        return new ScreenshotFile(fileBytes, screenshot.getContentType(), screenshot.getOriginalFileName());
    }

    /**
     * Delete a screenshot.
     */
    public void deleteScreenshot(User user, UUID screenshotId) {
        TradeScreenshot screenshot = tradeScreenshotRepository.findById(screenshotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Screenshot not found"));

        // Verify ownership
        if (screenshot.getTrade() == null
                || screenshot.getTrade().getJournalEntry() == null
                || !Objects.requireNonNull(screenshot.getTrade().getJournalEntry().getUser(), "Journal entry must have an owner").getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Screenshot not found");
        }

        // Delete file from disk
        Path filePath = Paths.get(uploadDir, Objects.requireNonNull(screenshot.getTrade()).getId().toString(), screenshot.getStoredFileName());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log but don't fail if file deletion fails - DB record will still be deleted
            System.err.println("Failed to delete screenshot file: " + e.getMessage());
        }

        // Delete database record
        tradeScreenshotRepository.delete(screenshot);
    }

    /**
     * Get all screenshots for a trade.
     */
    @Transactional(readOnly = true)
    public List<TradeScreenshotDTO> getScreenshotsForTrade(User user, UUID tradeId) {
        // Verify trade ownership
        findOwnedTradeOrThrow(user, tradeId);

        List<TradeScreenshot> screenshots = tradeScreenshotRepository.findByTrade_TradeId(tradeId);
        return screenshots.stream()
                .map(TradeScreenshotDTO::fromEntity)
                .toList();
    }

    private Trade findOwnedTradeOrThrow(User user, UUID tradeId) {
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found"));

        if (trade.getJournalEntry() == null
                || trade.getJournalEntry().getUser() == null
                || !trade.getJournalEntry().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found");
        }

        return trade;
    }

    private boolean isAllowedContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        for (String allowed : ALLOWED_CONTENT_TYPES) {
            if (contentType.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * DTO for returning screenshot file with metadata.
     */
    public static class ScreenshotFile {
        public final byte[] fileBytes;
        public final String contentType;
        public final String originalFileName;

        public ScreenshotFile(byte[] fileBytes, String contentType, String originalFileName) {
            this.fileBytes = fileBytes;
            this.contentType = contentType;
            this.originalFileName = originalFileName;
        }
    }
}

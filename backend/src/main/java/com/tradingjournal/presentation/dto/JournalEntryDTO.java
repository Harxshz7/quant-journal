package com.tradingjournal.presentation.dto;

import com.tradingjournal.domain.entity.JournalEntry;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record JournalEntryDTO(
    UUID journalEntryId,
    LocalDate entryDate,
    String notes,
    List<TradeDTO> trades,
    Instant createdAt,
    Instant updatedAt
) {
    public static JournalEntryDTO fromEntity(JournalEntry entry) {
        return fromEntity(entry, Collections.emptyMap());
    }

    /**
     * Maps an entry to a DTO, attaching screenshots for each trade.
     * Callers must pass a pre-fetched map of tradeId -> screenshots (batched query)
     * to avoid an N+1 repository call per trade.
     */
    public static JournalEntryDTO fromEntity(JournalEntry entry, Map<UUID, List<TradeScreenshotDTO>> screenshotsByTradeId) {
        Map<UUID, List<TradeScreenshotDTO>> screenshots = screenshotsByTradeId != null ? screenshotsByTradeId : Collections.emptyMap();

        List<TradeDTO> tradeDTOs = entry.getTrades() != null ?
            entry.getTrades().stream().map(trade -> {
                List<TradeScreenshotDTO> tradeScreenshots = screenshots.getOrDefault(trade.getId(), Collections.emptyList());
                return TradeDTO.fromEntity(trade, tradeScreenshots);
            }).toList() : Collections.emptyList();

        return new JournalEntryDTO(
            entry.getId(),
            entry.getEntryDate(),
            entry.getNotes(),
            tradeDTOs,
            entry.getCreatedAt(),
            entry.getUpdatedAt()
        );
    }
}

package com.tradingjournal.presentation.dto;

import com.tradingjournal.domain.entity.JournalEntry;
import com.tradingjournal.domain.entity.Mood;

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
    Instant updatedAt,
    Mood mood,
    Integer energy,
    String marketBias,
    String dailyGoal,
    Integer dayRating
) {
    public static JournalEntryDTO fromEntity(JournalEntry entry) {
        return fromEntity(entry, Collections.emptyMap(), Collections.emptyMap());
    }

    public static JournalEntryDTO fromEntity(JournalEntry entry, Map<UUID, List<TradeScreenshotDTO>> screenshotsByTradeId) {
        return fromEntity(entry, screenshotsByTradeId, Collections.emptyMap());
    }

    public static JournalEntryDTO fromEntity(
            JournalEntry entry,
            Map<UUID, List<TradeScreenshotDTO>> screenshotsByTradeId,
            Map<UUID, List<TradeChecklistItemDTO>> checklistByTradeId) {
        Map<UUID, List<TradeScreenshotDTO>> screenshots = screenshotsByTradeId != null ? screenshotsByTradeId : Collections.emptyMap();
        Map<UUID, List<TradeChecklistItemDTO>> checklists = checklistByTradeId != null ? checklistByTradeId : Collections.emptyMap();

        List<TradeDTO> tradeDTOs = entry.getTrades() != null ?
            entry.getTrades().stream().map(trade -> {
                List<TradeScreenshotDTO> tradeScreenshots = screenshots.getOrDefault(trade.getId(), Collections.emptyList());
                List<TradeChecklistItemDTO> tradeChecklist = checklists.getOrDefault(trade.getId(), Collections.emptyList());
                return TradeDTO.fromEntity(trade, tradeScreenshots, tradeChecklist);
            }).toList() : Collections.emptyList();

        return new JournalEntryDTO(
            entry.getId(),
            entry.getEntryDate(),
            entry.getNotes(),
            tradeDTOs,
            entry.getCreatedAt(),
            entry.getUpdatedAt(),
            entry.getMood(),
            entry.getEnergy(),
            entry.getMarketBias(),
            entry.getDailyGoal(),
            entry.getDayRating()
        );
    }
}

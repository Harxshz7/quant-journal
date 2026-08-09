package com.tradingjournal.presentation.dto;

import com.tradingjournal.domain.entity.JournalEntry;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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
        List<TradeDTO> tradeDTOs = entry.getTrades() != null ?
            entry.getTrades().stream().map(TradeDTO::fromEntity).toList() : Collections.emptyList();
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

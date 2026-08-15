package com.tradingjournal.application.journal;

import com.tradingjournal.domain.entity.JournalEntry;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.JournalEntryRepository;
import com.tradingjournal.presentation.dto.CreateJournalEntryRequest;
import com.tradingjournal.presentation.dto.JournalEntryDTO;
import com.tradingjournal.presentation.dto.TradeScreenshotDTO;
import com.tradingjournal.presentation.dto.UpdateJournalEntryRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final com.tradingjournal.infrastructure.repository.TradeScreenshotRepository tradeScreenshotRepository;

    public JournalEntryService(
            JournalEntryRepository journalEntryRepository,
            com.tradingjournal.infrastructure.repository.TradeScreenshotRepository tradeScreenshotRepository
    ) {
        this.journalEntryRepository = journalEntryRepository;
        this.tradeScreenshotRepository = tradeScreenshotRepository;
    }

    public JournalEntryDTO createJournalEntry(User user, CreateJournalEntryRequest request) {
        Optional<JournalEntry> existing = journalEntryRepository.findByUserAndEntryDate(user, request.entryDate());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Journal entry already exists for date: " + request.entryDate());
        }

        JournalEntry entry = new JournalEntry(user, request.entryDate(), request.notes());
        JournalEntry saved = journalEntryRepository.save(entry);
        return JournalEntryDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<JournalEntryDTO> getUserJournalEntries(User user) {
        return journalEntryRepository.findByUserWithTrades(user)
                .stream()
                .map(JournalEntryDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public JournalEntryDTO getJournalEntryById(User user, UUID id) {
        JournalEntry entry = journalEntryRepository.findWithTradesById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found");
        }

        // Batch-load screenshots for all of the entry's trades in a single query
        List<UUID> tradeIds = entry.getTrades().stream()
                .map(trade -> trade.getId())
                .toList();

        Map<UUID, List<TradeScreenshotDTO>> screenshotsByTradeId = tradeIds.isEmpty()
                ? Map.of()
                : tradeScreenshotRepository.findByTradeIdIn(tradeIds).stream()
                        .collect(Collectors.groupingBy(
                                screenshot -> screenshot.getTrade().getId(),
                                Collectors.mapping(TradeScreenshotDTO::fromEntity, Collectors.toList())
                        ));

        return JournalEntryDTO.fromEntity(entry, screenshotsByTradeId);
    }

    public JournalEntryDTO updateJournalEntry(User user, UUID id, UpdateJournalEntryRequest request) {
        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found");
        }

        entry.setNotes(request.notes());
        JournalEntry updated = journalEntryRepository.save(entry);
        return JournalEntryDTO.fromEntity(updated);
    }
}

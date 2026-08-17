package com.tradingjournal.application.journal;

import com.tradingjournal.application.account.AccountService;
import com.tradingjournal.domain.entity.Account;
import com.tradingjournal.domain.entity.JournalEntry;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.JournalEntryRepository;
import com.tradingjournal.infrastructure.repository.TradeChecklistItemRepository;
import com.tradingjournal.infrastructure.repository.TradeScreenshotRepository;
import com.tradingjournal.presentation.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final TradeScreenshotRepository tradeScreenshotRepository;
    private final TradeChecklistItemRepository tradeChecklistItemRepository;
    private final AccountService accountService;

    public JournalEntryService(
            JournalEntryRepository journalEntryRepository,
            TradeScreenshotRepository tradeScreenshotRepository,
            TradeChecklistItemRepository tradeChecklistItemRepository,
            AccountService accountService
    ) {
        this.journalEntryRepository = journalEntryRepository;
        this.tradeScreenshotRepository = tradeScreenshotRepository;
        this.tradeChecklistItemRepository = tradeChecklistItemRepository;
        this.accountService = accountService;
    }

    public JournalEntryDTO createJournalEntry(User user, UUID accountId, CreateJournalEntryRequest request) {
        Account account = accountId != null
                ? accountService.resolveOwnedAccount(user, accountId)
                : accountService.getDefaultAccount(user);

        Optional<JournalEntry> existing = account != null
                ? journalEntryRepository.findByUserAndAccountAndEntryDate(user, account, request.entryDate())
                : journalEntryRepository.findByUserAndEntryDate(user, request.entryDate());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Journal entry already exists for date: " + request.entryDate());
        }

        JournalEntry entry = new JournalEntry(user, request.entryDate(), request.notes());
        entry.setAccount(account);
        entry.setMood(request.mood());
        entry.setEnergy(request.energy());
        entry.setMarketBias(request.marketBias());
        entry.setDailyGoal(request.dailyGoal());
        entry.setDayRating(request.dayRating());
        JournalEntry saved = journalEntryRepository.save(entry);
        return JournalEntryDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<JournalEntryDTO> getUserJournalEntries(User user, UUID accountId) {
        Account account = accountId != null
                ? accountService.resolveOwnedAccount(user, accountId)
                : null;
        return journalEntryRepository.findByUserWithTrades(user, account)
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

        Map<UUID, List<TradeChecklistItemDTO>> checklistByTradeId = tradeIds.isEmpty()
                ? Map.of()
                : tradeChecklistItemRepository.findByTradeIdIn(tradeIds).stream()
                        .collect(Collectors.groupingBy(
                                item -> item.getTrade().getId(),
                                Collectors.mapping(TradeChecklistItemDTO::fromEntity, Collectors.toList())
                        ));

        return JournalEntryDTO.fromEntity(entry, screenshotsByTradeId, checklistByTradeId);
    }

    public JournalEntryDTO updateJournalEntry(User user, UUID id, UpdateJournalEntryRequest request) {
        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found");
        }

        if (request.notes() != null) entry.setNotes(request.notes());
        if (request.mood() != null) entry.setMood(request.mood());
        if (request.energy() != null) entry.setEnergy(request.energy());
        if (request.marketBias() != null) entry.setMarketBias(request.marketBias());
        if (request.dailyGoal() != null) entry.setDailyGoal(request.dailyGoal());
        if (request.dayRating() != null) entry.setDayRating(request.dayRating());

        JournalEntry updated = journalEntryRepository.save(entry);
        return JournalEntryDTO.fromEntity(updated);
    }
}

package com.tradingjournal.application.journal;

import com.tradingjournal.domain.entity.JournalEntry;
import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.JournalEntryRepository;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import com.tradingjournal.presentation.dto.CloseTradeRequest;
import com.tradingjournal.presentation.dto.CreateTradeRequest;
import com.tradingjournal.presentation.dto.TradeDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TradeService {

    private final TradeRepository tradeRepository;
    private final JournalEntryRepository journalEntryRepository;

    public TradeService(TradeRepository tradeRepository, JournalEntryRepository journalEntryRepository) {
        this.tradeRepository = tradeRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    public TradeDTO createTrade(User user, CreateTradeRequest request) {
        JournalEntry journalEntry = journalEntryRepository.findById(request.journalEntryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found"));

        if (!journalEntry.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found");
        }

        Trade trade = new Trade(
                journalEntry,
                request.ticker(),
                request.positionType(),
                request.entryPrice(),
                request.quantity()
        );

        Trade saved = tradeRepository.save(trade);
        return TradeDTO.fromEntity(saved);
    }

    public TradeDTO closeTrade(User user, UUID tradeId, CloseTradeRequest request) {
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found"));

        if (!trade.getJournalEntry().getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found");
        }

        trade.setExitPrice(request.exitPrice());
        trade.setExitDate(Instant.now());

        Trade saved = tradeRepository.save(trade);
        return TradeDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<TradeDTO> getTrades(User user, UUID journalEntryId) {
        if (journalEntryId != null) {
            JournalEntry journalEntry = journalEntryRepository.findById(journalEntryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found"));

            if (!journalEntry.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found");
            }

            return tradeRepository.findByJournalEntry_JournalEntryId(journalEntryId)
                    .stream()
                    .map(TradeDTO::fromEntity)
                    .toList();
        }

        return tradeRepository.findByJournalEntry_User(user)
                .stream()
                .map(TradeDTO::fromEntity)
                .toList();
    }
}

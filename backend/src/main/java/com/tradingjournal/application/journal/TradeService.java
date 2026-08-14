package com.tradingjournal.application.journal;

import com.tradingjournal.domain.entity.JournalEntry;
import com.tradingjournal.domain.entity.PositionType;
import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.TradeOutcomeFilter;
import com.tradingjournal.domain.entity.TradeStatusFilter;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.JournalEntryRepository;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import com.tradingjournal.presentation.dto.CloseTradeRequest;
import com.tradingjournal.presentation.dto.CreateTradeRequest;
import com.tradingjournal.presentation.dto.TradeDTO;
import com.tradingjournal.presentation.dto.UpdateTradeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class TradeService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final TradeRepository tradeRepository;
    private final JournalEntryRepository journalEntryRepository;

    public TradeService(TradeRepository tradeRepository, JournalEntryRepository journalEntryRepository) {
        this.tradeRepository = tradeRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    public TradeDTO createTrade(User user, CreateTradeRequest request) {
        JournalEntry journalEntry = findOwnedJournalEntryOrThrow(user, request.journalEntryId());

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

    public TradeDTO updateTrade(User user, UUID tradeId, UpdateTradeRequest request) {
        Trade trade = findOwnedTradeOrThrow(user, tradeId);

        if (trade.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found");
        }

        if (trade.getExitPrice() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Closed trades cannot be edited");
        }

        trade.setTicker(request.ticker());
        trade.setPositionType(request.positionType());
        trade.setEntryPrice(request.entryPrice());
        trade.setQuantity(request.quantity());
        trade.setStopLoss(request.stopLoss());
        trade.setStrategy(normalizeStrategy(request.strategy()));

        Trade saved = tradeRepository.save(trade);
        return TradeDTO.fromEntity(saved);
    }

    public TradeDTO closeTrade(User user, UUID tradeId, CloseTradeRequest request) {
        Trade trade = findOwnedTradeOrThrow(user, tradeId);

        if (trade.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found");
        }

        if (trade.getExitPrice() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Trade is already closed");
        }

        trade.setExitPrice(request.exitPrice());
        trade.setExitDate(Instant.now());
        trade.setFees(request.fees() != null ? request.fees() : ZERO);

        Trade saved = tradeRepository.save(trade);
        return TradeDTO.fromEntity(saved);
    }

    public void deleteTrade(User user, UUID tradeId) {
        Trade trade = findOwnedTradeOrThrow(user, tradeId);
        trade.setDeleted(true);
        tradeRepository.save(trade);
    }

    @Transactional(readOnly = true)
    public Page<TradeDTO> getTrades(
            User user,
            String ticker,
            String strategy,
            TradeStatusFilter status,
            TradeOutcomeFilter outcome,
            LocalDate fromDate,
            LocalDate toDate,
            boolean includeArchived,
            Pageable pageable
    ) {
        Specification<Trade> spec = Specification.where(ownedBy(user))
                .and(notDeletedIfNeeded(includeArchived))
                .and(tickerContains(ticker))
                .and(strategyContains(strategy))
                .and(statusIs(status))
                .and(outcomeIs(outcome))
                .and(entryDateGte(fromDate))
                .and(entryDateLte(toDate));

        return tradeRepository.findAll(spec, pageable).map(TradeDTO::fromEntity);
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

    private JournalEntry findOwnedJournalEntryOrThrow(User user, UUID journalEntryId) {
        JournalEntry journalEntry = journalEntryRepository.findById(journalEntryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found"));

        if (journalEntry.getUser() == null || !journalEntry.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found");
        }

        return journalEntry;
    }

    private Specification<Trade> ownedBy(User user) {
        return (root, query, cb) -> cb.equal(root.<JournalEntry>get("journalEntry").<User>get("user").get("id"), user.getId());
    }

    private Specification<Trade> notDeletedIfNeeded(boolean includeArchived) {
        if (includeArchived) {
            return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    private Specification<Trade> tickerContains(String ticker) {
        return (root, query, cb) -> {
            if (ticker == null || ticker.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + ticker.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("ticker")), pattern);
        };
    }

    private Specification<Trade> strategyContains(String strategy) {
        return (root, query, cb) -> {
            if (strategy == null || strategy.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + strategy.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("strategy")), pattern);
        };
    }

    private Specification<Trade> statusIs(TradeStatusFilter status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return status == TradeStatusFilter.OPEN
                    ? cb.isNull(root.<BigDecimal>get("exitPrice"))
                    : cb.isNotNull(root.<BigDecimal>get("exitPrice"));
        };
    }

    private Specification<Trade> outcomeIs(TradeOutcomeFilter outcome) {
        return (root, query, cb) -> {
            if (outcome == null) {
                return cb.conjunction();
            }

            var grossPnl = grossPnlExpression(root, cb);
            var netPnl = cb.diff(grossPnl, coalesceFees(root, cb));

            return switch (outcome) {
                case WIN -> cb.greaterThan(netPnl, ZERO);
                case LOSS -> cb.lessThan(netPnl, ZERO);
                case BREAKEVEN -> cb.equal(netPnl, ZERO);
            };
        };
    }

    private Specification<Trade> entryDateGte(LocalDate fromDate) {
        return (root, query, cb) -> {
            if (fromDate == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.<JournalEntry>get("journalEntry").<LocalDate>get("entryDate"), fromDate);
        };
    }

    private Specification<Trade> entryDateLte(LocalDate toDate) {
        return (root, query, cb) -> {
            if (toDate == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.<JournalEntry>get("journalEntry").<LocalDate>get("entryDate"), toDate);
        };
    }

    private jakarta.persistence.criteria.Expression<BigDecimal> grossPnlExpression(
            jakarta.persistence.criteria.Root<Trade> root,
            jakarta.persistence.criteria.CriteriaBuilder cb
    ) {
        var positionType = root.<PositionType>get("positionType");
        var entryPrice = root.<BigDecimal>get("entryPrice");
        var exitPrice = root.<BigDecimal>get("exitPrice");
        var quantity = root.<BigDecimal>get("quantity");

        var longGross = cb.prod(cb.diff(exitPrice, entryPrice), quantity);
        var shortGross = cb.prod(cb.diff(entryPrice, exitPrice), quantity);

        return cb.<BigDecimal>selectCase()
                .when(cb.equal(positionType, PositionType.LONG), longGross)
                .otherwise(shortGross);
    }

    private jakarta.persistence.criteria.Expression<BigDecimal> coalesceFees(
            jakarta.persistence.criteria.Root<Trade> root,
            jakarta.persistence.criteria.CriteriaBuilder cb
    ) {
        return cb.<BigDecimal>coalesce()
                .value(root.<BigDecimal>get("fees"))
                .value(ZERO);
    }

    private String normalizeStrategy(String strategy) {
        if (strategy == null) {
            return null;
        }

        String trimmed = strategy.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

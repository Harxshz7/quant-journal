package com.tradingjournal.application.journal;

import com.tradingjournal.domain.entity.*;
import com.tradingjournal.application.statistics.StatisticsService;
import com.tradingjournal.infrastructure.repository.*;
import com.tradingjournal.presentation.dto.*;
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
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TradeService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final TradeRepository tradeRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final StatisticsService statisticsService;
    private final TradeScreenshotRepository tradeScreenshotRepository;
    private final ChecklistItemTemplateRepository checklistTemplateRepository;
    private final TradeChecklistItemRepository tradeChecklistItemRepository;

    public TradeService(
            TradeRepository tradeRepository,
            JournalEntryRepository journalEntryRepository,
            StatisticsService statisticsService,
            TradeScreenshotRepository tradeScreenshotRepository,
            ChecklistItemTemplateRepository checklistTemplateRepository,
            TradeChecklistItemRepository tradeChecklistItemRepository
    ) {
        this.tradeRepository = tradeRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.statisticsService = statisticsService;
        this.tradeScreenshotRepository = tradeScreenshotRepository;
        this.checklistTemplateRepository = checklistTemplateRepository;
        this.tradeChecklistItemRepository = tradeChecklistItemRepository;
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
        trade.setStopLoss(request.stopLoss());

        Trade saved = tradeRepository.save(trade);

        // Snapshot checklist items from templates
        if (request.checklistItemIds() != null && !request.checklistItemIds().isEmpty()) {
            List<ChecklistItemTemplate> templates = checklistTemplateRepository.findAllById(request.checklistItemIds());
            for (ChecklistItemTemplate template : templates) {
                if (!template.getUser().getId().equals(user.getId()) || !template.isActive()) continue;
                TradeChecklistItem item = new TradeChecklistItem(saved, template.getText());
                tradeChecklistItemRepository.save(item);
            }
        }

        var screenshots = tradeScreenshotRepository.findByTrade_TradeId(saved.getId())
                .stream().map(TradeScreenshotDTO::fromEntity).toList();
        var checklist = tradeChecklistItemRepository.findByTrade_IdOrderByIdAsc(saved.getId())
                .stream().map(TradeChecklistItemDTO::fromEntity).toList();
        return TradeDTO.fromEntity(saved, screenshots, checklist);
    }

    public TradeDTO updateTrade(User user, UUID tradeId, UpdateTradeRequest request) {
        Trade trade = findOwnedTradeOrThrow(user, tradeId);

        if (trade.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trade not found");
        }

        boolean isClosed = trade.getExitPrice() != null;

        if (isClosed) {
            // Closed trades: only allow reflection fields to be edited
            boolean onlyReflectionFields =
                    request.postTradeReflection() != null ||
                    request.mistakeTags() != null ||
                    request.setupQuality() != null;
            boolean hasOtherChanges =
                    !request.ticker().equals(trade.getTicker()) ||
                    request.positionType() != trade.getPositionType() ||
                    request.entryPrice().compareTo(trade.getEntryPrice()) != 0 ||
                    request.quantity().compareTo(trade.getQuantity()) != 0 ||
                    (request.stopLoss() != null ? request.stopLoss().compareTo(trade.getStopLoss() != null ? trade.getStopLoss() : BigDecimal.ZERO) != 0 : trade.getStopLoss() != null) ||
                    (request.strategy() != null ? !request.strategy().equals(trade.getStrategy()) : trade.getStrategy() != null);

            if (hasOtherChanges) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Closed trades cannot be edited");
            }

            // Apply only reflection fields
            applyReflectionFields(trade, request);
        } else {
            trade.setTicker(request.ticker());
            trade.setPositionType(request.positionType());
            trade.setEntryPrice(request.entryPrice());
            trade.setQuantity(request.quantity());
            trade.setStopLoss(request.stopLoss());
            trade.setStrategy(normalizeStrategy(request.strategy()));
            applyReflectionFields(trade, request);
        }

        Trade saved = tradeRepository.save(trade);
        statisticsService.recalculate(user);
        var screenshots = tradeScreenshotRepository.findByTrade_TradeId(saved.getId())
                .stream().map(TradeScreenshotDTO::fromEntity).toList();
        var checklist = tradeChecklistItemRepository.findByTrade_IdOrderByIdAsc(saved.getId())
                .stream().map(TradeChecklistItemDTO::fromEntity).toList();
        return TradeDTO.fromEntity(saved, screenshots, checklist);
    }

    private void applyReflectionFields(Trade trade, UpdateTradeRequest request) {
        if (request.postTradeReflection() != null) trade.setPostTradeReflection(request.postTradeReflection());
        if (request.mistakeTags() != null) trade.setMistakeTags(request.mistakeTags());
        if (request.setupQuality() != null) {
            if (request.setupQuality() < 1 || request.setupQuality() > 5) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Setup quality must be between 1 and 5");
            }
            trade.setSetupQuality(request.setupQuality());
        }
    }

    public TradeDTO toggleChecklistItem(User user, UUID tradeId, UUID itemId, boolean checked) {
        Trade trade = findOwnedTradeOrThrow(user, tradeId);
        TradeChecklistItem item = tradeChecklistItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist item not found"));
        if (!item.getTrade().getId().equals(trade.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist item not found");
        }
        item.setChecked(checked);
        tradeChecklistItemRepository.save(item);

        var screenshots = tradeScreenshotRepository.findByTrade_TradeId(trade.getId())
                .stream().map(TradeScreenshotDTO::fromEntity).toList();
        var checklist = tradeChecklistItemRepository.findByTrade_IdOrderByIdAsc(trade.getId())
                .stream().map(TradeChecklistItemDTO::fromEntity).toList();
        return TradeDTO.fromEntity(trade, screenshots, checklist);
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
        statisticsService.recalculate(user);
        var screenshots = tradeScreenshotRepository.findByTrade_TradeId(saved.getId())
                .stream().map(TradeScreenshotDTO::fromEntity).toList();
        var checklist = tradeChecklistItemRepository.findByTrade_IdOrderByIdAsc(saved.getId())
                .stream().map(TradeChecklistItemDTO::fromEntity).toList();
        return TradeDTO.fromEntity(saved, screenshots, checklist);
    }

    public void deleteTrade(User user, UUID tradeId) {
        Trade trade = findOwnedTradeOrThrow(user, tradeId);
        trade.setDeleted(true);
        tradeRepository.save(trade);
        statisticsService.recalculate(user);
    }

    @Transactional(readOnly = true)
    public Page<TradeDTO> getTrades(
            User user,
            UUID accountId,
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
                .and(accountIs(accountId))
                .and(notDeletedIfNeeded(includeArchived))
                .and(tickerContains(ticker))
                .and(strategyContains(strategy))
                .and(statusIs(status))
                .and(outcomeIs(outcome))
                .and(entryDateGte(fromDate))
                .and(entryDateLte(toDate));

        Page<Trade> page = tradeRepository.findAll(spec, pageable);

        if (!page.getContent().isEmpty()) {
            List<UUID> tradeIds = page.getContent().stream().map(Trade::getId).toList();
            tradeRepository.findWithJournalEntryByIds(tradeIds);
        }

        return page.map(TradeDTO::fromEntity);
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
        return (root, query, cb) -> {
            var journalEntry = root.join("journalEntry");
            var owner = journalEntry.join("user");
            return cb.equal(owner.get("id"), user.getId());
        };
    }

    private Specification<Trade> accountIs(UUID accountId) {
        return (root, query, cb) -> {
            if (accountId == null) return cb.conjunction();
            var journalEntry = root.join("journalEntry");
            return cb.equal(journalEntry.get("account").get("id"), accountId);
        };
    }

    private Specification<Trade> notDeletedIfNeeded(boolean includeArchived) {
        if (includeArchived) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    private Specification<Trade> tickerContains(String ticker) {
        return (root, query, cb) -> {
            if (ticker == null || ticker.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("ticker")), "%" + ticker.trim().toLowerCase() + "%");
        };
    }

    private Specification<Trade> strategyContains(String strategy) {
        return (root, query, cb) -> {
            if (strategy == null || strategy.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("strategy")), "%" + strategy.trim().toLowerCase() + "%");
        };
    }

    private Specification<Trade> statusIs(TradeStatusFilter status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return status == TradeStatusFilter.OPEN
                    ? cb.isNull(root.<BigDecimal>get("exitPrice"))
                    : cb.isNotNull(root.<BigDecimal>get("exitPrice"));
        };
    }

    private Specification<Trade> outcomeIs(TradeOutcomeFilter outcome) {
        return (root, query, cb) -> {
            if (outcome == null) return cb.conjunction();
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
            if (fromDate == null) return cb.conjunction();
            var journalEntry = root.join("journalEntry");
            return cb.greaterThanOrEqualTo(journalEntry.get("entryDate"), fromDate);
        };
    }

    private Specification<Trade> entryDateLte(LocalDate toDate) {
        return (root, query, cb) -> {
            if (toDate == null) return cb.conjunction();
            var journalEntry = root.join("journalEntry");
            return cb.lessThanOrEqualTo(journalEntry.get("entryDate"), toDate);
        };
    }

    private jakarta.persistence.criteria.Expression<BigDecimal> grossPnlExpression(
            jakarta.persistence.criteria.Root<Trade> root,
            jakarta.persistence.criteria.CriteriaBuilder cb) {
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
            jakarta.persistence.criteria.CriteriaBuilder cb) {
        return cb.<BigDecimal>coalesce()
                .value(root.<BigDecimal>get("fees"))
                .value(ZERO);
    }

    private String normalizeStrategy(String strategy) {
        if (strategy == null) return null;
        String trimmed = strategy.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

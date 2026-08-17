package com.tradingjournal.application.webhook;

import com.tradingjournal.application.account.AccountService;
import com.tradingjournal.application.statistics.StatisticsService;
import com.tradingjournal.domain.entity.*;
import com.tradingjournal.infrastructure.repository.JournalEntryRepository;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import com.tradingjournal.presentation.dto.TradingViewWebhookPayload;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final TradeRepository tradeRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final StatisticsService statisticsService;
    private final AccountService accountService;

    public WebhookService(
            TradeRepository tradeRepository,
            JournalEntryRepository journalEntryRepository,
            StatisticsService statisticsService,
            AccountService accountService
    ) {
        this.tradeRepository = tradeRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.statisticsService = statisticsService;
        this.accountService = accountService;
    }

    public Map<String, Object> processTradingViewAlert(
            User user,
            TradingViewWebhookPayload payload,
            HttpServletRequest request
    ) {
        String ip = request.getRemoteAddr();
        log.info("Webhook received  user={}, ip={}, ticker={}, action={}, price={}, quantity={}, strategy={}, time={}",
                user.getEmail(), ip,
                payload.ticker(), payload.action(), payload.price(),
                payload.quantity(), payload.strategy(), payload.time());

        if (payload.ticker() == null || payload.ticker().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ticker is required");
        }
        if (payload.action() == null || payload.action().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "action is required");
        }
        if (payload.parsedPrice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "price is required and must be a valid number");
        }

        String action = payload.action().trim().toLowerCase();
        String ticker = payload.ticker().trim().toUpperCase();
        BigDecimal price = payload.parsedPrice();
        BigDecimal quantity = payload.parsedQuantity();
        String strategy = payload.strategy() != null && !payload.strategy().isBlank()
                ? payload.strategy().trim() : null;

        return switch (action) {
            case "buy", "sell" -> handleOpen(user, ticker, action, price, quantity, strategy);
            case "close" -> handleClose(user, ticker, price);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "action must be one of: buy, sell, close");
        };
    }

    private Map<String, Object> handleOpen(
            User user, String ticker, String action,
            BigDecimal price, BigDecimal quantity, String strategy
    ) {
        PositionType positionType = "buy".equals(action) ? PositionType.LONG : PositionType.SHORT;

        JournalEntry journalEntry = findOrCreateJournalEntry(user, LocalDate.now());

        Trade trade = new Trade(journalEntry, ticker, positionType, price, quantity);
        trade.setSource(TradeSource.WEBHOOK);
        if (strategy != null) trade.setStrategy(strategy);

        Trade saved = tradeRepository.save(trade);
        statisticsService.recalculate(user);

        log.info("Webhook trade created  tradeId={}, ticker={}, action={}, price={}, quantity={}",
                saved.getId(), ticker, action, price, quantity);

        return Map.of(
                "status", "created",
                "tradeId", saved.getId().toString(),
                "message", "Trade created: " + action.toUpperCase() + " " + quantity + " " + ticker + " @ " + price
        );
    }

    private Map<String, Object> handleClose(User user, String ticker, BigDecimal price) {
        List<Trade> openTrades = tradeRepository.findOpenByTicker(user, ticker);

        if (openTrades.isEmpty()) {
            log.warn("Webhook close  no open trade found for ticker={}", ticker);
            return Map.of(
                    "status", "no_match",
                    "message", "No open trade found for " + ticker + "  alert ignored"
            );
        }

        Trade trade = openTrades.get(0);
        trade.setExitPrice(price);
        trade.setExitDate(Instant.now());
        trade.setFees(BigDecimal.ZERO);

        Trade saved = tradeRepository.save(trade);
        statisticsService.recalculate(user);

        log.info("Webhook trade closed  tradeId={}, ticker={}, exitPrice={}",
                saved.getId(), ticker, price);

        return Map.of(
                "status", "closed",
                "tradeId", saved.getId().toString(),
                "message", "Trade closed: " + ticker + " @ " + price
        );
    }

    private JournalEntry findOrCreateJournalEntry(User user, LocalDate date) {
        Account defaultAccount = accountService.getDefaultAccount(user);
        Optional<JournalEntry> existing = defaultAccount != null
                ? journalEntryRepository.findByUserAndAccountAndEntryDate(user, defaultAccount, date)
                : journalEntryRepository.findByUserAndEntryDate(user, date);
        if (existing.isPresent()) {
            return existing.get();
        }
        JournalEntry newEntry = new JournalEntry(user, date, "Auto-created via TradingView webhook");
        newEntry.setAccount(defaultAccount);
        return journalEntryRepository.save(newEntry);
    }
}

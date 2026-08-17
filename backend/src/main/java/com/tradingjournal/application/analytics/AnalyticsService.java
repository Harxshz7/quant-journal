package com.tradingjournal.application.analytics;

import com.tradingjournal.application.account.AccountService;
import com.tradingjournal.domain.entity.Account;
import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import com.tradingjournal.presentation.dto.BreakdownEntryDTO;
import com.tradingjournal.presentation.dto.DrawdownDTO;
import com.tradingjournal.presentation.dto.EquityPointDTO;
import com.tradingjournal.presentation.dto.TimeBreakdownDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final TradeRepository tradeRepository;
    private final AccountService accountService;

    @Value("${app.timezone:Asia/Kolkata}")
    private String appTimezone;

    public AnalyticsService(TradeRepository tradeRepository, AccountService accountService) {
        this.tradeRepository = tradeRepository;
        this.accountService = accountService;
    }

    private ZoneId zone() {
        return ZoneId.of(appTimezone);
    }

    private List<Trade> closedTrades(User user, UUID accountId, LocalDate fromDate, LocalDate toDate) {
        Account account = accountId != null ? accountService.resolveOwnedAccount(user, accountId) : null;
        Instant from = fromDate != null ? fromDate.atStartOfDay(ZoneId.of("UTC")).toInstant() : null;
        Instant to = toDate != null ? toDate.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant() : null;
        return tradeRepository.findClosedTradesInRange(user, account, from, to);
    }

    //  Equity Curve 

    public List<EquityPointDTO> equityCurve(User user, UUID accountId, LocalDate fromDate, LocalDate toDate) {
        List<Trade> trades = closedTrades(user, accountId, fromDate, toDate);
        List<EquityPointDTO> points = new ArrayList<>();
        BigDecimal cumulative = BigDecimal.ZERO;

        for (Trade t : trades) {
            BigDecimal net = PnlCalculator.netPnl(t);
            if (net == null) continue;
            cumulative = cumulative.add(net);
            points.add(new EquityPointDTO(t.getExitDate(), cumulative.setScale(2, RoundingMode.HALF_UP)));
        }
        return points;
    }

    //  Drawdown 

    public DrawdownDTO drawdown(User user, UUID accountId, LocalDate fromDate, LocalDate toDate) {
        List<Trade> trades = closedTrades(user, accountId, fromDate, toDate);

        BigDecimal equity = BigDecimal.ZERO;
        BigDecimal highestEquity = BigDecimal.ZERO;
        Instant peakDt = null;
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        BigDecimal maxDrawdownPct = BigDecimal.ZERO;
        Instant maxPeakDt = null;
        Instant maxTroughDt = null;

        for (Trade t : trades) {
            BigDecimal net = PnlCalculator.netPnl(t);
            if (net == null) continue;

            equity = equity.add(net);
            Instant exitDt = t.getExitDate();

            if (equity.compareTo(highestEquity) > 0) {
                highestEquity = equity;
                peakDt = exitDt;
            }

            BigDecimal drawdown = highestEquity.subtract(equity);
            BigDecimal drawdownPct = highestEquity.compareTo(BigDecimal.ZERO) != 0
                    ? drawdown.multiply(HUNDRED).divide(highestEquity.abs(), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
                maxDrawdownPct = drawdownPct;
                maxPeakDt = peakDt;
                maxTroughDt = exitDt;
            }
        }

        return new DrawdownDTO(
                maxDrawdown.setScale(2, RoundingMode.HALF_UP),
                maxDrawdownPct.setScale(2, RoundingMode.HALF_UP),
                maxPeakDt,
                maxTroughDt
        );
    }

    //  By Strategy 

    public List<BreakdownEntryDTO> byStrategy(User user, UUID accountId, LocalDate fromDate, LocalDate toDate) {
        List<Trade> trades = closedTrades(user, accountId, fromDate, toDate);
        Map<String, List<Trade>> grouped = trades.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getStrategy() != null ? t.getStrategy() : "Unspecified",
                        LinkedHashMap::new,
                        Collectors.toList()));

        return grouped.entrySet().stream()
                .map(e -> buildBreakdown(e.getKey(), e.getValue()))
                .toList();
    }

    //  By Ticker 

    public List<BreakdownEntryDTO> byTicker(User user, UUID accountId, LocalDate fromDate, LocalDate toDate) {
        List<Trade> trades = closedTrades(user, accountId, fromDate, toDate);
        Map<String, List<Trade>> grouped = trades.stream()
                .collect(Collectors.groupingBy(
                        Trade::getTicker,
                        LinkedHashMap::new,
                        Collectors.toList()));

        return grouped.entrySet().stream()
                .map(e -> buildBreakdown(e.getKey(), e.getValue()))
                .toList();
    }

    //  By Day of Week 

    public List<TimeBreakdownDTO> byDayOfWeek(User user, UUID accountId, LocalDate fromDate, LocalDate toDate) {
        List<Trade> trades = closedTrades(user, accountId, fromDate, toDate);
        Map<DayOfWeek, List<Trade>> grouped = trades.stream()
                .filter(t -> t.getExitDate() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getExitDate().atZone(zone()).getDayOfWeek(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        return grouped.entrySet().stream()
                .map(e -> buildTimeBreakdown(e.getKey().getDisplayName(TextStyle.FULL, Locale.ENGLISH), e.getValue()))
                .toList();
    }

    //  By Hour 

    public List<TimeBreakdownDTO> byHour(User user, UUID accountId, LocalDate fromDate, LocalDate toDate) {
        List<Trade> trades = closedTrades(user, accountId, fromDate, toDate);
        Map<Integer, List<Trade>> grouped = trades.stream()
                .filter(t -> t.getExitDate() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getExitDate().atZone(zone()).getHour(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> buildTimeBreakdown(String.valueOf(e.getKey()), e.getValue()))
                .toList();
    }

    //  Monthly 

    public List<TimeBreakdownDTO> monthly(User user, UUID accountId, LocalDate fromDate, LocalDate toDate) {
        List<Trade> trades = closedTrades(user, accountId, fromDate, toDate);
        Map<String, List<Trade>> grouped = trades.stream()
                .filter(t -> t.getExitDate() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getExitDate().atZone(zone()).format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        LinkedHashMap::new,
                        Collectors.toList()));

        return grouped.entrySet().stream()
                .map(e -> buildTimeBreakdown(e.getKey(), e.getValue()))
                .toList();
    }

    //  Weekly 

    public List<TimeBreakdownDTO> weekly(User user, UUID accountId, LocalDate fromDate, LocalDate toDate) {
        List<Trade> trades = closedTrades(user, accountId, fromDate, toDate);
        WeekFields wf = WeekFields.ISO;
        Map<String, List<Trade>> grouped = trades.stream()
                .filter(t -> t.getExitDate() != null)
                .collect(Collectors.groupingBy(
                        t -> {
                            LocalDate d = t.getExitDate().atZone(zone()).toLocalDate();
                            int year = d.getYear();
                            int week = d.get(wf.weekOfWeekBasedYear());
                            return year + "-W" + String.format("%02d", week);
                        },
                        LinkedHashMap::new,
                        Collectors.toList()));

        return grouped.entrySet().stream()
                .map(e -> buildTimeBreakdown(e.getKey(), e.getValue()))
                .toList();
    }

    //  Helpers 

    private BreakdownEntryDTO buildBreakdown(String group, List<Trade> trades) {
        int total = 0;
        int wins = 0;
        BigDecimal sumWins = BigDecimal.ZERO;
        BigDecimal sumLosses = BigDecimal.ZERO;
        BigDecimal net = BigDecimal.ZERO;

        for (Trade t : trades) {
            String outcome = PnlCalculator.outcome(t);
            if (outcome == null) continue;
            total++;
            BigDecimal tradeNet = PnlCalculator.netPnl(t);
            if (tradeNet != null) {
                net = net.add(tradeNet);
                if ("WIN".equals(outcome)) {
                    wins++;
                    sumWins = sumWins.add(tradeNet);
                } else if ("LOSS".equals(outcome)) {
                    sumLosses = sumLosses.add(tradeNet);
                }
            }
        }

        int decided = wins + (total - wins - countBreakEven(trades));
        BigDecimal winRate = total > 0
                ? HUNDRED.multiply(BigDecimal.valueOf(wins))
                        .divide(BigDecimal.valueOf(decided > 0 ? decided : 1), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal profitFactor = null;
        BigDecimal grossLosses = sumLosses.abs();
        if (grossLosses.compareTo(BigDecimal.ZERO) != 0) {
            profitFactor = sumWins.divide(grossLosses, 4, RoundingMode.HALF_UP);
        }

        return new BreakdownEntryDTO(group, total, winRate.setScale(2, RoundingMode.HALF_UP),
                net.setScale(2, RoundingMode.HALF_UP), profitFactor);
    }

    private int countBreakEven(List<Trade> trades) {
        int count = 0;
        for (Trade t : trades) {
            if ("BREAKEVEN".equals(PnlCalculator.outcome(t))) count++;
        }
        return count;
    }

    private TimeBreakdownDTO buildTimeBreakdown(String period, List<Trade> trades) {
        int total = 0;
        int wins = 0;
        BigDecimal net = BigDecimal.ZERO;

        for (Trade t : trades) {
            String outcome = PnlCalculator.outcome(t);
            if (outcome == null) continue;
            total++;
            BigDecimal tradeNet = PnlCalculator.netPnl(t);
            if (tradeNet != null) {
                net = net.add(tradeNet);
                if ("WIN".equals(outcome)) wins++;
            }
        }

        int decided = wins + (total - wins - countBreakEven(trades));
        BigDecimal winRate = total > 0
                ? HUNDRED.multiply(BigDecimal.valueOf(wins))
                        .divide(BigDecimal.valueOf(decided > 0 ? decided : 1), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new TimeBreakdownDTO(period, total, net.setScale(2, RoundingMode.HALF_UP),
                winRate.setScale(2, RoundingMode.HALF_UP));
    }
}

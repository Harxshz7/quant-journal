package com.tradingjournal.application.statistics;

import com.tradingjournal.application.account.AccountService;
import com.tradingjournal.domain.entity.JournalEntry;
import com.tradingjournal.domain.entity.PositionType;
import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.TradeStatistics;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import com.tradingjournal.infrastructure.repository.TradeStatisticsRepository;
import com.tradingjournal.presentation.dto.StatisticsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeStatisticsRepository statisticsRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private StatisticsService statisticsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Trader One", "trader1@example.com", "hash1");
        user.setId(UUID.randomUUID());
    }

    @Test
    void recalculate_ComputesAggregateMetrics() {
        JournalEntry entry1 = new JournalEntry(user, LocalDate.of(2026, 8, 10), "e1");
        entry1.setId(UUID.randomUUID());
        JournalEntry entry2 = new JournalEntry(user, LocalDate.of(2026, 8, 11), "e2");
        entry2.setId(UUID.randomUUID());
        JournalEntry entry3 = new JournalEntry(user, LocalDate.of(2026, 8, 12), "e3");
        entry3.setId(UUID.randomUUID());
        JournalEntry entry4 = new JournalEntry(user, LocalDate.of(2026, 8, 13), "e4");
        entry4.setId(UUID.randomUUID());
        JournalEntry entry5 = new JournalEntry(user, LocalDate.of(2026, 8, 14), "e5");
        entry5.setId(UUID.randomUUID());

        Trade win1 = trade(entry1, PositionType.LONG, "AAPL", "100", "10", "105", "95");
        Trade win2 = trade(entry2, PositionType.SHORT, "MSFT", "200", "5", "196", "204");
        Trade loss1 = trade(entry3, PositionType.LONG, "TSLA", "50", "4", "47.5", "48");
        Trade breakeven = trade(entry4, PositionType.LONG, "NVDA", "300", "2", "300", null);
        Trade loss2 = trade(entry5, PositionType.SHORT, "AMD", "90", "1", "95", "100");

        when(tradeRepository.findClosedActiveTradesForStatistics(user, null)).thenReturn(List.of(win1, win2, loss1, breakeven, loss2));
        when(statisticsRepository.findByUser(user)).thenReturn(Optional.empty());
        when(statisticsRepository.save(any(TradeStatistics.class))).thenAnswer(inv -> inv.getArgument(0));

        TradeStatistics stats = statisticsService.recalculate(user);

        assertEquals(5, stats.getTotalTrades());
        assertEquals(2, stats.getWinCount());
        assertEquals(2, stats.getLossCount());
        assertEquals(1, stats.getBreakEvenCount());
        assertEquals(new BigDecimal("50.0000"), stats.getWinRate());
        assertEquals(new BigDecimal("4.6667"), stats.getProfitFactor());
        assertEquals(new BigDecimal("35.0000"), stats.getAvgWin());
        assertEquals(new BigDecimal("-7.5000"), stats.getAvgLoss());
        assertEquals(new BigDecimal("50.0000"), stats.getLargestWin());
        assertEquals(new BigDecimal("-10.0000"), stats.getLargestLoss());
        assertEquals(2, stats.getMaxConsecutiveWins());
        assertEquals(1, stats.getMaxConsecutiveLosses());
        assertEquals(new BigDecimal("0.9375"), stats.getAvgRiskReward());
        assertNotNull(stats.getExpectancy());
        assertNotNull(stats.getRiskOfRuin());
    }

    @Test
    void recalculate_ComputesExpectancyAndRiskOfRuin() {
        JournalEntry entry1 = new JournalEntry(user, LocalDate.of(2026, 8, 10), "e1");
        entry1.setId(UUID.randomUUID());
        JournalEntry entry2 = new JournalEntry(user, LocalDate.of(2026, 8, 11), "e2");
        entry2.setId(UUID.randomUUID());

        Trade win = trade(entry1, PositionType.LONG, "AAPL", "100", "10", "110", "95");
        Trade loss = trade(entry2, PositionType.LONG, "MSFT", "50", "4", "49", "52");

        when(tradeRepository.findClosedActiveTradesForStatistics(user, null)).thenReturn(List.of(win, loss));
        when(statisticsRepository.findByUser(user)).thenReturn(Optional.empty());
        when(statisticsRepository.save(any(TradeStatistics.class))).thenAnswer(inv -> inv.getArgument(0));

        TradeStatistics stats = statisticsService.recalculate(user);

        // winRate = 50%, avgWin = 100, avgLoss = -4
        // expectancy = 0.5*100 + 0.5*(-4) = 48
        assertEquals(new BigDecimal("48.0000"), stats.getExpectancy());
        // edge = 0.5*(100/4) - 0.5 = 12, units = 100, RoR = ((1-12)/(1+12))^100 -> essentially 0
        assertNotNull(stats.getRiskOfRuin());
        assertEquals(0, stats.getRiskOfRuin().compareTo(BigDecimal.ZERO));
    }

    @Test
    void getStatistics_WithoutRow_ReturnsDefaultDto() {
        when(statisticsRepository.findByUser(user)).thenReturn(Optional.empty());

        StatisticsDTO dto = statisticsService.getStatistics(user, null, null, null);

        assertEquals(0, dto.totalTrades());
        assertEquals(BigDecimal.ZERO, dto.winRate());
        assertNull(dto.profitFactor());
        assertNull(dto.updatedAt());
        assertNull(dto.expectancy());
        assertNull(dto.riskOfRuin());
    }

    private Trade trade(
            JournalEntry entry,
            PositionType positionType,
            String ticker,
            String entryPrice,
            String quantity,
            String exitPrice,
            String stopLoss
    ) {
        Trade trade = new Trade(entry, ticker, positionType, new BigDecimal(entryPrice), new BigDecimal(quantity));
        trade.setId(UUID.randomUUID());
        trade.setExitPrice(new BigDecimal(exitPrice));
        trade.setExitDate(Instant.now());
        trade.setFees(BigDecimal.ZERO);
        if (stopLoss != null) {
            trade.setStopLoss(new BigDecimal(stopLoss));
        }
        return trade;
    }
}

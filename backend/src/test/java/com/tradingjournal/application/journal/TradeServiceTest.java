package com.tradingjournal.application.journal;

import com.tradingjournal.application.statistics.StatisticsService;
import com.tradingjournal.domain.entity.*;
import com.tradingjournal.infrastructure.repository.JournalEntryRepository;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import com.tradingjournal.presentation.dto.CloseTradeRequest;
import com.tradingjournal.presentation.dto.CreateTradeRequest;
import com.tradingjournal.presentation.dto.TradeDTO;
import com.tradingjournal.presentation.dto.UpdateTradeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private TradeService tradeService;

    private User user1;
    private User user2;
    private JournalEntry journalEntryUser1;

    @BeforeEach
    void setUp() {
        user1 = new User("Trader One", "trader1@example.com", "hash1");
        user1.setId(UUID.randomUUID());

        user2 = new User("Trader Two", "trader2@example.com", "hash2");
        user2.setId(UUID.randomUUID());

        journalEntryUser1 = new JournalEntry(user1, LocalDate.now(), "Notes");
        journalEntryUser1.setId(UUID.randomUUID());
    }

    @Test
    void createTrade_Success() {
        CreateTradeRequest req = new CreateTradeRequest(
                journalEntryUser1.getId(),
                "AAPL",
                PositionType.LONG,
                new BigDecimal("150.00"),
                new BigDecimal("10"),
                null,
                null
        );

        when(journalEntryRepository.findById(journalEntryUser1.getId())).thenReturn(Optional.of(journalEntryUser1));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(inv -> {
            Trade t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        TradeDTO dto = tradeService.createTrade(user1, req);

        assertNotNull(dto);
        assertEquals("AAPL", dto.ticker());
        assertEquals(PositionType.LONG, dto.positionType());
        assertEquals(new BigDecimal("150.00"), dto.entryPrice());
        assertEquals(new BigDecimal("10"), dto.quantity());
        assertEquals("OPEN", dto.status());
        assertNull(dto.exitPrice());
        assertNull(dto.realizedPnl());
        verify(tradeRepository).save(any(Trade.class));
        verify(statisticsService, never()).recalculate(any());
    }

    @Test
    void createTrade_ForOtherUserJournalEntry_ThrowsNotFound() {
        CreateTradeRequest req = new CreateTradeRequest(
                journalEntryUser1.getId(),
                "AAPL",
                PositionType.LONG,
                new BigDecimal("150.00"),
                new BigDecimal("10"),
                null,
                null
        );

        when(journalEntryRepository.findById(journalEntryUser1.getId())).thenReturn(Optional.of(journalEntryUser1));

        // user2 attempts to create trade for user1's journal entry
        assertThrows(ResponseStatusException.class, () -> tradeService.createTrade(user2, req));
    }

    @Test
    void updateTrade_Success() {
        UUID tradeId = UUID.randomUUID();
        Trade trade = new Trade(journalEntryUser1, "AAPL", PositionType.LONG, new BigDecimal("150.00"), new BigDecimal("10"));
        trade.setId(tradeId);

        UpdateTradeRequest req = new UpdateTradeRequest(
                "MSFT",
                PositionType.SHORT,
                new BigDecimal("250.00"),
                new BigDecimal("8"),
                new BigDecimal("245.00"),
                "Swing",
                null,
                null,
                null
        );

        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(inv -> inv.getArgument(0));

        TradeDTO dto = tradeService.updateTrade(user1, tradeId, req);

        assertNotNull(dto);
        assertEquals("MSFT", dto.ticker());
        assertEquals(PositionType.SHORT, dto.positionType());
        assertEquals(new BigDecimal("245.00"), dto.stopLoss());
        assertEquals("Swing", dto.strategy());
        verify(statisticsService).recalculate(user1);
    }

    @Test
    void updateTrade_ClosedTrade_ThrowsConflict() {
        UUID tradeId = UUID.randomUUID();
        Trade trade = new Trade(journalEntryUser1, "AAPL", PositionType.LONG, new BigDecimal("150.00"), new BigDecimal("10"));
        trade.setId(tradeId);
        trade.setExitPrice(new BigDecimal("170.00"));

        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));

        UpdateTradeRequest req = new UpdateTradeRequest(
                "MSFT",
                PositionType.SHORT,
                new BigDecimal("250.00"),
                new BigDecimal("8"),
                null,
                null,
                null,
                null,
                null
        );

        assertThrows(ResponseStatusException.class, () -> tradeService.updateTrade(user1, tradeId, req));
    }

    @Test
    void closeTrade_Success() {
        UUID tradeId = UUID.randomUUID();
        Trade trade = new Trade(journalEntryUser1, "AAPL", PositionType.LONG, new BigDecimal("150.00"), new BigDecimal("10"));
        trade.setId(tradeId);

        CloseTradeRequest req = new CloseTradeRequest(new BigDecimal("170.00"), new BigDecimal("4.00"));

        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(inv -> inv.getArgument(0));

        TradeDTO dto = tradeService.closeTrade(user1, tradeId, req);

        assertNotNull(dto);
        assertEquals("CLOSED", dto.status());
        assertEquals(new BigDecimal("170.00"), dto.exitPrice());
        assertNotNull(dto.exitDate());
        assertEquals(new BigDecimal("200.00"), dto.realizedPnl());
        verify(statisticsService).recalculate(user1);
    }

    @Test
    void closeTrade_ShortPosition_Success() {
        UUID tradeId = UUID.randomUUID();
        Trade trade = new Trade(journalEntryUser1, "TSLA", PositionType.SHORT, new BigDecimal("200.00"), new BigDecimal("5"));
        trade.setId(tradeId);

        CloseTradeRequest req = new CloseTradeRequest(new BigDecimal("180.00"), null);

        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(inv -> inv.getArgument(0));

        TradeDTO dto = tradeService.closeTrade(user1, tradeId, req);

        assertNotNull(dto);
        assertEquals("CLOSED", dto.status());
        assertEquals(new BigDecimal("100.00"), dto.realizedPnl());
        verify(statisticsService).recalculate(user1);
    }

    @Test
    void closeTrade_ForOtherUser_ThrowsNotFound() {
        UUID tradeId = UUID.randomUUID();
        Trade trade = new Trade(journalEntryUser1, "AAPL", PositionType.LONG, new BigDecimal("150.00"), new BigDecimal("10"));
        trade.setId(tradeId);

        CloseTradeRequest req = new CloseTradeRequest(new BigDecimal("170.00"), null);

        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));

        assertThrows(ResponseStatusException.class, () -> tradeService.closeTrade(user2, tradeId, req));
        verify(statisticsService, never()).recalculate(any());
    }

    @Test
    void deleteTrade_SoftDeletesRow() {
        UUID tradeId = UUID.randomUUID();
        Trade trade = new Trade(journalEntryUser1, "AAPL", PositionType.LONG, new BigDecimal("150.00"), new BigDecimal("10"));
        trade.setId(tradeId);

        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));
        when(tradeRepository.save(any(Trade.class))).thenAnswer(inv -> inv.getArgument(0));

        tradeService.deleteTrade(user1, tradeId);

        assertTrue(trade.isDeleted());
        verify(tradeRepository).save(trade);
        verify(statisticsService).recalculate(user1);
    }
}

package com.tradingjournal.application.journal;

import com.tradingjournal.domain.entity.*;
import com.tradingjournal.infrastructure.repository.JournalEntryRepository;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import com.tradingjournal.presentation.dto.CreateTradeRequest;
import com.tradingjournal.presentation.dto.TradeDTO;
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
                new BigDecimal("10")
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
        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    void createTrade_ForOtherUserJournalEntry_ThrowsNotFound() {
        CreateTradeRequest req = new CreateTradeRequest(
                journalEntryUser1.getId(),
                "AAPL",
                PositionType.LONG,
                new BigDecimal("150.00"),
                new BigDecimal("10")
        );

        when(journalEntryRepository.findById(journalEntryUser1.getId())).thenReturn(Optional.of(journalEntryUser1));

        // user2 attempts to create trade for user1's journal entry
        assertThrows(ResponseStatusException.class, () -> tradeService.createTrade(user2, req));
    }
}

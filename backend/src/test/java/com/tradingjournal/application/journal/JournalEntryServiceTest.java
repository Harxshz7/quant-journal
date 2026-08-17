package com.tradingjournal.application.journal;

import com.tradingjournal.application.account.AccountService;
import com.tradingjournal.domain.entity.JournalEntry;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.JournalEntryRepository;
import com.tradingjournal.presentation.dto.CreateJournalEntryRequest;
import com.tradingjournal.presentation.dto.JournalEntryDTO;
import com.tradingjournal.presentation.dto.UpdateJournalEntryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalEntryServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private JournalEntryService journalEntryService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User("Trader One", "trader1@example.com", "hash1");
        user1.setId(UUID.randomUUID());

        user2 = new User("Trader Two", "trader2@example.com", "hash2");
        user2.setId(UUID.randomUUID());
    }

    @Test
    void createJournalEntry_Success() {
        LocalDate date = LocalDate.now();
        CreateJournalEntryRequest req = new CreateJournalEntryRequest(date, "Great trading session", null, null, null, null, null);

        when(accountService.getDefaultAccount(user1)).thenReturn(null);
        when(journalEntryRepository.findByUserAndEntryDate(user1, date)).thenReturn(Optional.empty());
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(inv -> {
            JournalEntry e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        JournalEntryDTO dto = journalEntryService.createJournalEntry(user1, null, req);

        assertNotNull(dto);
        assertEquals(date, dto.entryDate());
        assertEquals("Great trading session", dto.notes());
        verify(journalEntryRepository).save(any(JournalEntry.class));
    }

    @Test
    void createJournalEntry_DuplicateDate_ThrowsConflict() {
        LocalDate date = LocalDate.now();
        CreateJournalEntryRequest req = new CreateJournalEntryRequest(date, "Duplicate date", null, null, null, null, null);
        JournalEntry existing = new JournalEntry(user1, date, "Existing");

        when(accountService.getDefaultAccount(user1)).thenReturn(null);
        when(journalEntryRepository.findByUserAndEntryDate(user1, date)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () -> journalEntryService.createJournalEntry(user1, null, req));
    }

    @Test
    void getJournalEntryById_OtherUser_ThrowsNotFound() {
        UUID entryId = UUID.randomUUID();
        JournalEntry entryOfUser2 = new JournalEntry(user2, LocalDate.now(), "User 2 notes");
        entryOfUser2.setId(entryId);

        when(journalEntryRepository.findWithTradesById(entryId)).thenReturn(Optional.of(entryOfUser2));

        assertThrows(ResponseStatusException.class, () -> journalEntryService.getJournalEntryById(user1, entryId));
    }

    @Test
    void updateJournalEntry_OtherUser_ThrowsNotFound() {
        UUID entryId = UUID.randomUUID();
        JournalEntry entryOfUser2 = new JournalEntry(user2, LocalDate.now(), "User 2 notes");
        entryOfUser2.setId(entryId);

        when(journalEntryRepository.findById(entryId)).thenReturn(Optional.of(entryOfUser2));
        UpdateJournalEntryRequest req = new UpdateJournalEntryRequest("Attempted update", null, null, null, null, null);

        assertThrows(ResponseStatusException.class, () -> journalEntryService.updateJournalEntry(user1, entryId, req));
    }
}

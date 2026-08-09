package com.tradingjournal.presentation.journal;

import com.tradingjournal.application.journal.JournalEntryService;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.presentation.dto.CreateJournalEntryRequest;
import com.tradingjournal.presentation.dto.JournalEntryDTO;
import com.tradingjournal.presentation.dto.UpdateJournalEntryRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/journal")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    public JournalEntryController(JournalEntryService journalEntryService) {
        this.journalEntryService = journalEntryService;
    }

    @PostMapping
    public ResponseEntity<JournalEntryDTO> createJournalEntry(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateJournalEntryRequest request
    ) {
        JournalEntryDTO created = journalEntryService.createJournalEntry(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<JournalEntryDTO>> getUserJournalEntries(
            @AuthenticationPrincipal User user
    ) {
        List<JournalEntryDTO> entries = journalEntryService.getUserJournalEntries(user);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalEntryDTO> getJournalEntryById(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        JournalEntryDTO entry = journalEntryService.getJournalEntryById(user, id);
        return ResponseEntity.ok(entry);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JournalEntryDTO> updateJournalEntry(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJournalEntryRequest request
    ) {
        JournalEntryDTO updated = journalEntryService.updateJournalEntry(user, id, request);
        return ResponseEntity.ok(updated);
    }
}

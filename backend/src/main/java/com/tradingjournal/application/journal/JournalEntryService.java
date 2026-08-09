package com.tradingjournal.application.journal;

import com.tradingjournal.domain.entity.JournalEntry;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.JournalEntryRepository;
import com.tradingjournal.presentation.dto.CreateJournalEntryRequest;
import com.tradingjournal.presentation.dto.JournalEntryDTO;
import com.tradingjournal.presentation.dto.UpdateJournalEntryRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;

    public JournalEntryService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    public JournalEntryDTO createJournalEntry(User user, CreateJournalEntryRequest request) {
        Optional<JournalEntry> existing = journalEntryRepository.findByUserAndEntryDate(user, request.entryDate());
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Journal entry already exists for date: " + request.entryDate());
        }

        JournalEntry entry = new JournalEntry(user, request.entryDate(), request.notes());
        JournalEntry saved = journalEntryRepository.save(entry);
        return JournalEntryDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<JournalEntryDTO> getUserJournalEntries(User user) {
        return journalEntryRepository.findByUser(user)
                .stream()
                .map(JournalEntryDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public JournalEntryDTO getJournalEntryById(User user, UUID id) {
        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found");
        }

        return JournalEntryDTO.fromEntity(entry);
    }

    public JournalEntryDTO updateJournalEntry(User user, UUID id, UpdateJournalEntryRequest request) {
        JournalEntry entry = journalEntryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found"));

        if (!entry.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found");
        }

        entry.setNotes(request.notes());
        JournalEntry updated = journalEntryRepository.save(entry);
        return JournalEntryDTO.fromEntity(updated);
    }
}

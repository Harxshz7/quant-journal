package com.tradingjournal.infrastructure.repository;

import com.tradingjournal.domain.entity.JournalEntry;
import com.tradingjournal.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {

    List<JournalEntry> findByUser(User user);

    Optional<JournalEntry> findByUserAndEntryDate(User user, LocalDate entryDate);
}

package com.tradingjournal.infrastructure.repository;

import com.tradingjournal.domain.entity.JournalEntry;
import com.tradingjournal.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {

    List<JournalEntry> findByUser(User user);

    /**
     * Loads entries with their trades (and each trade's journal entry) in a single query
     * to avoid N+1 lazy loads when mapping to DTOs.
     */
    @Query("""
            SELECT DISTINCT je
            FROM JournalEntry je
            LEFT JOIN FETCH je.trades t
            LEFT JOIN FETCH t.journalEntry
            WHERE je.user = :user
            ORDER BY je.entryDate DESC
            """)
    List<JournalEntry> findByUserWithTrades(@Param("user") User user);

    /**
     * Same as {@link #findByUserWithTrades(User)} but for a single entry.
     */
    @Query("""
            SELECT DISTINCT je
            FROM JournalEntry je
            LEFT JOIN FETCH je.trades t
            LEFT JOIN FETCH t.journalEntry
            WHERE je.id = :id
            """)
    Optional<JournalEntry> findWithTradesById(@Param("id") UUID id);

    Optional<JournalEntry> findByUserAndEntryDate(User user, LocalDate entryDate);
}

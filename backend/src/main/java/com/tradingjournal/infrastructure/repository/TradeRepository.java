package com.tradingjournal.infrastructure.repository;

import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TradeRepository extends JpaRepository<Trade, UUID>, JpaSpecificationExecutor<Trade> {

    @Query("SELECT t FROM Trade t WHERE t.journalEntry.id = :journalEntryId")
    List<Trade> findByJournalEntry_JournalEntryId(@Param("journalEntryId") UUID journalEntryId);

    @Query("SELECT t FROM Trade t WHERE t.journalEntry.user = :user")
    List<Trade> findByJournalEntry_User(@Param("user") User user);

    @Query("""
            SELECT t
            FROM Trade t
            JOIN FETCH t.journalEntry je
            JOIN FETCH je.user u
            WHERE u = :user
              AND t.deleted = false
              AND t.exitDate IS NOT NULL
            ORDER BY t.exitDate ASC
            """)
    List<Trade> findClosedActiveTradesForStatistics(@Param("user") User user);

    @Query("SELECT COUNT(t) > 0 FROM Trade t WHERE t.journalEntry.user = :user AND t.externalId = :externalId")
    boolean existsByJournalEntry_UserAndExternalId(@Param("user") User user, @Param("externalId") String externalId);

    /**
     * Initializes the lazy journalEntry association for a batch of trades in one query,
     * avoiding N+1 loads when mapping a page of trades to DTOs.
     */
    @Query("SELECT t FROM Trade t JOIN FETCH t.journalEntry WHERE t.id IN :ids")
    List<Trade> findWithJournalEntryByIds(@Param("ids") Collection<UUID> ids);
}

package com.tradingjournal.infrastructure.repository;

import com.tradingjournal.domain.entity.TradeChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TradeChecklistItemRepository extends JpaRepository<TradeChecklistItem, UUID> {
    List<TradeChecklistItem> findByTrade_IdOrderByidAsc(UUID tradeId);

    @Query("SELECT tci FROM TradeChecklistItem tci WHERE tci.trade.id IN :tradeIds")
    List<TradeChecklistItem> findByTradeIdIn(@Param("tradeIds") List<UUID> tradeIds);
}

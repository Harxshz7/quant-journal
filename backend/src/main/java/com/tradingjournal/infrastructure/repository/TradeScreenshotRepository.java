package com.tradingjournal.infrastructure.repository;

import com.tradingjournal.domain.entity.TradeScreenshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TradeScreenshotRepository extends JpaRepository<TradeScreenshot, UUID> {

    @Query("SELECT ts FROM TradeScreenshot ts WHERE ts.trade.id = :tradeId ORDER BY ts.uploadedAt DESC")
    List<TradeScreenshot> findByTrade_TradeId(@Param("tradeId") UUID tradeId);
}

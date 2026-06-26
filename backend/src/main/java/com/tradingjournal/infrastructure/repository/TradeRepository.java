package com.tradingjournal.infrastructure.repository;

import com.tradingjournal.domain.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TradeRepository extends JpaRepository<Trade, UUID> {
}

package com.tradingjournal.infrastructure.repository;

import com.tradingjournal.domain.entity.TradeStatistics;
import com.tradingjournal.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TradeStatisticsRepository extends JpaRepository<TradeStatistics, UUID> {
    Optional<TradeStatistics> findByUser(User user);
}

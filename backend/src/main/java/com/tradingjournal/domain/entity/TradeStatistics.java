package com.tradingjournal.domain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "trade_statistics",
        uniqueConstraints = @UniqueConstraint(name = "uk_trade_statistics_user", columnNames = "user_id")
)
public class TradeStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "total_trades", nullable = false)
    private int totalTrades;

    @Column(name = "win_count", nullable = false)
    private int winCount;

    @Column(name = "loss_count", nullable = false)
    private int lossCount;

    @Column(name = "break_even_count", nullable = false)
    private int breakEvenCount;

    @Column(name = "win_rate", precision = 10, scale = 4)
    private BigDecimal winRate;

    @Column(name = "profit_factor", precision = 10, scale = 4)
    private BigDecimal profitFactor;

    @Column(name = "avg_win", precision = 19, scale = 4)
    private BigDecimal avgWin;

    @Column(name = "avg_loss", precision = 19, scale = 4)
    private BigDecimal avgLoss;

    @Column(name = "largest_win", precision = 19, scale = 4)
    private BigDecimal largestWin;

    @Column(name = "largest_loss", precision = 19, scale = 4)
    private BigDecimal largestLoss;

    @Column(name = "max_consecutive_wins", nullable = false)
    private int maxConsecutiveWins;

    @Column(name = "max_consecutive_losses", nullable = false)
    private int maxConsecutiveLosses;

    @Column(name = "avg_risk_reward", precision = 10, scale = 4)
    private BigDecimal avgRiskReward;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public TradeStatistics() {
    }

    @PrePersist
    @PreUpdate
    protected void onWrite() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getTotalTrades() {
        return totalTrades;
    }

    public void setTotalTrades(int totalTrades) {
        this.totalTrades = totalTrades;
    }

    public int getWinCount() {
        return winCount;
    }

    public void setWinCount(int winCount) {
        this.winCount = winCount;
    }

    public int getLossCount() {
        return lossCount;
    }

    public void setLossCount(int lossCount) {
        this.lossCount = lossCount;
    }

    public int getBreakEvenCount() {
        return breakEvenCount;
    }

    public void setBreakEvenCount(int breakEvenCount) {
        this.breakEvenCount = breakEvenCount;
    }

    public BigDecimal getWinRate() {
        return winRate;
    }

    public void setWinRate(BigDecimal winRate) {
        this.winRate = winRate;
    }

    public BigDecimal getProfitFactor() {
        return profitFactor;
    }

    public void setProfitFactor(BigDecimal profitFactor) {
        this.profitFactor = profitFactor;
    }

    public BigDecimal getAvgWin() {
        return avgWin;
    }

    public void setAvgWin(BigDecimal avgWin) {
        this.avgWin = avgWin;
    }

    public BigDecimal getAvgLoss() {
        return avgLoss;
    }

    public void setAvgLoss(BigDecimal avgLoss) {
        this.avgLoss = avgLoss;
    }

    public BigDecimal getLargestWin() {
        return largestWin;
    }

    public void setLargestWin(BigDecimal largestWin) {
        this.largestWin = largestWin;
    }

    public BigDecimal getLargestLoss() {
        return largestLoss;
    }

    public void setLargestLoss(BigDecimal largestLoss) {
        this.largestLoss = largestLoss;
    }

    public int getMaxConsecutiveWins() {
        return maxConsecutiveWins;
    }

    public void setMaxConsecutiveWins(int maxConsecutiveWins) {
        this.maxConsecutiveWins = maxConsecutiveWins;
    }

    public int getMaxConsecutiveLosses() {
        return maxConsecutiveLosses;
    }

    public void setMaxConsecutiveLosses(int maxConsecutiveLosses) {
        this.maxConsecutiveLosses = maxConsecutiveLosses;
    }

    public BigDecimal getAvgRiskReward() {
        return avgRiskReward;
    }

    public void setAvgRiskReward(BigDecimal avgRiskReward) {
        this.avgRiskReward = avgRiskReward;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

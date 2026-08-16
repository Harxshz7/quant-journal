package com.tradingjournal.application.statistics;

import com.tradingjournal.application.analytics.PnlCalculator;
import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.TradeStatistics;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import com.tradingjournal.infrastructure.repository.TradeStatisticsRepository;
import com.tradingjournal.presentation.dto.StatisticsDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
public class StatisticsService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final TradeRepository tradeRepository;
    private final TradeStatisticsRepository statisticsRepository;

    public StatisticsService(TradeRepository tradeRepository, TradeStatisticsRepository statisticsRepository) {
        this.tradeRepository = tradeRepository;
        this.statisticsRepository = statisticsRepository;
    }

    public TradeStatistics recalculate(User user) {
        List<Trade> trades = tradeRepository.findClosedActiveTradesForStatistics(user);
        return computeAndSave(user, trades);
    }

    @Transactional(readOnly = true)
    public StatisticsDTO getStatistics(User user, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null) {
            return statisticsRepository.findByUser(user)
                    .map(this::toDto)
                    .orElseGet(this::defaultStatistics);
        }
        Instant from = fromDate != null ? fromDate.atStartOfDay(ZoneId.of("UTC")).toInstant() : null;
        Instant to = toDate != null ? toDate.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant() : null;
        List<Trade> trades = tradeRepository.findClosedTradesInRange(user, from, to);
        return computeFromTrades(trades);
    }

    private TradeStatistics computeAndSave(User user, List<Trade> trades) {
        int totalTrades = trades.size();
        int winCount = 0;
        int lossCount = 0;
        int breakEvenCount = 0;

        BigDecimal sumWins = BigDecimal.ZERO;
        BigDecimal sumLosses = BigDecimal.ZERO;
        BigDecimal sumRiskReward = BigDecimal.ZERO;
        int riskRewardCount = 0;

        BigDecimal largestWin = null;
        BigDecimal largestLoss = null;

        int currentWinStreak = 0;
        int currentLossStreak = 0;
        int maxWinStreak = 0;
        int maxLossStreak = 0;

        for (Trade trade : trades) {
            BigDecimal netPnl = PnlCalculator.netPnl(trade);
            if (netPnl == null) continue;

            int compare = netPnl.compareTo(BigDecimal.ZERO);
            if (compare > 0) {
                winCount++;
                sumWins = sumWins.add(netPnl);
                largestWin = largestWin == null || netPnl.compareTo(largestWin) > 0 ? netPnl : largestWin;
                currentWinStreak++;
                currentLossStreak = 0;
                maxWinStreak = Math.max(maxWinStreak, currentWinStreak);
            } else if (compare < 0) {
                lossCount++;
                sumLosses = sumLosses.add(netPnl);
                largestLoss = largestLoss == null || netPnl.compareTo(largestLoss) < 0 ? netPnl : largestLoss;
                currentLossStreak++;
                currentWinStreak = 0;
                maxLossStreak = Math.max(maxLossStreak, currentLossStreak);
            } else {
                breakEvenCount++;
                currentWinStreak = 0;
                currentLossStreak = 0;
            }

            BigDecimal rr = PnlCalculator.riskRewardRatio(trade);
            if (rr != null) {
                sumRiskReward = sumRiskReward.add(rr);
                riskRewardCount++;
            }
        }

        BigDecimal winRate = null;
        int decidedTrades = winCount + lossCount;
        if (decidedTrades > 0) {
            winRate = percent(BigDecimal.valueOf(winCount), BigDecimal.valueOf(decidedTrades));
        }

        BigDecimal profitFactor = null;
        if (lossCount > 0) {
            BigDecimal grossLosses = sumLosses.abs();
            if (grossLosses.compareTo(BigDecimal.ZERO) != 0) {
                profitFactor = scale(sumWins.divide(grossLosses, 4, RoundingMode.HALF_UP));
            }
        }

        BigDecimal avgWin = winCount > 0 ? scale(sumWins.divide(BigDecimal.valueOf(winCount), 4, RoundingMode.HALF_UP)) : null;
        BigDecimal avgLoss = lossCount > 0 ? scale(sumLosses.divide(BigDecimal.valueOf(lossCount), 4, RoundingMode.HALF_UP)) : null;
        BigDecimal avgRiskReward = riskRewardCount > 0
                ? scale(sumRiskReward.divide(BigDecimal.valueOf(riskRewardCount), 4, RoundingMode.HALF_UP))
                : null;

        TradeStatistics stats = statisticsRepository.findByUser(user).orElseGet(TradeStatistics::new);
        stats.setUser(user);
        stats.setTotalTrades(totalTrades);
        stats.setWinCount(winCount);
        stats.setLossCount(lossCount);
        stats.setBreakEvenCount(breakEvenCount);
        stats.setWinRate(winRate == null ? BigDecimal.ZERO : winRate);
        stats.setProfitFactor(profitFactor);
        stats.setAvgWin(avgWin);
        stats.setAvgLoss(avgLoss);
        stats.setLargestWin(largestWin);
        stats.setLargestLoss(largestLoss);
        stats.setMaxConsecutiveWins(maxWinStreak);
        stats.setMaxConsecutiveLosses(maxLossStreak);
        stats.setAvgRiskReward(avgRiskReward);

        return statisticsRepository.save(stats);
    }

    private StatisticsDTO computeFromTrades(List<Trade> trades) {
        int totalTrades = trades.size();
        int winCount = 0;
        int lossCount = 0;
        int breakEvenCount = 0;

        BigDecimal sumWins = BigDecimal.ZERO;
        BigDecimal sumLosses = BigDecimal.ZERO;
        BigDecimal sumRiskReward = BigDecimal.ZERO;
        int riskRewardCount = 0;

        BigDecimal largestWin = null;
        BigDecimal largestLoss = null;

        for (Trade trade : trades) {
            BigDecimal netPnl = PnlCalculator.netPnl(trade);
            if (netPnl == null) continue;

            int compare = netPnl.compareTo(BigDecimal.ZERO);
            if (compare > 0) {
                winCount++;
                sumWins = sumWins.add(netPnl);
                largestWin = largestWin == null || netPnl.compareTo(largestWin) > 0 ? netPnl : largestWin;
            } else if (compare < 0) {
                lossCount++;
                sumLosses = sumLosses.add(netPnl);
                largestLoss = largestLoss == null || netPnl.compareTo(largestLoss) < 0 ? netPnl : largestLoss;
            } else {
                breakEvenCount++;
            }

            BigDecimal rr = PnlCalculator.riskRewardRatio(trade);
            if (rr != null) {
                sumRiskReward = sumRiskReward.add(rr);
                riskRewardCount++;
            }
        }

        BigDecimal winRate = null;
        int decidedTrades = winCount + lossCount;
        if (decidedTrades > 0) {
            winRate = percent(BigDecimal.valueOf(winCount), BigDecimal.valueOf(decidedTrades));
        }

        BigDecimal profitFactor = null;
        if (lossCount > 0) {
            BigDecimal grossLosses = sumLosses.abs();
            if (grossLosses.compareTo(BigDecimal.ZERO) != 0) {
                profitFactor = scale(sumWins.divide(grossLosses, 4, RoundingMode.HALF_UP));
            }
        }

        BigDecimal avgWin = winCount > 0 ? scale(sumWins.divide(BigDecimal.valueOf(winCount), 4, RoundingMode.HALF_UP)) : null;
        BigDecimal avgLoss = lossCount > 0 ? scale(sumLosses.divide(BigDecimal.valueOf(lossCount), 4, RoundingMode.HALF_UP)) : null;
        BigDecimal avgRiskReward = riskRewardCount > 0
                ? scale(sumRiskReward.divide(BigDecimal.valueOf(riskRewardCount), 4, RoundingMode.HALF_UP))
                : null;

        return new StatisticsDTO(
                null,
                totalTrades,
                winCount,
                lossCount,
                breakEvenCount,
                winRate == null ? BigDecimal.ZERO : winRate,
                profitFactor,
                avgWin,
                avgLoss,
                largestWin,
                largestLoss,
                0,
                0,
                avgRiskReward,
                null
        );
    }

    private StatisticsDTO toDto(TradeStatistics stats) {
        return new StatisticsDTO(
                stats.getId(),
                stats.getTotalTrades(),
                stats.getWinCount(),
                stats.getLossCount(),
                stats.getBreakEvenCount(),
                stats.getWinRate(),
                stats.getProfitFactor(),
                stats.getAvgWin(),
                stats.getAvgLoss(),
                stats.getLargestWin(),
                stats.getLargestLoss(),
                stats.getMaxConsecutiveWins(),
                stats.getMaxConsecutiveLosses(),
                stats.getAvgRiskReward(),
                stats.getUpdatedAt()
        );
    }

    private StatisticsDTO defaultStatistics() {
        return new StatisticsDTO(
                null,
                0,
                0,
                0,
                0,
                BigDecimal.ZERO,
                null,
                null,
                null,
                null,
                null,
                0,
                0,
                null,
                null
        );
    }

    private BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
        return numerator
                .multiply(HUNDRED)
                .divide(denominator, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP);
    }
}

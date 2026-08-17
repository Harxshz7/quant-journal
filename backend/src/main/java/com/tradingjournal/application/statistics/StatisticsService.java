package com.tradingjournal.application.statistics;

import com.tradingjournal.application.account.AccountService;
import com.tradingjournal.application.analytics.PnlCalculator;
import com.tradingjournal.domain.entity.Account;
import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.TradeStatistics;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import com.tradingjournal.infrastructure.repository.TradeStatisticsRepository;
import com.tradingjournal.presentation.dto.StatisticsDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class StatisticsService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /** Default fraction of the account risked per trade when no risk setting exists. */
    private static final BigDecimal DEFAULT_RISK_PER_TRADE = new BigDecimal("0.01");

    private final TradeRepository tradeRepository;
    private final TradeStatisticsRepository statisticsRepository;
    private final AccountService accountService;

    public StatisticsService(
            TradeRepository tradeRepository,
            TradeStatisticsRepository statisticsRepository,
            AccountService accountService
    ) {
        this.tradeRepository = tradeRepository;
        this.statisticsRepository = statisticsRepository;
        this.accountService = accountService;
    }

    public TradeStatistics recalculate(User user) {
        List<Trade> trades = tradeRepository.findClosedActiveTradesForStatistics(user, null);
        return computeAndSave(user, trades);
    }

    @Transactional(readOnly = true)
    public StatisticsDTO getStatistics(User user, UUID accountId, LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null && toDate == null && accountId == null) {
            return statisticsRepository.findByUser(user)
                    .map(this::toDto)
                    .orElseGet(this::defaultStatistics);
        }
        Account account = accountId != null ? accountService.resolveOwnedAccount(user, accountId) : null;
        Instant from = fromDate != null ? fromDate.atStartOfDay(ZoneId.of("UTC")).toInstant() : null;
        Instant to = toDate != null ? toDate.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant() : null;
        List<Trade> trades = tradeRepository.findClosedTradesInRange(user, account, from, to);
        return computeFromTrades(user, trades);
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

        BigDecimal expectancy = expectancy(winRate, avgWin, avgLoss);
        BigDecimal riskOfRuin = riskOfRuin(winRate, avgWin, avgLoss);

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
        stats.setLargestWin(scaleIfNotNull(largestWin));
        stats.setLargestLoss(scaleIfNotNull(largestLoss));
        stats.setMaxConsecutiveWins(maxWinStreak);
        stats.setMaxConsecutiveLosses(maxLossStreak);
        stats.setAvgRiskReward(avgRiskReward);
        stats.setExpectancy(expectancy);
        stats.setRiskOfRuin(riskOfRuin);

        return statisticsRepository.save(stats);
    }

    private StatisticsDTO computeFromTrades(User user, List<Trade> trades) {
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
                scaleIfNotNull(largestWin),
                scaleIfNotNull(largestLoss),
                0,
                0,
                avgRiskReward,
                expectancy(winRate, avgWin, avgLoss),
                riskOfRuin(winRate, avgWin, avgLoss),
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
                stats.getExpectancy(),
                stats.getRiskOfRuin(),
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
                null,
                null,
                null
        );
    }

    /**
     * Expectancy (average net P&L per trade) = winRate*avgWin - lossRate*|avgLoss|.
     * avgLoss is stored as a negative value, so (1-p)*avgLoss already subtracts it.
     */
    private BigDecimal expectancy(BigDecimal winRate, BigDecimal avgWin, BigDecimal avgLoss) {
        if (winRate == null || avgWin == null || avgLoss == null) return null;
        BigDecimal p = winRate.divide(HUNDRED, 8, RoundingMode.HALF_UP);
        return p.multiply(avgWin).add(BigDecimal.ONE.subtract(p).multiply(avgLoss))
                .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Risk of ruin under a fixed-fractional model (Ralph Vince formulation):
     *   edge = p*R - (1-p),  R = avgWin/|avgLoss|,  units = 1/f (f = fraction risked, default 1%)
     *   RoR  = ((1-edge)/(1+edge))^units, or 1 (certain ruin) when there is no edge.
     * An edge >= 1 (average profit per trade >= the amount risked) drives ruin to 0.
     */
    private BigDecimal riskOfRuin(BigDecimal winRate, BigDecimal avgWin, BigDecimal avgLoss) {
        if (winRate == null || avgWin == null || avgLoss == null || avgLoss.compareTo(BigDecimal.ZERO) == 0) return null;

        BigDecimal p = winRate.divide(HUNDRED, 8, RoundingMode.HALF_UP);
        BigDecimal lossAmount = avgLoss.abs();
        BigDecimal rr = avgWin.divide(lossAmount, 8, RoundingMode.HALF_UP);
        BigDecimal edge = p.multiply(rr).subtract(BigDecimal.ONE.subtract(p));

        if (edge.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("1.00000000");
        }
        if (edge.compareTo(BigDecimal.ONE) >= 0) {
            return new BigDecimal("0.00000000");
        }

        BigDecimal units = BigDecimal.ONE.divide(DEFAULT_RISK_PER_TRADE, 4, RoundingMode.HALF_UP);
        BigDecimal base = BigDecimal.ONE.subtract(edge).divide(BigDecimal.ONE.add(edge), 12, RoundingMode.HALF_UP);
        if (base.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("0.00000000");
        }
        BigDecimal logBase = new BigDecimal(Math.log(base.doubleValue()), MathContext.DECIMAL64);
        BigDecimal result = new BigDecimal(Math.exp(logBase.multiply(units).doubleValue()), MathContext.DECIMAL64);
        return result.setScale(8, RoundingMode.HALF_UP);
    }

    private BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
        return numerator
                .multiply(HUNDRED)
                .divide(denominator, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleIfNotNull(BigDecimal value) {
        return value == null ? null : scale(value);
    }
}
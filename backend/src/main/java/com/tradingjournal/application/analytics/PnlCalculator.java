package com.tradingjournal.application.analytics;

import com.tradingjournal.domain.entity.PositionType;
import com.tradingjournal.domain.entity.Trade;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PnlCalculator {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private PnlCalculator() {}

    public static BigDecimal grossPnl(Trade trade) {
        if (trade.getExitPrice() == null || trade.getEntryPrice() == null || trade.getQuantity() == null) {
            return null;
        }
        if (trade.getPositionType() == PositionType.LONG) {
            return trade.getExitPrice().subtract(trade.getEntryPrice()).multiply(trade.getQuantity());
        } else {
            return trade.getEntryPrice().subtract(trade.getExitPrice()).multiply(trade.getQuantity());
        }
    }

    public static BigDecimal netPnl(Trade trade) {
        BigDecimal gross = grossPnl(trade);
        if (gross == null) return null;
        BigDecimal fees = trade.getFees() != null ? trade.getFees() : BigDecimal.ZERO;
        return gross.subtract(fees);
    }

    public static String outcome(Trade trade) {
        BigDecimal net = netPnl(trade);
        if (net == null) return null;
        int cmp = net.compareTo(BigDecimal.ZERO);
        if (cmp > 0) return "WIN";
        if (cmp < 0) return "LOSS";
        return "BREAKEVEN";
    }

    public static BigDecimal pnlPercent(Trade trade) {
        BigDecimal net = netPnl(trade);
        if (net == null) return null;
        BigDecimal denominator = trade.getEntryPrice().multiply(trade.getQuantity());
        if (denominator.compareTo(BigDecimal.ZERO) == 0) return null;
        return net.multiply(HUNDRED).divide(denominator, 4, RoundingMode.HALF_UP);
    }

    public static BigDecimal riskRewardRatio(Trade trade) {
        if (trade.getStopLoss() == null || trade.getExitPrice() == null || trade.getEntryPrice() == null) {
            return null;
        }
        BigDecimal risk = trade.getEntryPrice().subtract(trade.getStopLoss()).abs();
        if (risk.compareTo(BigDecimal.ZERO) == 0) return null;
        return trade.getExitPrice().subtract(trade.getEntryPrice()).abs()
                .divide(risk, 4, RoundingMode.HALF_UP);
    }
}

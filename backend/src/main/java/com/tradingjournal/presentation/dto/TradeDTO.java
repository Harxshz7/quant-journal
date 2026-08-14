package com.tradingjournal.presentation.dto;

import com.tradingjournal.domain.entity.PositionType;
import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.TradeSource;
import com.tradingjournal.domain.entity.TradeOutcomeFilter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TradeDTO(
    UUID tradeId,
    UUID journalEntryId,
    LocalDate entryDate,
    String ticker,
    PositionType positionType,
    TradeSource source,
    BigDecimal entryPrice,
    BigDecimal quantity,
    BigDecimal stopLoss,
    String strategy,
    BigDecimal exitPrice,
    Instant exitDate,
    String status,
    BigDecimal grossPnl,
    BigDecimal netPnl,
    BigDecimal pnlPercent,
    BigDecimal riskRewardRatio,
    TradeOutcomeFilter outcome,
    BigDecimal realizedPnl,
    BigDecimal fees,
    boolean deleted,
    Instant createdAt,
    Instant updatedAt
) {
    public static TradeDTO fromEntity(Trade trade) {
        BigDecimal exitPrice = trade.getExitPrice();
        Instant exitDate = trade.getExitDate();
        String status = exitPrice == null ? "OPEN" : "CLOSED";
        BigDecimal grossPnl = null;
        BigDecimal netPnl = null;
        BigDecimal pnlPercent = null;
        BigDecimal riskRewardRatio = null;
        TradeOutcomeFilter outcome = null;

        BigDecimal fees = trade.getFees() != null ? trade.getFees() : BigDecimal.ZERO;

        if (exitPrice != null && trade.getEntryPrice() != null && trade.getQuantity() != null) {
            if (trade.getPositionType() == PositionType.LONG) {
                grossPnl = exitPrice.subtract(trade.getEntryPrice()).multiply(trade.getQuantity());
            } else if (trade.getPositionType() == PositionType.SHORT) {
                grossPnl = trade.getEntryPrice().subtract(exitPrice).multiply(trade.getQuantity());
            }

            if (grossPnl != null) {
                netPnl = grossPnl.subtract(fees);

                BigDecimal denominator = trade.getEntryPrice().multiply(trade.getQuantity());
                if (denominator.compareTo(BigDecimal.ZERO) != 0) {
                    pnlPercent = netPnl
                            .multiply(new BigDecimal("100"))
                            .divide(denominator, 4, RoundingMode.HALF_UP);
                }

                if (netPnl.compareTo(BigDecimal.ZERO) > 0) {
                    outcome = TradeOutcomeFilter.WIN;
                } else if (netPnl.compareTo(BigDecimal.ZERO) < 0) {
                    outcome = TradeOutcomeFilter.LOSS;
                } else {
                    outcome = TradeOutcomeFilter.BREAKEVEN;
                }
            }
        }

        if (trade.getStopLoss() != null && exitPrice != null && trade.getEntryPrice() != null) {
            BigDecimal risk = trade.getEntryPrice().subtract(trade.getStopLoss()).abs();
            if (risk.compareTo(BigDecimal.ZERO) != 0) {
                riskRewardRatio = exitPrice.subtract(trade.getEntryPrice()).abs()
                        .divide(risk, 4, RoundingMode.HALF_UP);
            }
        }

        return new TradeDTO(
            trade.getId(),
            trade.getJournalEntry().getId(),
            trade.getJournalEntry().getEntryDate(),
            trade.getTicker(),
            trade.getPositionType(),
            trade.getSource(),
            trade.getEntryPrice(),
            trade.getQuantity(),
            trade.getStopLoss(),
            trade.getStrategy(),
            exitPrice,
            exitDate,
            status,
            grossPnl,
            netPnl,
            pnlPercent,
            riskRewardRatio,
            outcome,
            netPnl,
            fees,
            trade.isDeleted(),
            trade.getCreatedAt(),
            trade.getUpdatedAt()
        );
    }
}

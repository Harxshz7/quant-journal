package com.tradingjournal.presentation.dto;

import com.tradingjournal.application.analytics.PnlCalculator;
import com.tradingjournal.domain.entity.PositionType;
import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.TradeSource;
import com.tradingjournal.domain.entity.TradeOutcomeFilter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
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
        Instant updatedAt,
        List<TradeScreenshotDTO> screenshots) {
    public static TradeDTO fromEntity(Trade trade) {
        return fromEntity(trade, Collections.emptyList());
    }

    public static TradeDTO fromEntity(Trade trade, List<TradeScreenshotDTO> screenshots) {
        BigDecimal exitPrice = trade.getExitPrice();
        String status = exitPrice == null ? "OPEN" : "CLOSED";

        BigDecimal grossPnl = PnlCalculator.grossPnl(trade);
        BigDecimal netPnl = PnlCalculator.netPnl(trade);
        BigDecimal pnlPercent = PnlCalculator.pnlPercent(trade);
        BigDecimal riskRewardRatio = PnlCalculator.riskRewardRatio(trade);

        TradeOutcomeFilter outcome = null;
        String outcomeStr = PnlCalculator.outcome(trade);
        if (outcomeStr != null) {
            outcome = TradeOutcomeFilter.valueOf(outcomeStr);
        }

        BigDecimal fees = trade.getFees() != null ? trade.getFees() : BigDecimal.ZERO;

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
                trade.getExitDate(),
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
                trade.getUpdatedAt(),
                screenshots != null ? screenshots : Collections.emptyList());
    }
}

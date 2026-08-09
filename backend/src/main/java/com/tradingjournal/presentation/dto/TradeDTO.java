package com.tradingjournal.presentation.dto;

import com.tradingjournal.domain.entity.PositionType;
import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.TradeSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TradeDTO(
    UUID tradeId,
    UUID journalEntryId,
    String ticker,
    PositionType positionType,
    TradeSource source,
    BigDecimal entryPrice,
    BigDecimal quantity,
    BigDecimal exitPrice,
    Instant exitDate,
    String status,
    BigDecimal realizedPnl,
    Instant createdAt,
    Instant updatedAt
) {
    public static TradeDTO fromEntity(Trade trade) {
        BigDecimal exitPrice = trade.getExitPrice();
        Instant exitDate = trade.getExitDate();
        String status = exitPrice == null ? "OPEN" : "CLOSED";
        BigDecimal realizedPnl = null;

        if (exitPrice != null && trade.getEntryPrice() != null && trade.getQuantity() != null) {
            if (trade.getPositionType() == PositionType.LONG) {
                realizedPnl = exitPrice.subtract(trade.getEntryPrice()).multiply(trade.getQuantity());
            } else if (trade.getPositionType() == PositionType.SHORT) {
                realizedPnl = trade.getEntryPrice().subtract(exitPrice).multiply(trade.getQuantity());
            }
        }

        return new TradeDTO(
            trade.getId(),
            trade.getJournalEntry().getId(),
            trade.getTicker(),
            trade.getPositionType(),
            trade.getSource(),
            trade.getEntryPrice(),
            trade.getQuantity(),
            exitPrice,
            exitDate,
            status,
            realizedPnl,
            trade.getCreatedAt(),
            trade.getUpdatedAt()
        );
    }
}

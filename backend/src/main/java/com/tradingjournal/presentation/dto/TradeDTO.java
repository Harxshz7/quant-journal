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
    Instant createdAt,
    Instant updatedAt
) {
    public static TradeDTO fromEntity(Trade trade) {
        return new TradeDTO(
            trade.getId(),
            trade.getJournalEntry().getId(),
            trade.getTicker(),
            trade.getPositionType(),
            trade.getSource(),
            trade.getEntryPrice(),
            trade.getQuantity(),
            trade.getCreatedAt(),
            trade.getUpdatedAt()
        );
    }
}

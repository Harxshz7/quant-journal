package com.tradingjournal.presentation.trade;

import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.PositionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TradeDTO {
    private UUID tradeId;
    private String ticker;
    private PositionType positionType;
    private BigDecimal entryPrice;
    private BigDecimal quantity;
    private Instant createdAt;
    private Instant updatedAt;

    public TradeDTO() {
    }

    public TradeDTO(UUID tradeId, String ticker, PositionType positionType, BigDecimal entryPrice, BigDecimal quantity,
            Instant createdAt, Instant updatedAt) {
        this.tradeId = tradeId;
        this.ticker = ticker;
        this.positionType = positionType;
        this.entryPrice = entryPrice;
        this.quantity = quantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TradeDTO fromEntity(Trade trade) {
        return new TradeDTO(
                trade.getTradeId(),
                trade.getTicker(),
                trade.getPositionType(),
                trade.getEntryPrice(),
                trade.getQuantity(),
                trade.getCreatedAt(),
                trade.getUpdatedAt());
    }

    public UUID getTradeId() {
        return tradeId;
    }

    public void setTradeId(UUID tradeId) {
        this.tradeId = tradeId;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public PositionType getPositionType() {
        return positionType;
    }

    public void setPositionType(PositionType positionType) {
        this.positionType = positionType;
    }

    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(BigDecimal entryPrice) {
        this.entryPrice = entryPrice;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

package com.tradingjournal.presentation.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record TradingViewWebhookPayload(
        @JsonProperty("ticker") String ticker,
        @JsonProperty("action") String action,
        @JsonProperty("price") String price,
        @JsonProperty("quantity") String quantity,
        @JsonProperty("strategy") String strategy,
        @JsonProperty("time") String time
) {
    @JsonCreator
    public TradingViewWebhookPayload {}

    public BigDecimal parsedPrice() {
        if (price == null || price.isBlank()) return null;
        try {
            return new BigDecimal(price.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public BigDecimal parsedQuantity() {
        if (quantity == null || quantity.isBlank()) return BigDecimal.ONE;
        try {
            BigDecimal q = new BigDecimal(quantity.trim());
            return q.compareTo(BigDecimal.ZERO) > 0 ? q : BigDecimal.ONE;
        } catch (NumberFormatException e) {
            return BigDecimal.ONE;
        }
    }
}

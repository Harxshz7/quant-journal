package com.tradingjournal.presentation.dto;

import com.tradingjournal.domain.entity.TradeChecklistItem;
import java.util.UUID;

public record TradeChecklistItemDTO(
    UUID id,
    String text,
    boolean checked
) {
    public static TradeChecklistItemDTO fromEntity(TradeChecklistItem item) {
        return new TradeChecklistItemDTO(item.getId(), item.getText(), item.isChecked());
    }
}

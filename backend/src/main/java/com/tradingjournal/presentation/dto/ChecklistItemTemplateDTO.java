package com.tradingjournal.presentation.dto;

import com.tradingjournal.domain.entity.ChecklistItemTemplate;
import java.time.Instant;
import java.util.UUID;

public record ChecklistItemTemplateDTO(
    UUID id,
    String text,
    int sortOrder,
    boolean active,
    Instant createdAt
) {
    public static ChecklistItemTemplateDTO fromEntity(ChecklistItemTemplate entity) {
        return new ChecklistItemTemplateDTO(
            entity.getId(),
            entity.getText(),
            entity.getSortOrder(),
            entity.isActive(),
            entity.getCreatedAt()
        );
    }
}

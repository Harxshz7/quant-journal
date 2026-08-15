package com.tradingjournal.presentation.dto;

import java.util.List;

public record ImportSummaryDTO(
        int totalRows,
        int imported,
        int duplicatesSkipped,
        List<RowErrorDTO> errors,
        List<String> unmappedHeaders) {
}

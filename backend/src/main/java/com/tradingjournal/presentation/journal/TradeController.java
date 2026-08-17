package com.tradingjournal.presentation.journal;

import com.tradingjournal.application.journal.TradeService;
import com.tradingjournal.domain.entity.TradeOutcomeFilter;
import com.tradingjournal.domain.entity.TradeStatusFilter;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.presentation.dto.CloseTradeRequest;
import com.tradingjournal.presentation.dto.CreateTradeRequest;
import com.tradingjournal.presentation.dto.TradeDTO;
import com.tradingjournal.presentation.dto.UpdateTradeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.tradingjournal.application.journal.TradeImportService;
import com.tradingjournal.presentation.dto.ImportSummaryDTO;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trades")
public class TradeController {

    private final TradeService tradeService;
    private final TradeImportService tradeImportService;

    public TradeController(TradeService tradeService, TradeImportService tradeImportService) {
        this.tradeService = tradeService;
        this.tradeImportService = tradeImportService;
    }

    @PostMapping
    public ResponseEntity<TradeDTO> createTrade(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateTradeRequest request
    ) {
        TradeDTO created = tradeService.createTrade(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(path = "/import/tradingview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportSummaryDTO> importTradingViewCsv(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "journalEntryId", required = false) UUID journalEntryId
    ) {
        ImportSummaryDTO summary = tradeImportService.importTradingViewCsv(user, file, journalEntryId);
        return ResponseEntity.ok(summary);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TradeDTO> updateTrade(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTradeRequest request
    ) {
        TradeDTO updated = tradeService.updateTrade(user, id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{tradeId}/checklist/{itemId}")
    public ResponseEntity<TradeDTO> toggleChecklistItem(
            @AuthenticationPrincipal User user,
            @PathVariable UUID tradeId,
            @PathVariable UUID itemId,
            @RequestBody Map<String, Boolean> body
    ) {
        boolean checked = body.getOrDefault("checked", false);
        TradeDTO updated = tradeService.toggleChecklistItem(user, tradeId, itemId, checked);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<TradeDTO> closeTrade(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody CloseTradeRequest request
    ) {
        TradeDTO closed = tradeService.closeTrade(user, id, request);
        return ResponseEntity.ok(closed);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrade(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        tradeService.deleteTrade(user, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<TradeDTO>> getTrades(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) String ticker,
            @RequestParam(required = false) String strategy,
            @RequestParam(required = false) TradeStatusFilter status,
            @RequestParam(required = false) TradeOutcomeFilter outcome,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "entryDate,desc") String sort
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(safePage, safeSize, parseSort(sort));
        Page<TradeDTO> trades = tradeService.getTrades(
                user, accountId, ticker, strategy, status, outcome, fromDate, toDate, includeArchived, pageable
        );
        return ResponseEntity.ok(trades);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "journalEntry.entryDate");
        }
        String[] parts = sort.split(",", 2);
        String property = mapSortProperty(parts[0].trim());
        Sort.Direction direction = parts.length > 1
                ? Sort.Direction.fromString(parts[1].trim())
                : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    private String mapSortProperty(String property) {
        return switch (property) {
            case "entryDate" -> "journalEntry.entryDate";
            case "ticker" -> "ticker";
            case "strategy" -> "strategy";
            case "entryPrice" -> "entryPrice";
            case "quantity" -> "quantity";
            case "exitPrice" -> "exitPrice";
            case "fees" -> "fees";
            case "deleted" -> "deleted";
            default -> "journalEntry.entryDate";
        };
    }
}

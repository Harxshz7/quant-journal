package com.tradingjournal.presentation.journal;

import com.tradingjournal.application.journal.TradeService;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.presentation.dto.CreateTradeRequest;
import com.tradingjournal.presentation.dto.TradeDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    public ResponseEntity<TradeDTO> createTrade(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateTradeRequest request
    ) {
        TradeDTO created = tradeService.createTrade(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<TradeDTO>> getTrades(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) UUID journalEntryId
    ) {
        List<TradeDTO> trades = tradeService.getTrades(user, journalEntryId);
        return ResponseEntity.ok(trades);
    }
}

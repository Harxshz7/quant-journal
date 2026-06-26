package com.tradingjournal.presentation.trade;

import com.tradingjournal.application.trade.TradeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    public ResponseEntity<TradeDTO> createTrade(@Valid @RequestBody CreateTradeRequest request) {
        TradeDTO trade = tradeService.createTrade(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(trade);
    }

    @GetMapping
    public ResponseEntity<List<TradeDTO>> getAllTrades() {
        return ResponseEntity.ok(tradeService.getAllTrades());
    }
}

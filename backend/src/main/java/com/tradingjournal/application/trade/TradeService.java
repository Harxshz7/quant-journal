package com.tradingjournal.application.trade;

import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import com.tradingjournal.presentation.trade.CreateTradeRequest;
import com.tradingjournal.presentation.trade.TradeDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public TradeDTO createTrade(CreateTradeRequest request) {
        Trade trade = new Trade();
        trade.setTicker(request.getTicker());
        trade.setPositionType(request.getPositionType());
        trade.setEntryPrice(request.getEntryPrice());
        trade.setQuantity(request.getQuantity());
        Trade saved = tradeRepository.save(trade);
        return TradeDTO.fromEntity(saved);
    }

    public List<TradeDTO> getAllTrades() {
        return tradeRepository.findAll().stream()
                .map(TradeDTO::fromEntity)
                .collect(Collectors.toList());
    }
}

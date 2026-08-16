package com.tradingjournal.application.export;

import com.tradingjournal.application.analytics.PnlCalculator;
import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.List;

/**
 * Exports a user's trades (all statuses) as a CSV document, one row per trade.
 */
@Service
@Transactional(readOnly = true)
public class CsvExportService {

    private static final int MAX_NOTES_LENGTH = 500;

    private final TradeRepository tradeRepository;

    public CsvExportService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public void exportTrades(User user, Writer writer) throws IOException {
        List<Trade> trades = tradeRepository.findByJournalEntry_User(user).stream()
                .filter(trade -> !trade.isDeleted())
                .toList();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(
                        "entryDate",
                        "journalNotes",
                        "ticker",
                        "positionType",
                        "strategy",
                        "entryPrice",
                        "exitPrice",
                        "quantity",
                        "fees",
                        "status",
                        "netPnl",
                        "pnlPercent",
                        "outcome",
                        "mistakeTags",
                        "setupQuality"
                )
                .build();

        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (Trade trade : trades) {
                BigDecimal netPnl = PnlCalculator.netPnl(trade);
                BigDecimal pnlPercent = PnlCalculator.pnlPercent(trade);
                String outcome = PnlCalculator.outcome(trade);

                printer.printRecord(
                        trade.getJournalEntry().getEntryDate(),
                        truncate(trade.getJournalEntry().getNotes()),
                        trade.getTicker(),
                        trade.getPositionType(),
                        trade.getStrategy(),
                        trade.getEntryPrice(),
                        trade.getExitPrice(),
                        trade.getQuantity(),
                        trade.getFees(),
                        trade.getExitPrice() == null ? "OPEN" : "CLOSED",
                        netPnl,
                        pnlPercent,
                        outcome,
                        String.join(";", trade.getMistakeTags().stream().map(Enum::name).toList()),
                        trade.getSetupQuality()
                );
            }
        }
    }

    private String truncate(String value) {
        if (value == null || value.isBlank()) return "";
        String trimmed = value.trim().replaceAll("\\s+", " ");
        return trimmed.length() <= MAX_NOTES_LENGTH ? trimmed : trimmed.substring(0, MAX_NOTES_LENGTH);
    }
}

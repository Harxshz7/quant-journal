package com.tradingjournal.application.journal;

import com.tradingjournal.application.statistics.StatisticsService;
import com.tradingjournal.domain.entity.JournalEntry;
import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.TradeSource;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.JournalEntryRepository;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import com.tradingjournal.presentation.dto.ImportSummaryDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class TradeImportService {

    private final TradeRepository tradeRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final StatisticsService statisticsService;

    public TradeImportService(
            TradeRepository tradeRepository,
            JournalEntryRepository journalEntryRepository,
            StatisticsService statisticsService
    ) {
        this.tradeRepository = tradeRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.statisticsService = statisticsService;
    }

    public ImportSummaryDTO importTradingViewCsv(User user, MultipartFile file, UUID explicitJournalEntryId) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required and cannot be empty");
        }

        JournalEntry explicitJournalEntry = null;
        if (explicitJournalEntryId != null) {
            explicitJournalEntry = journalEntryRepository.findById(explicitJournalEntryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found"));

            if (!explicitJournalEntry.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal entry not found");
            }
        }

        TradingViewCsvParser parser = new TradingViewCsvParser();
        TradingViewCsvParser.ParseResult parseResult;

        try (InputStream inputStream = file.getInputStream()) {
            parseResult = parser.parse(inputStream);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to parse CSV file: " + e.getMessage());
        }

        int importedCount = 0;
        int duplicatesSkipped = 0;
        Map<LocalDate, JournalEntry> autoEntriesCache = new HashMap<>();

        for (TradingViewCsvParser.ParsedRow row : parseResult.validRows) {
            // Check for duplicates
            if (row.externalId != null && tradeRepository.existsByJournalEntry_UserAndExternalId(user, row.externalId)) {
                duplicatesSkipped++;
                continue;
            }

            // Resolve journal entry
            JournalEntry targetEntry = explicitJournalEntry;
            if (targetEntry == null) {
                LocalDate entryDate = row.entryDate != null ? row.entryDate : LocalDate.now();
                targetEntry = autoEntriesCache.computeIfAbsent(entryDate, date ->
                        journalEntryRepository.findByUserAndEntryDate(user, date)
                                .orElseGet(() -> {
                                    JournalEntry newEntry = new JournalEntry(user, date, "Imported from TradingView");
                                    return journalEntryRepository.save(newEntry);
                                })
                );
            }

            // Create trade entity
            Trade trade = new Trade(
                    targetEntry,
                    row.ticker,
                    row.positionType,
                    row.entryPrice,
                    row.quantity
            );

            trade.setStopLoss(row.stopLoss);
            trade.setStrategy(row.strategy);
            trade.setExitPrice(row.exitPrice);
            trade.setExitDate(row.exitDate);
            trade.setFees(row.fees != null ? row.fees : java.math.BigDecimal.ZERO);
            trade.setSource(TradeSource.IMPORTED);
            trade.setExternalId(row.externalId);

            tradeRepository.save(trade);
            importedCount++;
        }

        if (importedCount > 0) {
            statisticsService.recalculate(user);
        }

        return new ImportSummaryDTO(
                parseResult.totalRows,
                importedCount,
                duplicatesSkipped,
                parseResult.errors,
                parseResult.unmappedHeaders
        );
    }
}

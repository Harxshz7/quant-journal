package com.tradingjournal.application.journal;

import com.tradingjournal.domain.entity.PositionType;
import com.tradingjournal.presentation.dto.RowErrorDTO;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class TradingViewCsvParser {

    public static class ParsedRow {
        public final int rowNum;
        public final String ticker;
        public final PositionType positionType;
        public final BigDecimal entryPrice;
        public final BigDecimal quantity;
        public final BigDecimal stopLoss;
        public final String strategy;
        public final BigDecimal exitPrice;
        public final Instant exitDate;
        public final BigDecimal fees;
        public final String externalId;
        public final LocalDate entryDate;

        public ParsedRow(int rowNum, String ticker, PositionType positionType, BigDecimal entryPrice,
                         BigDecimal quantity, BigDecimal stopLoss, String strategy, BigDecimal exitPrice,
                         Instant exitDate, BigDecimal fees, String externalId, LocalDate entryDate) {
            this.rowNum = rowNum;
            this.ticker = ticker;
            this.positionType = positionType;
            this.entryPrice = entryPrice;
            this.quantity = quantity;
            this.stopLoss = stopLoss;
            this.strategy = strategy;
            this.exitPrice = exitPrice;
            this.exitDate = exitDate;
            this.fees = fees;
            this.externalId = externalId;
            this.entryDate = entryDate;
        }
    }

    public static class ParseResult {
        public final int totalRows;
        public final List<ParsedRow> validRows;
        public final List<RowErrorDTO> errors;
        public final List<String> unmappedHeaders;

        public ParseResult(int totalRows, List<ParsedRow> validRows, List<RowErrorDTO> errors, List<String> unmappedHeaders) {
            this.totalRows = totalRows;
            this.validRows = validRows;
            this.errors = errors;
            this.unmappedHeaders = unmappedHeaders;
        }
    }

    private static final List<String> ALIAS_TICKER = List.of("symbol", "ticker", "instrument", "contract", "pair", "symbol/ticker");
    private static final List<String> ALIAS_SIDE = List.of("side", "type", "action", "direction", "buy/sell", "order type", "type/side");
    private static final List<String> ALIAS_QTY = List.of("qty", "quantity", "size", "contracts", "amount", "filled qty", "units", "filled quantity");
    private static final List<String> ALIAS_ENTRY_PRICE = List.of("entry price", "open price", "buy price", "fill price", "price", "avg price", "execution price");
    private static final List<String> ALIAS_EXIT_PRICE = List.of("exit price", "close price", "sell price");
    private static final List<String> ALIAS_ENTRY_TIME = List.of("entry time", "open time", "time", "date", "fill time", "order time", "date/time", "created time", "open date");
    private static final List<String> ALIAS_EXIT_TIME = List.of("exit time", "close time", "closed", "close date");
    private static final List<String> ALIAS_COMMISSION = List.of("commission", "fee", "fees", "total fee", "commission (usd)", "fee (usd)", "comm");
    private static final List<String> ALIAS_EXTERNAL_ID = List.of("order id", "trade id", "id", "order no", "fill id", "trade #", "order #", "external id");
    private static final List<String> ALIAS_PNL = List.of("p&l", "profit", "net p&l", "gross p&l", "profit/loss", "pnl", "realized pnl");

    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy")
    };

    public ParseResult parse(InputStream inputStream) {
        List<ParsedRow> validRows = new ArrayList<>();
        List<RowErrorDTO> errors = new ArrayList<>();
        List<String> unmappedHeaders = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                errors.add(new RowErrorDTO(1, "CSV file is empty"));
                return new ParseResult(0, validRows, errors, unmappedHeaders);
            }

            // Remove UTF-8 BOM if present
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }

            List<String> headers = parseCsvLine(headerLine);
            Map<String, Integer> colIndexMap = new HashMap<>();

            for (int i = 0; i < headers.size(); i++) {
                String h = headers.get(i).trim().toLowerCase();
                if (h.isEmpty()) continue;

                String mappedKey = findMatchedKey(h);
                if (mappedKey != null) {
                    colIndexMap.putIfAbsent(mappedKey, i);
                } else {
                    unmappedHeaders.add(headers.get(i).trim());
                }
            }

            // Verify required columns present
            if (!colIndexMap.containsKey("TICKER")) {
                errors.add(new RowErrorDTO(1, "Missing required column for Ticker (e.g. Symbol, Ticker)"));
            }
            if (!colIndexMap.containsKey("SIDE")) {
                errors.add(new RowErrorDTO(1, "Missing required column for Side (e.g. Side, Action, Buy/Sell)"));
            }
            if (!colIndexMap.containsKey("QTY")) {
                errors.add(new RowErrorDTO(1, "Missing required column for Quantity (e.g. Qty, Quantity)"));
            }
            if (!colIndexMap.containsKey("ENTRY_PRICE")) {
                errors.add(new RowErrorDTO(1, "Missing required column for Price (e.g. Price, Entry Price, Fill Price)"));
            }

            if (!errors.isEmpty()) {
                return new ParseResult(0, validRows, errors, unmappedHeaders);
            }

            String line;
            int rowNum = 1; // line 1 was header
            int dataRowCount = 0;

            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (line.isBlank()) continue;

                dataRowCount++;
                List<String> fields = parseCsvLine(line);

                try {
                    ParsedRow parsed = parseRow(rowNum, fields, colIndexMap, errors);
                    if (parsed != null) {
                        validRows.add(parsed);
                    }
                } catch (Exception e) {
                    errors.add(new RowErrorDTO(rowNum, "Malformed row: " + e.getMessage()));
                }
            }

            return new ParseResult(dataRowCount, validRows, errors, unmappedHeaders);

        } catch (Exception e) {
            errors.add(new RowErrorDTO(0, "Failed to read CSV file: " + e.getMessage()));
            return new ParseResult(0, validRows, errors, unmappedHeaders);
        }
    }

    private ParsedRow parseRow(int rowNum, List<String> fields, Map<String, Integer> colMap, List<RowErrorDTO> errors) {
        String ticker = getFieldValue(fields, colMap.get("TICKER"));
        if (ticker == null || ticker.isBlank()) {
            errors.add(new RowErrorDTO(rowNum, "Missing ticker/symbol"));
            return null;
        }

        String rawSide = getFieldValue(fields, colMap.get("SIDE"));
        PositionType positionType = parsePositionType(rawSide);
        if (positionType == null) {
            errors.add(new RowErrorDTO(rowNum, "Invalid side/position type: '" + rawSide + "'"));
            return null;
        }

        BigDecimal qty = parseBigDecimal(getFieldValue(fields, colMap.get("QTY")));
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(new RowErrorDTO(rowNum, "Invalid quantity: '" + getFieldValue(fields, colMap.get("QTY")) + "'"));
            return null;
        }

        BigDecimal entryPrice = parseBigDecimal(getFieldValue(fields, colMap.get("ENTRY_PRICE")));
        if (entryPrice == null || entryPrice.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(new RowErrorDTO(rowNum, "Invalid entry price: '" + getFieldValue(fields, colMap.get("ENTRY_PRICE")) + "'"));
            return null;
        }

        BigDecimal fees = parseBigDecimal(getFieldValue(fields, colMap.get("COMMISSION")));
        if (fees == null || fees.compareTo(BigDecimal.ZERO) < 0) {
            fees = BigDecimal.ZERO;
        }

        String rawEntryTime = getFieldValue(fields, colMap.get("ENTRY_TIME"));
        LocalDate entryDate = parseLocalDate(rawEntryTime);
        Instant entryInstant = parseInstant(rawEntryTime);
        if (entryDate == null) {
            entryDate = LocalDate.now();
        }

        BigDecimal exitPrice = parseBigDecimal(getFieldValue(fields, colMap.get("EXIT_PRICE")));
        Instant exitDate = null;
        if (exitPrice != null && exitPrice.compareTo(BigDecimal.ZERO) > 0) {
            String rawExitTime = getFieldValue(fields, colMap.get("EXIT_TIME"));
            exitDate = parseInstant(rawExitTime);
            if (exitDate == null) {
                exitDate = entryInstant != null ? entryInstant : Instant.now();
            }

            // Check P&L discrepancy if CSV includes PNL column
            BigDecimal csvPnl = parseBigDecimal(getFieldValue(fields, colMap.get("PNL")));
            if (csvPnl != null) {
                BigDecimal derivedGrossPnl = positionType == PositionType.LONG
                        ? exitPrice.subtract(entryPrice).multiply(qty)
                        : entryPrice.subtract(exitPrice).multiply(qty);
                BigDecimal derivedNetPnl = derivedGrossPnl.subtract(fees);

                if (derivedNetPnl.subtract(csvPnl).abs().compareTo(new BigDecimal("0.05")) > 0) {
                    errors.add(new RowErrorDTO(rowNum, "Warning: CSV P&L (" + csvPnl + ") differs from derived P&L (" + derivedNetPnl.setScale(2, RoundingMode.HALF_UP) + ")"));
                }
            }
        }

        String externalId = getFieldValue(fields, colMap.get("EXTERNAL_ID"));
        if (externalId == null || externalId.isBlank()) {
            externalId = generateDeterministicId(ticker, positionType, qty, entryPrice, rawEntryTime);
        } else {
            externalId = externalId.trim();
        }

        return new ParsedRow(
                rowNum,
                ticker.trim().toUpperCase(),
                positionType,
                entryPrice,
                qty,
                null,
                "IMPORTED",
                exitPrice,
                exitDate,
                fees,
                externalId,
                entryDate
        );
    }

    private String findMatchedKey(String header) {
        if (ALIAS_TICKER.contains(header)) return "TICKER";
        if (ALIAS_SIDE.contains(header)) return "SIDE";
        if (ALIAS_QTY.contains(header)) return "QTY";
        if (ALIAS_ENTRY_PRICE.contains(header)) return "ENTRY_PRICE";
        if (ALIAS_EXIT_PRICE.contains(header)) return "EXIT_PRICE";
        if (ALIAS_ENTRY_TIME.contains(header)) return "ENTRY_TIME";
        if (ALIAS_EXIT_TIME.contains(header)) return "EXIT_TIME";
        if (ALIAS_COMMISSION.contains(header)) return "COMMISSION";
        if (ALIAS_EXTERNAL_ID.contains(header)) return "EXTERNAL_ID";
        if (ALIAS_PNL.contains(header)) return "PNL";
        return null;
    }

    private PositionType parsePositionType(String side) {
        if (side == null) return null;
        String s = side.trim().toLowerCase();
        if (s.contains("buy") || s.contains("long") || s.equals("b") || s.equals("l")) {
            return PositionType.LONG;
        }
        if (s.contains("sell") || s.contains("short") || s.equals("s")) {
            return PositionType.SHORT;
        }
        return null;
    }

    private BigDecimal parseBigDecimal(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            String clean = val.replaceAll("[^0-9.\\-]", "").trim();
            if (clean.isEmpty()) return null;
            return new BigDecimal(clean);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseLocalDate(String str) {
        if (str == null || str.isBlank()) return null;
        String val = str.trim();

        for (DateTimeFormatter dtf : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(val, dtf);
            } catch (DateTimeParseException ignored) {
            }
            try {
                return LocalDateTime.parse(val, dtf).toLocalDate();
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private Instant parseInstant(String str) {
        if (str == null || str.isBlank()) return null;
        String val = str.trim();

        for (DateTimeFormatter dtf : DATE_FORMATTERS) {
            try {
                return LocalDateTime.parse(val, dtf).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException ignored) {
            }
            try {
                return LocalDate.parse(val, dtf).atStartOfDay().toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private String generateDeterministicId(String ticker, PositionType side, BigDecimal qty, BigDecimal price, String timeStr) {
        String raw = (ticker + "_" + side + "_" + qty + "_" + price + "_" + (timeStr != null ? timeStr : "")).toUpperCase();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder("HASH_");
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 24);
        } catch (NoSuchAlgorithmException e) {
            return "HASH_" + raw.hashCode();
        }
    }

    private String getFieldValue(List<String> fields, Integer index) {
        if (index == null || index < 0 || index >= fields.size()) {
            return null;
        }
        return fields.get(index);
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        if (line == null) return result;

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result;
    }
}

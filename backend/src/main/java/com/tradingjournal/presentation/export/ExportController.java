package com.tradingjournal.presentation.export;

import com.tradingjournal.application.export.CsvExportService;
import com.tradingjournal.domain.entity.User;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/export")
public class ExportController {

    private final CsvExportService csvExportService;

    public ExportController(CsvExportService csvExportService) {
        this.csvExportService = csvExportService;
    }

    @GetMapping("/csv")
    public void exportCsv(@AuthenticationPrincipal User user, HttpServletResponse response) throws IOException {
        String filename = "quant-journal-export-" + LocalDate.now() + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        response.setCharacterEncoding("UTF-8");

        csvExportService.exportTrades(user, response.getWriter());
    }
}

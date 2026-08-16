package com.tradingjournal.presentation.analytics;

import com.tradingjournal.application.analytics.AnalyticsService;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.presentation.dto.BreakdownEntryDTO;
import com.tradingjournal.presentation.dto.DrawdownDTO;
import com.tradingjournal.presentation.dto.EquityPointDTO;
import com.tradingjournal.presentation.dto.TimeBreakdownDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/equity-curve")
    public ResponseEntity<List<EquityPointDTO>> equityCurve(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(analyticsService.equityCurve(user, fromDate, toDate));
    }

    @GetMapping("/drawdown")
    public ResponseEntity<DrawdownDTO> drawdown(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(analyticsService.drawdown(user, fromDate, toDate));
    }

    @GetMapping("/by-strategy")
    public ResponseEntity<List<BreakdownEntryDTO>> byStrategy(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(analyticsService.byStrategy(user, fromDate, toDate));
    }

    @GetMapping("/by-ticker")
    public ResponseEntity<List<BreakdownEntryDTO>> byTicker(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate) {
        return ResponseEntity.ok(analyticsService.byTicker(user, fromDate, toDate));
    }

    @GetMapping("/by-day-of-week")
    public ResponseEntity<List<TimeBreakdownDTO>> byDayOfWeek(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(analyticsService.byDayOfWeek(user, fromDate, toDate));
    }

    @GetMapping("/by-hour")
    public ResponseEntity<List<TimeBreakdownDTO>> byHour(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(analyticsService.byHour(user, fromDate, toDate));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<TimeBreakdownDTO>> monthly(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(analyticsService.monthly(user, fromDate, toDate));
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<TimeBreakdownDTO>> weekly(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(analyticsService.weekly(user, fromDate, toDate));
    }
}

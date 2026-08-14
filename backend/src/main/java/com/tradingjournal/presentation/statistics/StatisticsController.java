package com.tradingjournal.presentation.statistics;

import com.tradingjournal.application.statistics.StatisticsService;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.presentation.dto.StatisticsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public ResponseEntity<StatisticsDTO> getStatistics(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statisticsService.getStatistics(user));
    }
}

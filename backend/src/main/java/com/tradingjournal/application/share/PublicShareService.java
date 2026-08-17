package com.tradingjournal.application.share;

import com.tradingjournal.application.analytics.AnalyticsService;
import com.tradingjournal.application.statistics.StatisticsService;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.UserRepository;
import com.tradingjournal.presentation.dto.PublicShareDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class PublicShareService {

    private final UserRepository userRepository;
    private final StatisticsService statisticsService;
    private final AnalyticsService analyticsService;

    public PublicShareService(
            UserRepository userRepository,
            StatisticsService statisticsService,
            AnalyticsService analyticsService
    ) {
        this.userRepository = userRepository;
        this.statisticsService = statisticsService;
        this.analyticsService = analyticsService;
    }

    public PublicShareDTO getShare(String shareToken) {
        User user = userRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Share link not found"));

        if (!user.isShareEnabled()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Share link not found");
        }

        var stats = statisticsService.getStatistics(user, null, null, null);
        return new PublicShareDTO(
                stats.winRate(),
                stats.profitFactor(),
                analyticsService.equityCurve(user, null, null, null),
                analyticsService.monthly(user, null, null, null)
        );
    }
}
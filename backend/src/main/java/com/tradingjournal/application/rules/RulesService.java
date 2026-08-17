package com.tradingjournal.application.rules;

import com.tradingjournal.application.account.AccountService;
import com.tradingjournal.application.analytics.PnlCalculator;
import com.tradingjournal.domain.entity.Account;
import com.tradingjournal.domain.entity.Trade;
import com.tradingjournal.domain.entity.User;
import com.tradingjournal.infrastructure.repository.TradeRepository;
import com.tradingjournal.presentation.dto.RulesStatusDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RulesService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final TradeRepository tradeRepository;
    private final AccountService accountService;

    @Value("${app.timezone:Asia/Kolkata}")
    private String appTimezone;

    public RulesService(TradeRepository tradeRepository, AccountService accountService) {
        this.tradeRepository = tradeRepository;
        this.accountService = accountService;
    }

    public RulesStatusDTO status(User user, UUID accountId) {
        ZoneId zone = ZoneId.of(appTimezone);
        Account account = accountId != null ? accountService.resolveOwnedAccount(user, accountId) : null;

        Instant dayStart = java.time.LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant dayEnd = java.time.LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant();
        BigDecimal dailyPnl = sumNetPnl(tradeRepository.findClosedTradesInRange(user, account, dayStart, dayEnd));

        Instant monthStart = java.time.LocalDate.now(zone)
                .with(TemporalAdjusters.firstDayOfMonth())
                .atStartOfDay(zone).toInstant();
        Instant monthEnd = java.time.LocalDate.now(zone)
                .with(TemporalAdjusters.firstDayOfNextMonth())
                .atStartOfDay(zone).toInstant();
        BigDecimal monthlyPnl = sumNetPnl(tradeRepository.findClosedTradesInRange(user, account, monthStart, monthEnd));

        BigDecimal dailyLossLimit = user.getDailyLossLimitAmount();
        boolean dailyLimitHit = dailyLossLimit != null
                && dailyLossLimit.compareTo(BigDecimal.ZERO) > 0
                && dailyPnl != null
                && dailyPnl.compareTo(dailyLossLimit.negate()) <= 0;

        BigDecimal monthlyGoal = user.getMonthlyGoalPnl();
        BigDecimal progress = null;
        if (monthlyGoal != null && monthlyGoal.compareTo(BigDecimal.ZERO) != 0 && monthlyPnl != null) {
            progress = monthlyPnl.multiply(HUNDRED)
                    .divide(monthlyGoal, 4, RoundingMode.HALF_UP);
        }

        return new RulesStatusDTO(
                dailyPnl,
                dailyLossLimit,
                dailyLimitHit,
                monthlyPnl,
                monthlyGoal,
                progress
        );
    }

    private BigDecimal sumNetPnl(List<Trade> trades) {
        BigDecimal sum = BigDecimal.ZERO;
        boolean any = false;
        for (Trade trade : trades) {
            BigDecimal net = PnlCalculator.netPnl(trade);
            if (net == null) continue;
            any = true;
            sum = sum.add(net);
        }
        return any ? sum : BigDecimal.ZERO;
    }
}

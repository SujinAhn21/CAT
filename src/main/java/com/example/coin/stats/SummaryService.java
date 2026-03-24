// src/main/java/com/example/coin/stats/SummaryService.java

package com.example.coin.stats;

import com.example.coin.auth.UserPrincipal;
import com.example.coin.bot.BotDailyConfig;
import com.example.coin.bot.BotDailyConfigRepository;
import com.example.coin.bot.BotStatusRepository;
import com.example.coin.capital.CapitalFlow;
import com.example.coin.capital.CapitalFlowRepository;
import com.example.coin.stats.dto.DailyStatsResponseDto;
import com.example.coin.stats.dto.SummaryResponseDto;
import com.example.coin.trade.TradeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class SummaryService
{
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final TradeRepository tradeRepository;
    private final BotDailyConfigRepository botDailyConfigRepository;
    private final BotStatusRepository botStatusRepository;
    private final CapitalFlowRepository capitalFlowRepository;
    private final StatsService statsService;

    public SummaryService(TradeRepository tradeRepository,
                          BotDailyConfigRepository botDailyConfigRepository,
                          BotStatusRepository botStatusRepository,
                          CapitalFlowRepository capitalFlowRepository,
                          StatsService statsService)
    {
        this.tradeRepository = tradeRepository;
        this.botDailyConfigRepository = botDailyConfigRepository;
        this.botStatusRepository = botStatusRepository;
        this.capitalFlowRepository = capitalFlowRepository;
        this.statsService = statsService;
    }

    /**
     * 선택 날짜 기준 요약 정보
     * - date == null 이면 "오늘" 기준
     * - date 가 과거면 그 날짜까지의 누적 기준
     * - 미래 날짜가 들어오면 오늘로 보정
     */
    public SummaryResponseDto getSummary(UserPrincipal userPrincipal, LocalDate date)
    {
        Long userId = userPrincipal.getUserId();

        LocalDate today = LocalDate.now(KST);
        LocalDate targetDate = (date != null) ? date : today;
        if (targetDate.isAfter(today))
        {
            targetDate = today;
        }

        // 1) 긴 기간(예: 10년) 기준 일별 통계 → targetDate까지의 principal / current / 누적 손익 얻기
        int windowDays = 3650; // 10년 정도면 충분
        List<DailyStatsResponseDto> dailyStats =
                statsService.getDailyStats(userPrincipal, windowDays, targetDate);

        BigDecimal principalKrw = BigDecimal.ZERO;
        BigDecimal currentKrw = BigDecimal.ZERO;
        BigDecimal totalPnl = BigDecimal.ZERO;
        BigDecimal totalPnlRate = BigDecimal.ZERO;
        BigDecimal todayPnl = BigDecimal.ZERO;
        BigDecimal todayPnlRate = BigDecimal.ZERO;

        if (!dailyStats.isEmpty())
        {
            DailyStatsResponseDto last = dailyStats.get(dailyStats.size() - 1);

            principalKrw = nonNull(last.principalKrw);
            currentKrw   = nonNull(last.currentKrw);
            totalPnl     = nonNull(last.cumulativePnlKrw);
            totalPnlRate = nonNull(last.cumulativePnlRate);

            // targetDate 하루 손익
            todayPnl = nonNull(last.realizedPnlKrw);

            if (principalKrw.compareTo(BigDecimal.ZERO) > 0)
            {
                todayPnlRate = todayPnl
                        .divide(principalKrw, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
        }

        // 2) 자본 입출금 합계 (targetDate 까지)
        LocalDateTime endDateTime = targetDate.plusDays(1).atStartOfDay().minusNanos(1);

        BigDecimal totalDeposit = nonNull(
                capitalFlowRepository.sumAmountByUserAndFlowTypeUpTo(
                        userId, CapitalFlow.FlowType.DEPOSIT, endDateTime));
        BigDecimal totalWithdraw = nonNull(
                capitalFlowRepository.sumAmountByUserAndFlowTypeUpTo(
                        userId, CapitalFlow.FlowType.WITHDRAWAL, endDateTime));

        // 2-1) 선택 날짜(=오늘/캘린더 선택일) 입금/출금
        LocalDateTime dayStart = targetDate.atStartOfDay();
        LocalDateTime dayEnd = targetDate.plusDays(1).atStartOfDay().minusNanos(1);

        BigDecimal dayDeposit = nonNull(
                capitalFlowRepository.sumAmountByUserAndFlowTypeBetween(
                        userId, CapitalFlow.FlowType.DEPOSIT, dayStart, dayEnd));
        BigDecimal dayWithdraw = nonNull(
                capitalFlowRepository.sumAmountByUserAndFlowTypeBetween(
                        userId, CapitalFlow.FlowType.WITHDRAWAL, dayStart, dayEnd));

        // 3) skip_today (BotDailyConfig에서, 없으면 false) – 선택 날짜 기준
        boolean skipToday = botDailyConfigRepository.findByUserIdAndTradeDate(userId, targetDate)
                .map(BotDailyConfig::isSkipToday)
                .orElse(false);

        // 4) 봇 상태 (없으면 INIT) – 현재 상태 그대로
        String botStatus = botStatusRepository.findById(userId)
                .map(status -> status.getCurrentStatus().name())
                .orElse("INIT");

        return new SummaryResponseDto(
                todayPnl,
                todayPnlRate,
                totalPnl,
                totalPnlRate,
                principalKrw,
                currentKrw,
                totalDeposit,
                totalWithdraw,
                dayDeposit,
                dayWithdraw,
                skipToday,
                botStatus
        );
    }

    private BigDecimal nonNull(BigDecimal v)
    {
        return v != null ? v : BigDecimal.ZERO;
    }
}

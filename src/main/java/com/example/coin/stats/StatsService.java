// src/main/java/com/example/coin/stats/StatsService.java
// trades + capital_flows 기반 일별/주별 통계

package com.example.coin.stats;

import com.example.coin.auth.UserPrincipal;
import com.example.coin.capital.CapitalFlow;
import com.example.coin.capital.CapitalFlowRepository;
import com.example.coin.stats.dto.DailyStatsResponseDto;
import com.example.coin.stats.dto.WeeklyStatsResponseDto;
import com.example.coin.trade.Trade;
import com.example.coin.trade.TradeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 일별/주별 통계 서비스
 * - 일별: trades + capital_flows로 누적 수익률까지 계산
 * - 주별: 기존처럼 trades만 집계 (그래프 확장 시 조정 가능)
 */
@Service
public class StatsService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final TradeRepository tradeRepository;
    private final CapitalFlowRepository capitalFlowRepository;

    public StatsService(TradeRepository tradeRepository,
                        CapitalFlowRepository capitalFlowRepository) {
        this.tradeRepository = tradeRepository;
        this.capitalFlowRepository = capitalFlowRepository;
    }

    /**
     * 최근 N일 일별 통계 (끝 날짜는 오늘)
     * days가 7이면 "오늘 포함 최근 7일".
     */
    public List<DailyStatsResponseDto> getDailyStats(UserPrincipal userPrincipal, int days) {
        return getDailyStats(userPrincipal, days, null);
    }

    /**
     * 최근 N일 일별 통계
     * - endDate가 null 이면 오늘 기준
     * - endDate가 있으면 그 날짜를 끝으로 하는 최근 N일
     */
    public List<DailyStatsResponseDto> getDailyStats(UserPrincipal userPrincipal,
                                                     int days,
                                                     LocalDate endDate) {
        if (days <= 0) {
            throw new IllegalArgumentException("days must be positive");
        }

        Long userId = userPrincipal.getUserId();

        LocalDate today = LocalDate.now(KST);
        LocalDate effectiveEnd = (endDate != null) ? endDate : today;
        if (effectiveEnd.isAfter(today)) {
            effectiveEnd = today;
        }

        LocalDate startDate = effectiveEnd.minusDays(days - 1);

        // 1) trades 범위 조회
        List<Trade> trades = tradeRepository.findByUserIdAndTradeDateBetween(userId, startDate, effectiveEnd);

        // 날짜별로 당일 실현 손익 / 거래 수 집계
        Map<LocalDate, DailyAccumulator> tradeMap = new HashMap<>();
        for (Trade trade : trades) {
            LocalDate date = trade.getTradeDate();
            DailyAccumulator acc = tradeMap.computeIfAbsent(date, d -> new DailyAccumulator());
            BigDecimal pnl = nonNull(trade.getPnlKrw());
            acc.realizedPnlKrw = acc.realizedPnlKrw.add(pnl);
            acc.tradeCount += 1;
        }

        // 2) capital_flows 로 입/출금 정보 조회
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = effectiveEnd.plusDays(1).atStartOfDay().minusNanos(1);

        // startDate 이전의 입/출금 누적 합
        BigDecimal depositBeforeStart = nonNull(
                capitalFlowRepository.sumAmountByUserAndFlowTypeUpTo(
                        userId, CapitalFlow.FlowType.DEPOSIT, startDateTime.minusNanos(1)));
        BigDecimal withdrawBeforeStart = nonNull(
                capitalFlowRepository.sumAmountByUserAndFlowTypeUpTo(
                        userId, CapitalFlow.FlowType.WITHDRAWAL, startDateTime.minusNanos(1)));

        // 조회 구간 내의 입/출금 리스트
        List<CapitalFlow> flowsInRange =
                capitalFlowRepository.findByUserIdAndOccurredAtBetween(userId, startDateTime, endDateTime);

        Map<LocalDate, BigDecimal> depositByDate = new HashMap<>();
        Map<LocalDate, BigDecimal> withdrawByDate = new HashMap<>();

        for (CapitalFlow flow : flowsInRange) {
            LocalDate d = flow.getOccurredAt().toLocalDate();
            BigDecimal amt = nonNull(flow.getAmountKrw());
            if (flow.getFlowType() == CapitalFlow.FlowType.DEPOSIT) {
                depositByDate.merge(d, amt, BigDecimal::add);
            } else if (flow.getFlowType() == CapitalFlow.FlowType.WITHDRAWAL) {
                withdrawByDate.merge(d, amt, BigDecimal::add);
            }
        }

        // 3) 날짜 루프를 돌면서 누적 값 계산
        List<DailyStatsResponseDto> result = new ArrayList<>();

        BigDecimal cumulativePnl = BigDecimal.ZERO;
        BigDecimal totalDepositSoFar = depositBeforeStart;
        BigDecimal totalWithdrawSoFar = withdrawBeforeStart;

        LocalDate cursor = startDate;
        while (!cursor.isAfter(effectiveEnd)) {
            DailyAccumulator tradeAcc = tradeMap.getOrDefault(cursor, new DailyAccumulator());

            // 당일 입/출금 반영
            BigDecimal depToday = depositByDate.getOrDefault(cursor, BigDecimal.ZERO);
            BigDecimal wdToday = withdrawByDate.getOrDefault(cursor, BigDecimal.ZERO);
            totalDepositSoFar = totalDepositSoFar.add(depToday);
            totalWithdrawSoFar = totalWithdrawSoFar.add(wdToday);

            // 당일 실현 손익 및 누적 손익
            cumulativePnl = cumulativePnl.add(tradeAcc.realizedPnlKrw);

            // 순투입 원금 = 총 입금 - 총 출금
            BigDecimal principalKrw = totalDepositSoFar.subtract(totalWithdrawSoFar);

            // 현재 금액 = 원금 + 누적 실현 손익
            BigDecimal currentKrw = principalKrw.add(cumulativePnl);

            // 누적 수익률 (%)
            BigDecimal cumulativeRate = BigDecimal.ZERO;
            if (principalKrw.compareTo(BigDecimal.ZERO) > 0) {
                cumulativeRate = cumulativePnl
                        .divide(principalKrw, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            result.add(new DailyStatsResponseDto(
                    cursor,
                    tradeAcc.realizedPnlKrw,
                    tradeAcc.tradeCount,
                    cumulativePnl,
                    cumulativeRate,
                    principalKrw,
                    currentKrw
            ));

            cursor = cursor.plusDays(1);
        }

        return result;
    }

    /**
     * 최근 N주 주별 통계 (기존 로직 유지)
     */
    public List<WeeklyStatsResponseDto> getWeeklyStats(UserPrincipal userPrincipal, int weeks) {
        if (weeks <= 0) {
            throw new IllegalArgumentException("weeks must be positive");
        }

        Long userId = userPrincipal.getUserId();

        LocalDate today = LocalDate.now(KST);
        LocalDate startDate = today.minusDays(weeks * 7L - 1); // 예: weeks=4 → 오늘 포함 28일 window

        List<Trade> trades = tradeRepository.findByUserIdAndTradeDateBetween(userId, startDate, today);

        // 주(7일) 단위 버킷 집계
        WeeklyAccumulator[] buckets = new WeeklyAccumulator[weeks];
        for (int i = 0; i < weeks; i++) {
            buckets[i] = new WeeklyAccumulator();
        }

        for (Trade trade : trades) {
            LocalDate date = trade.getTradeDate();
            long daysFromStart = ChronoUnit.DAYS.between(startDate, date);
            int bucketIndex = (int) (daysFromStart / 7);

            if (bucketIndex < 0 || bucketIndex >= weeks) {
                continue;
            }

            WeeklyAccumulator acc = buckets[bucketIndex];
            BigDecimal pnl = nonNull(trade.getPnlKrw());
            acc.realizedPnlKrw = acc.realizedPnlKrw.add(pnl);
            acc.tradeCount += 1;
        }

        List<WeeklyStatsResponseDto> result = new ArrayList<>();
        for (int i = 0; i < weeks; i++) {
            LocalDate bucketStart = startDate.plusDays(i * 7L);
            LocalDate bucketEnd = bucketStart.plusDays(6);
            if (bucketEnd.isAfter(today)) {
                bucketEnd = today;
            }

            WeeklyAccumulator acc = buckets[i];
            result.add(new WeeklyStatsResponseDto(
                    bucketStart,
                    bucketEnd,
                    acc.realizedPnlKrw,
                    acc.tradeCount
            ));
        }

        return result;
    }

    private static class DailyAccumulator {
        BigDecimal realizedPnlKrw = BigDecimal.ZERO;
        int tradeCount = 0;
    }

    private static class WeeklyAccumulator {
        BigDecimal realizedPnlKrw = BigDecimal.ZERO;
        int tradeCount = 0;
    }

    private BigDecimal nonNull(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}

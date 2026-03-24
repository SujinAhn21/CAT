// src/main/java/com/example/coin/stats/dto/WeeklyStatsResponseDto.java

package com.example.coin.stats.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 주별 통계 응답 DTO
 *
 * 주 단위는 "최근 N주"를 7일씩 끊은 롤링 윈도우로 본다.
 * 예: weeks=4면 오늘 포함 최근 28일을 7일씩 4버킷으로 나눔.
 */
public class WeeklyStatsResponseDto {

    // 이 주간 구간의 시작일
    public LocalDate startDate;

    // 이 주간 구간의 종료일
    public LocalDate endDate;

    // 해당 주간의 실현 손익 합 (KRW)
    public BigDecimal realizedPnlKrw;

    // 해당 주간의 거래 횟수
    public int tradeCount;

    public WeeklyStatsResponseDto() {
    }

    public WeeklyStatsResponseDto(LocalDate startDate,
                                  LocalDate endDate,
                                  BigDecimal realizedPnlKrw,
                                  int tradeCount) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.realizedPnlKrw = realizedPnlKrw;
        this.tradeCount = tradeCount;
    }
}

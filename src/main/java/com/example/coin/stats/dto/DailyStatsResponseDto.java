// src/main/java/com/example/coin/stats/dto/DailyStatsResponseDto.java

package com.example.coin.stats.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 일별 통계 응답 DTO
 * - 해당 날짜 실현 손익 / 거래 수
 * - 해당 날짜까지의 누적 손익 / 누적 수익률
 * - 해당 날짜 기준 원금 / 현재 금액
 */
public class DailyStatsResponseDto {

    // 거래 날짜 (한국 기준)
    public LocalDate tradeDate;

    // 해당 날짜 실현 손익 합 (KRW)
    public BigDecimal realizedPnlKrw;

    // 해당 날짜 거래 횟수 (체결 수)
    public int tradeCount;

    // 이 날짜까지의 누적 실현 손익 (KRW)
    public BigDecimal cumulativePnlKrw;

    // 이 날짜까지의 누적 수익률 (%)
    public BigDecimal cumulativePnlRate;

    // 이 날짜 기준 순투입 원금 (총 입금 - 총 출금)
    public BigDecimal principalKrw;

    // 이 날짜 기준 현재 금액 (원금 + 누적 실현 손익)
    public BigDecimal currentKrw;

    public DailyStatsResponseDto() {
    }

    public DailyStatsResponseDto(LocalDate tradeDate,
                                 BigDecimal realizedPnlKrw,
                                 int tradeCount,
                                 BigDecimal cumulativePnlKrw,
                                 BigDecimal cumulativePnlRate,
                                 BigDecimal principalKrw,
                                 BigDecimal currentKrw) {
        this.tradeDate = tradeDate;
        this.realizedPnlKrw = realizedPnlKrw;
        this.tradeCount = tradeCount;
        this.cumulativePnlKrw = cumulativePnlKrw;
        this.cumulativePnlRate = cumulativePnlRate;
        this.principalKrw = principalKrw;
        this.currentKrw = currentKrw;
    }
}

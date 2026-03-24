// src/main/java/com/example/coin/stats/dto/SummaryResponseDto.java

package com.example.coin.stats.dto;

import java.math.BigDecimal;

/**
 * 홈 상단 요약 정보 DTO
 * - 오늘 손익 / 누적 손익
 * - 원금(순투입 자본) / 현재 금액 / 입출금
 * - 수익률, 봇 상태, skip_today
 */
public class SummaryResponseDto {

    // 오늘 실현 손익 (KRW)
    public BigDecimal todayPnlKrw;

    // 오늘 수익률 (%) - 필요 시 사용
    public BigDecimal todayPnlRate;

    // 전체 기간 누적 실현 손익 (KRW)
    public BigDecimal totalPnlKrw;

    // 전체 기간 누적 수익률 (%)
    public BigDecimal totalPnlRate;

    // 기준 원금 (총 입금 - 총 출금, 순투입 자본)
    public BigDecimal principalKrw;

    // 현재 금액 (원금 + 누적 실현 손익)
    public BigDecimal currentKrw;

    // 지금까지 총 입금액
    public BigDecimal totalDepositKrw;

    // 지금까지 총 출금액
    public BigDecimal totalWithdrawKrw;

    // 선택 날짜(=오늘/캘린더 선택일) 입금액
    public BigDecimal dayDepositKrw;

    // 선택 날짜(=오늘/캘린더 선택일) 출금액
    public BigDecimal dayWithdrawKrw;

    // 오늘은 진입 안 함 여부
    public boolean skipToday;

    // 봇 상태 (INIT / RUNNING / STOPPED / PAUSED 등)
    public String botStatus;

    public SummaryResponseDto() {
    }

    // 기존 시그니처(호환용) 유지
    public SummaryResponseDto(BigDecimal todayPnlKrw,
                              BigDecimal todayPnlRate,
                              BigDecimal totalPnlKrw,
                              BigDecimal totalPnlRate,
                              BigDecimal principalKrw,
                              BigDecimal currentKrw,
                              BigDecimal totalDepositKrw,
                              BigDecimal totalWithdrawKrw,
                              boolean skipToday,
                              String botStatus) {
        this.todayPnlKrw = todayPnlKrw;
        this.todayPnlRate = todayPnlRate;
        this.totalPnlKrw = totalPnlKrw;
        this.totalPnlRate = totalPnlRate;
        this.principalKrw = principalKrw;
        this.currentKrw = currentKrw;
        this.totalDepositKrw = totalDepositKrw;
        this.totalWithdrawKrw = totalWithdrawKrw;
        this.dayDepositKrw = BigDecimal.ZERO;
        this.dayWithdrawKrw = BigDecimal.ZERO;
        this.skipToday = skipToday;
        this.botStatus = botStatus;
    }

    // 확장 시그니처(선택 날짜 입금/출금 포함)
    public SummaryResponseDto(BigDecimal todayPnlKrw,
                              BigDecimal todayPnlRate,
                              BigDecimal totalPnlKrw,
                              BigDecimal totalPnlRate,
                              BigDecimal principalKrw,
                              BigDecimal currentKrw,
                              BigDecimal totalDepositKrw,
                              BigDecimal totalWithdrawKrw,
                              BigDecimal dayDepositKrw,
                              BigDecimal dayWithdrawKrw,
                              boolean skipToday,
                              String botStatus) {
        this.todayPnlKrw = todayPnlKrw;
        this.todayPnlRate = todayPnlRate;
        this.totalPnlKrw = totalPnlKrw;
        this.totalPnlRate = totalPnlRate;
        this.principalKrw = principalKrw;
        this.currentKrw = currentKrw;
        this.totalDepositKrw = totalDepositKrw;
        this.totalWithdrawKrw = totalWithdrawKrw;
        this.dayDepositKrw = dayDepositKrw;
        this.dayWithdrawKrw = dayWithdrawKrw;
        this.skipToday = skipToday;
        this.botStatus = botStatus;
    }
}

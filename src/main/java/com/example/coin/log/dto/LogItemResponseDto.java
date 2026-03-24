// src/main/java/com/example/coin/log/dto/LogItemResponseDto.java

package com.example.coin.log.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 통합 로그 단건 DTO
 * - kind: TRADE / CAPITAL
 * - action: BUY / SELL / DEPOSIT / WITHDRAWAL
 */
@Getter
@Builder
public class LogItemResponseDto
{
    private String kind;        // "TRADE" | "CAPITAL"
    private Long id;

    private String action;      // "BUY" | "SELL" | "DEPOSIT" | "WITHDRAWAL"

    private String symbol;      // trade only
    private Double volume;      // trade only

    private Long amountKrw;     // KRW (rounded)
    private Long profitLossKrw; // trade only (pnl_krw)
    private String memo;        // capital only

    private LocalDateTime occurredAt; // trade_time or occurred_at
}

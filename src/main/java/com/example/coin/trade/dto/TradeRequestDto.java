// src/main/java/com/example/coin/trade/dto/TradeRequestDto.java
// Python 봇이 POST /api/trades 할 때 쓰는 요청 바디.

package com.example.coin.trade.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Python 봇이 POST /api/trades 호출할 때 사용하는 요청 바디.
 * - user_id 는 JWT(로그인 사용자) 기준으로 서버에서 세팅하므로 바디에는 포함하지 않음.
 */
@Getter
@Setter
@NoArgsConstructor
public class TradeRequestDto
{
    /**
     * 체결 시각 (KST 기준 ISO-8601 문자열을 LocalDateTime으로 변환해서 사용)
     */
    private LocalDateTime tradeTime;

    /**
     * 심볼 (기본 KRW-BTC)
     */
    private String symbol;

    /**
     * 매수/매도: "BUY" / "SELL"
     */
    private String side;

    /**
     * 체결가 (BTC 1개 기준 KRW)
     */
    private BigDecimal price;

    /**
     * 수량 (BTC)
     */
    private BigDecimal volume;

    /**
     * 금액 (price * volume, KRW). null 이면 서버에서 계산.
     */
    private BigDecimal amountKrw;

    /**
     * 수수료 (KRW)
     */
    private BigDecimal feeKrw;

    /**
     * 실현 손익 (KRW)
     */
    private BigDecimal pnlKrw;

    /**
     * 수익률 (비율)
     */
    private BigDecimal pnlRate;

    /**
     * Upbit 주문 ID
     */
    private String upbitOrderId;

    /**
     * 주문 타입 (MARKET, LIMIT 등)
     */
    private String orderType;

    /**
     * 원본 JSON 문자열
     */
    private String rawPayload;
}


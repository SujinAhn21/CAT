// src/main/java/com/example/coin/log/dto/UnifiedLogItemDto.java

package com.example.coin.log.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 통합 로그 단건 DTO
 * - trades(매수/매도) + capital_flows(입금/출금) 를 한 리스트로 섞어서 내려준다.
 *
 * 프론트(app.js)의 기존 trade 렌더링 포맷을 최대한 재사용하기 위해
 * 필드명을 tradeTime / profitLossKrw / amountKrw / volume / side 로 맞췄다.
 */
public class UnifiedLogItemDto
{
    // "TRADE" or "CAPITAL"
    public String kind;

    // "BUY" / "SELL" / "DEPOSIT" / "WITHDRAWAL"
    public String side;

    // KRW 금액
    public BigDecimal amountKrw;

    // BTC 수량(TRADE만), CAPITAL은 null
    public BigDecimal volume;

    // 실현 손익(TRADE만), CAPITAL은 null
    public BigDecimal profitLossKrw;

    // 발생 시각(TRADE: trade_time, CAPITAL: occurred_at)
    public LocalDateTime tradeTime;

    // CAPITAL 메모(선택)
    public String memo;

    // 원본 테이블 PK
    public long id;

    // 정렬 타이브레이커(TRADE=1, CAPITAL=2)
    public int kindRank;

    public UnifiedLogItemDto() {
    }

    public UnifiedLogItemDto(String kind,
                             String side,
                             BigDecimal amountKrw,
                             BigDecimal volume,
                             BigDecimal profitLossKrw,
                             LocalDateTime tradeTime,
                             String memo,
                             long id,
                             int kindRank) {
        this.kind = kind;
        this.side = side;
        this.amountKrw = amountKrw;
        this.volume = volume;
        this.profitLossKrw = profitLossKrw;
        this.tradeTime = tradeTime;
        this.memo = memo;
        this.id = id;
        this.kindRank = kindRank;
    }
}

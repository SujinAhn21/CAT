// src/main/java/com/example/coin/trade/dto/TradeResponseDto.java
package com.example.coin.trade.dto;

import com.example.coin.trade.Trade;
import com.example.coin.trade.TradeSide;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 단일 체결 응답 DTO.
 * - 리스트/상세 조회에서 공통으로 사용.
 */
@Getter
public class TradeResponseDto
{
    private Long id;
    private String symbol;        // KRW-BTC
    private String side;          // "BUY" / "SELL"
    private long price;           // 체결가 (KRW)
    private double volume;        // 수량 (BTC)
    private long amountKrw;       // 체결 금액 (KRW)
    private Long profitLossKrw;   // 실현 손익 (pnl_krw), 없으면 null
    private LocalDateTime tradeTime;

    /**
     * Trade 엔티티 -> DTO 변환.
     */
    public static TradeResponseDto fromEntity(Trade t)
    {
        if (t == null) {
            return null;
        }

        TradeResponseDto dto = new TradeResponseDto();
        dto.id = t.getId();
        dto.symbol = t.getSymbol();

        TradeSide sideEnum = t.getSide();
        dto.side = (sideEnum != null ? sideEnum.name() : null);

        BigDecimal price = t.getPrice();
        BigDecimal volume = t.getVolume();
        BigDecimal amount = t.getAmountKrw();
        BigDecimal pnl = t.getPnlKrw();   // 엔티티 필드 이름에 맞게

        dto.price = (price != null ? price.longValue() : 0L);
        dto.volume = (volume != null ? volume.doubleValue() : 0.0);
        dto.amountKrw = (amount != null ? amount.longValue() : 0L);
        dto.profitLossKrw = (pnl != null ? pnl.longValue() : null);
        dto.tradeTime = t.getTradeTime();
        return dto;
    }

    /**
     * 이전 이름 호환을 위해 남겨둠.
     */
    public static TradeResponseDto from(Trade t)
    {
        return fromEntity(t);
    }
}

// src/main/java/com/example/coin/trade/Trade.java
// (Entity: trades)


package com.example.coin.trade;

import com.example.coin.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * trades 테이블 매핑 엔티티.
 * - 실매매 체결 로그 (당일/과거 조회, 통계의 기초 데이터)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "trades")
public class Trade
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 사용자의 체결인지 (users.id FK)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 실제 체결 시각 (초 단위까지)
     */
    @Column(name = "trade_time", nullable = false)
    private LocalDateTime tradeTime;

    /**
     * 한국 시간 기준 날짜 조회용 컬럼 (YYYY-MM-DD)
     */
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    /**
     * 코인 심볼 (기본 KRW-BTC)
     */
    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    /**
     * 매수/매도 방향
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 10)
    private TradeSide side;

    /**
     * 체결가 (BTC 1개 기준 KRW)
     */
    @Column(name = "price", nullable = false, precision = 18, scale = 8)
    private BigDecimal price;

    /**
     * 체결 수량 (BTC)
     */
    @Column(name = "volume", nullable = false, precision = 18, scale = 8)
    private BigDecimal volume;

    /**
     * 체결 금액 (price * volume, KRW)
     */
    @Column(name = "amount_krw", nullable = false, precision = 18, scale = 2)
    private BigDecimal amountKrw;

    /**
     * 수수료 (KRW 기준으로 환산)
     */
    @Column(name = "fee_krw", nullable = false, precision = 18, scale = 2)
    private BigDecimal feeKrw;

    /**
     * 이 체결로 확정된 실현 손익 (KRW)
     */
    @Column(name = "pnl_krw", nullable = false, precision = 18, scale = 2)
    private BigDecimal pnlKrw;

    /**
     * 필요 시 수익률 (비율, 예: 0.0123)
     */
    @Column(name = "pnl_rate", precision = 10, scale = 4)
    private BigDecimal pnlRate;

    /**
     * Upbit 주문 ID (선택)
     */
    @Column(name = "upbit_order_id", length = 64)
    private String upbitOrderId;

    /**
     * 주문 타입 (MARKET, LIMIT 등)
     */
    @Column(name = "order_type", nullable = false, length = 30)
    private String orderType;

    /**
     * Upbit 원본 응답 JSON (선택)
     * DB에는 JSON 타입으로 들어가지만, JPA에서는 문자열로 매핑.
     */
    @Lob
    @Column(name = "raw_payload", columnDefinition = "json")
    private String rawPayload;

    /**
     * 레코드 생성 시각
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

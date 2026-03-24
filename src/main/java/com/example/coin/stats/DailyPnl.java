// src/main/java/com/example/coin/stats/DailyPnl.java
// (Entity: daily_pnl)

package com.example.coin.stats;

import com.example.coin.common.BaseTimeEntity;
import com.example.coin.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 일별 손익 요약 테이블.
 * 현재 StatsService 는 trades 를 직접 group by 하지만,
 * 나중에 성능 최적화를 위해 이 엔티티를 활용할 예정이다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "daily_pnl")
public class DailyPnl extends BaseTimeEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 사용자의 통계인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 거래 날짜 (KST 기준)
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    // 해당 날짜의 실현 손익 합 (KRW)
    @Column(name = "realized_pnl_krw", nullable = false, precision = 18, scale = 2)
    private BigDecimal realizedPnlKrw;

    // 해당 날짜의 거래 횟수
    @Column(name = "trade_count", nullable = false)
    private int tradeCount;
}

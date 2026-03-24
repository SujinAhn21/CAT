// src/main/java/com/example/coin/bot/BotDailyConfig.java
// best_K, max_ror, skip_today, start_equity 등 일일 봇 설정

package com.example.coin.bot;

import com.example.coin.common.BaseTimeEntity;
import com.example.coin.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bot_daily_config")
public class BotDailyConfig extends BaseTimeEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 사용자의 설정인지 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 09:00 기준 거래일
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    // best_K 값
    @Column(name = "best_k", nullable = false, precision = 4, scale = 3)
    private BigDecimal bestK;

    // best_K 기준 max_ror
    @Column(name = "max_ror", nullable = false, precision = 18, scale = 8)
    private BigDecimal maxRor;

    // 오늘은 진입 안 함 여부
    @Column(name = "skip_today", nullable = false)
    private boolean skipToday;

    // 일간 손실 한도(예: -0.03)
    @Column(name = "loss_limit_rate", precision = 10, scale = 4)
    private BigDecimal lossLimitRate;

    // 09:00 시점 계좌 평가액
    @Column(name = "start_equity_krw", precision = 18, scale = 2)
    private BigDecimal startEquityKrw;
}

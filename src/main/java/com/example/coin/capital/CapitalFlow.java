// src/main/java/com/example/coin/capital/CapitalFlow.java
// 자본 입출금 로그 엔티티 (capital_flows 테이블 매핑)

package com.example.coin.capital;

import com.example.coin.common.BaseTimeEntity;
import com.example.coin.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * capital_flows 테이블 매핑 엔티티.
 *
 * DDL:
 * CREATE TABLE capital_flows (
 *   id           BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
 *   user_id      BIGINT UNSIGNED NOT NULL,
 *   flow_type    ENUM('DEPOSIT', 'WITHDRAWAL') NOT NULL,
 *   amount_krw   DECIMAL(18,2)   NOT NULL,
 *   occurred_at  DATETIME(6)     NOT NULL,
 *   memo         VARCHAR(255)    NULL,
 *   created_at   DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 *   updated_at   DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
 *                                 ON UPDATE CURRENT_TIMESTAMP(6)
 * );
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "capital_flows")
public class CapitalFlow extends BaseTimeEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 사용자의 자본 흐름인지 (users.id FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 자본 흐름 타입 (입금 / 출금)
    @Enumerated(EnumType.STRING)
    @Column(name = "flow_type", nullable = false, length = 10)
    private FlowType flowType;

    // 금액 (KRW)
    @Column(name = "amount_krw", nullable = false, precision = 18, scale = 2)
    private BigDecimal amountKrw;

    // 자본 흐름이 실제로 발생한 시각 (DATETIME(6))
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    // 메모 (선택)
    @Column(name = "memo", length = 255)
    private String memo;

    public enum FlowType
    {
        DEPOSIT,
        WITHDRAWAL
    }
}

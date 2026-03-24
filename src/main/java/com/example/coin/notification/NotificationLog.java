// src/main/java/com/example/coin/notification/NotificationLog.java
// FCM 발송 내역 로그

package com.example.coin.notification;

import com.example.coin.bot.BotErrorLog;
import com.example.coin.trade.Trade;
import com.example.coin.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notification_logs")
public class NotificationLog
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 사용자에게 보낸 알림인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // BUY / SELL / ERROR
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private Type type;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "body", length = 255)
    private String body;

    // 실제 발송 시각
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    // 발송 성공 여부
    @Column(name = "success", nullable = false)
    private boolean success;

    // 사용한 FCM 토큰 (문제 생겼을 때 추적용)
    @Column(name = "fcm_token", length = 255)
    private String fcmToken;

    // 실패 사유(예외 메시지 등)
    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    // 매수/매도 알림이면 관련 체결 레코드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_trade_id")
    private Trade relatedTrade;

    // 오류 알림이면 관련 봇 에러 로그
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_error_id")
    private BotErrorLog relatedError;

    public enum Type
    {
        BUY,
        SELL,
        ERROR
    }
}

// src/main/java/com/example/coin/bot/BotErrorLog.java
// (Entity: bot_error_logs)
// 봇 에러 로그 (반복 에러/중단 원인 기록)

package com.example.coin.bot;

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
@Table(name = "bot_error_logs")
public class BotErrorLog
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 사용자의 에러인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", nullable = false, length = 255)
    private String errorMessage;

    @Column(name = "error_detail", columnDefinition = "TEXT")
    private String errorDetail;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private Severity severity;

    @Column(name = "handled", nullable = false)
    private boolean handled;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate()
    {
        if (createdAt == null)
        {
            createdAt = LocalDateTime.now();
        }
    }

    public enum Severity
    {
        WARN,
        ERROR,
        FATAL
    }
}


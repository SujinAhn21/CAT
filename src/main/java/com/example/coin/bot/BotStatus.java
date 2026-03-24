// src/main/java/com/example/coin/bot/BotStatus.java
// (Entity: bot_status)

// src/main/java/com/example/coin/bot/BotStatus.java
// 현재 봇 상태 (RUNNING/STOPPED 등) + 마지막 하트비트/에러 정보

package com.example.coin.bot;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bot_status")
public class BotStatus
{
    // user_id가 PK 겸 FK
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 16)
    private Status currentStatus;

    @Column(name = "last_heartbeat_at")
    private LocalDateTime lastHeartbeatAt;

    @Column(name = "last_error_code", length = 50)
    private String lastErrorCode;

    @Column(name = "last_error_message", length = 255)
    private String lastErrorMessage;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void touchUpdatedAt()
    {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Status
    {
        INIT,
        RUNNING,
        STOPPED,
        PAUSED
    }
}


// src/main/java/com/example/coin/loginlog/LoginLog.java
// (Entity: login_logs)
package com.example.coin.loginlog;

import com.example.coin.common.BaseTimeEntity;
import com.example.coin.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 로그인 시도 이력 로그.
 * 성공/실패 모두 기록해서 향후 모니터링에 활용할 수 있다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "login_logs")
public class LoginLog extends BaseTimeEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 성공한 로그인일 경우 user 가 채워지고, 실패 시 null 일 수 있다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 카카오 numeric ID (성공/실패 모두 기록)
    @Column(name = "kakao_user_id")
    private Long kakaoUserId;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;
}


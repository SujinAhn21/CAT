// src/main/java/com/example/coin/notification/DeviceToken.java
// FCM 디바이스 토큰 (브라우저/PWA/앱 단위)

package com.example.coin.notification;

import com.example.coin.common.BaseTimeEntity;
import com.example.coin.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "device_tokens")
public class DeviceToken extends BaseTimeEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 사용자 것인지 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // FCM 토큰 값 (브라우저/PWA별로 유일)
    @Column(name = "fcm_token", nullable = false, length = 255, unique = true)
    private String fcmToken;

    // WEB / ANDROID / IOS
    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 16)
    private Platform platform;

    // 활성/비활성 (로그아웃/브라우저 폐기 등으로 더 이상 사용되지 않을 때 false)
    @Column(name = "is_active", nullable = false)
    private boolean active;

    public enum Platform
    {
        WEB,
        ANDROID,
        IOS
    }
}

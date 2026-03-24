// src/main/java/com/example/coin/notification/DeviceTokenRepository.java

package com.example.coin.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long>
{
    // fcm_token 으로 단일 조회
    Optional<DeviceToken> findByFcmToken(String fcmToken);

    // 특정 사용자 + active=true 인 토큰들
    List<DeviceToken> findAllByUserIdAndActiveIsTrue(Long userId);
}

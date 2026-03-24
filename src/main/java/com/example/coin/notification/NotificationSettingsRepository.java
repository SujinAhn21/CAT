// src/main/java/com/example/coin/notification/NotificationSettingsRepository.java
// 알림 설정 레포지토리
//  - PK(user_id) 기준으로 조회

package com.example.coin.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long>
{
    // 추가 커스텀 메서드 현재는 필요 없음
    // Optional<NotificationSettings> findByUser(User user); 이런 것도 필요해지면 나중에 추가
}

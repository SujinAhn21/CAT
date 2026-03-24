// src/main/java/com/example/coin/notification/NotificationLogRepository.java

package com.example.coin.notification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long>
{
    // 필요하면 나중에 사용자별 최근 알림 조회 메서드 추가
}

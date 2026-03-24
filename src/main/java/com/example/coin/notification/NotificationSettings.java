// src/main/java/com/example/coin/notification/NotificationSettings.java
// 알림 설정 엔티티
//  - DB 테이블 notification_settings 에 매핑
//  - PK = user_id (users(id)와 1:1)

package com.example.coin.notification;

import com.example.coin.common.BaseTimeEntity;
import com.example.coin.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_settings")
public class NotificationSettings extends BaseTimeEntity
{
    /**
     * PK = user_id
     * users 테이블의 id 와 1:1 매핑
     */
    @Id
    @Column(name = "user_id")
    private Long id;

    /**
     * User 와 1:1 관계, PK 공유
     * - @MapsId 로 user.id 값을 그대로 PK로 사용
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 매수 알림 on/off
     */
    @Column(name = "enable_buy", nullable = false)
    private boolean enableBuy = true;

    /**
     * 매도 알림 on/off
     */
    @Column(name = "enable_sell", nullable = false)
    private boolean enableSell = true;

    /**
     * 오류 알림 on/off
     */
    @Column(name = "enable_error", nullable = false)
    private boolean enableError = true;
}

// src/main/java/com/example/coin/notification/dto/NotificationSettingsResponseDto.java
// 알림 설정 조회 응답 DTO

package com.example.coin.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NotificationSettingsResponseDto
{
    public boolean enableBuy;
    public boolean enableSell;
    public boolean enableError;
}

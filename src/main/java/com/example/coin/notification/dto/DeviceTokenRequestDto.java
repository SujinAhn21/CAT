// src/main/java/com/example/coin/notification/dto/DeviceTokenRequestDto.java
// 프론트에서 FCM 토큰 등록용 요청 DTO

package com.example.coin.notification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceTokenRequestDto
{
    // FCM 토큰 (필수)
    private String fcmToken;

    // "WEB", "ANDROID", "IOS" (없으면 WEB으로 처리)
    private String platform;
}
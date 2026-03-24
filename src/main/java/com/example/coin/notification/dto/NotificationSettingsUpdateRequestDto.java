// src/main/java/com/example/coin/notification/dto/NotificationSettingsUpdateRequestDto.java
// 알림 설정 수정 요청 DTO

package com.example.coin.notification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationSettingsUpdateRequestDto
{
    // null이 아니면 해당 값으로 업데이트, null이면 기존 값 유지
    private Boolean enableBuy;
    private Boolean enableSell;
    private Boolean enableError;
}
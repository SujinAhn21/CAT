// src/main/java/com/example/coin/bot/dto/BotErrorLogRequestDto.java
// 에러 발생 시 로그/알림용 DTO

package com.example.coin.bot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BotErrorLogRequestDto
{
    private String errorCode;
    private String errorMessage;
    private String errorDetail;

    // "WARN", "ERROR", "FATAL"
    private String severity;
}

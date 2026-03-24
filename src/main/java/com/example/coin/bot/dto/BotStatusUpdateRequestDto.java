// src/main/java/com/example/coin/bot/dto/BotStatusUpdateRequestDto.java
// 봇 상태 업데이트용 DTO (RUNNING/STOPPED 등)

package com.example.coin.bot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BotStatusUpdateRequestDto
{
    // "INIT", "RUNNING", "STOPPED", "PAUSED"
    private String currentStatus;

    private String lastErrorCode;
    private String lastErrorMessage;
}

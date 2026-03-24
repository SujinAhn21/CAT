// src/main/java/com/example/coin/bot/dto/BotDailyConfigRequestDto.java
// Python 봇이 하루 한 번 보내는 일일 설정 정보

package com.example.coin.bot.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BotDailyConfigRequestDto
{
    // "YYYY-MM-DD" 문자열, null이면 서버에서 오늘(KST)로 처리
    private String tradeDate;

    private BigDecimal bestK;
    private BigDecimal maxRor;

    // null이면 false로 본다.
    private Boolean skipToday;

    private BigDecimal lossLimitRate;
    private BigDecimal startEquityKrw;
}

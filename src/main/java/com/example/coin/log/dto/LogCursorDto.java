// src/main/java/com/example/coin/log/LogCursorDto.java

package com.example.coin.log.dto;

import java.time.LocalDateTime;

/**
 * 커서 DTO
 * - occurredAt: 정렬 기준 시간
 * - kindRank  : TRADE=1, CAPITAL=2
 * - id        : 각 테이블 PK
 */
public class LogCursorDto
{
    public LocalDateTime occurredAt;
    public int kindRank;
    public long id;

    public LogCursorDto() {
    }

    public LogCursorDto(LocalDateTime occurredAt, int kindRank, long id) {
        this.occurredAt = occurredAt;
        this.kindRank = kindRank;
        this.id = id;
    }
}

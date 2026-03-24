// src/main/java/com/example/coin/log/dto/UnifiedLogPageResponseDto.java

package com.example.coin.log.dto;

import java.util.List;

/**
 * 통합 로그 페이지 응답 (커서 기반)
 * - prevCursor: 현재 페이지의 "첫 아이템" 커서 (이걸 cursor로 dir=prev 호출)
 * - nextCursor: 현재 페이지의 "마지막 아이템" 커서 (이걸 cursor로 dir=next 호출)
 */
public class UnifiedLogPageResponseDto
{
    public List<UnifiedLogItemDto> items;

    public String prevCursor;
    public String nextCursor;

    public boolean hasPrev;
    public boolean hasNext;

    public UnifiedLogPageResponseDto() {
    }

    public UnifiedLogPageResponseDto(List<UnifiedLogItemDto> items,
                                     String prevCursor,
                                     String nextCursor,
                                     boolean hasPrev,
                                     boolean hasNext) {
        this.items = items;
        this.prevCursor = prevCursor;
        this.nextCursor = nextCursor;
        this.hasPrev = hasPrev;
        this.hasNext = hasNext;
    }
}

// src/main/java/com/example/coin/trade/dto/TradePageResponseDto.java
// 페이징 리스트 응답(오늘, 특정 날짜 기록).

package com.example.coin.trade.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 오늘/특정일 매매 기록을 페이지 단위로 내려줄 때 사용하는 DTO.
 */
@Getter
@Builder
public class TradePageResponseDto
{
    private List<TradeResponseDto> content;
    private int page;          // 현재 페이지 (1-based)
    private int size;          // 페이지 크기
    private long totalElements;
    private int totalPages;
    private boolean last;
}


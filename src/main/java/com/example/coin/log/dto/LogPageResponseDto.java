// src/main/java/com/example/coin/log/dto/LogPageResponseDto.java

package com.example.coin.log.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 통합 로그 페이징 응답 DTO (1-based page)
 */
@Getter
@Builder
public class LogPageResponseDto
{
    private List<LogItemResponseDto> content;
    private int page;          // 현재 페이지 (1-based)
    private int size;          // 페이지 크기
    private long totalElements;
    private int totalPages;
    private boolean last;
}

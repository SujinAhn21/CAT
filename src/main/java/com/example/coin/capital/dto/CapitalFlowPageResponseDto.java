// src/main/java/com/example/coin/capital/dto/CapitalFlowPageResponseDto.java

package com.example.coin.capital.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 선택일(또는 오늘) 입출금 로그 페이징 응답 DTO
 */
@Getter
@Builder
public class CapitalFlowPageResponseDto
{
    private List<CapitalFlowResponseDto> content;
    private int page;          // 1-based
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}

// src/main/java/com/example/coin/capital/dto/CapitalFlowResponseDto.java

package com.example.coin.capital.dto;

import com.example.coin.capital.CapitalFlow;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 단일 입출금 로그 응답 DTO
 */
@Getter
public class CapitalFlowResponseDto
{
    private Long id;
    private String flowType;          // "DEPOSIT" / "WITHDRAWAL"
    private long amountKrw;           // KRW
    private LocalDateTime occurredAt; // 발생 시각
    private String memo;              // 메모

    public static CapitalFlowResponseDto fromEntity(CapitalFlow cf)
    {
        if (cf == null) {
            return null;
        }

        CapitalFlowResponseDto dto = new CapitalFlowResponseDto();
        dto.id = cf.getId();

        CapitalFlow.FlowType ft = cf.getFlowType();
        dto.flowType = (ft != null ? ft.name() : null);

        BigDecimal amt = cf.getAmountKrw();
        dto.amountKrw = (amt != null ? amt.longValue() : 0L);

        dto.occurredAt = cf.getOccurredAt();
        dto.memo = cf.getMemo();
        return dto;
    }
}

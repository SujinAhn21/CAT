// src/main/java/com/example/coin/notification/dto/SystemErrorAlertRequestDto.java
// 시스템 오류(거래 봇 / 서버 오류 등) 알림 요청 DTO

package com.example.coin.notification.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 시스템 오류 알림 요청 DTO.
 * - 거래 봇(Python) 또는 서버 내부에서 심각한 오류 발생 시 사용.
 * - NotificationService.sendErrorAlert(...)에 전달할 title/body 생성을 위한 기초 데이터.
 */
@Getter
@Setter
public class SystemErrorAlertRequestDto
{
    /**
     * 오류 심각도.
     * - 예: "INFO", "WARN", "ERROR", "CRITICAL"
     * - null 또는 알 수 없는 값이면 "ERROR"로 취급.
     */
    private String severity;

    /**
     * 오류 코드
     * - 예: "UPBIT_API_ERROR", "ORDER_REJECTED", "BALANCE_EMPTY"
     */
    private String errorCode;

    /**
     * 한 줄 요약 메시지 (선택이지만, 있으면 알림 본문에 사용).
     */
    private String message;

    /**
     * 상세 설명
     * - 필요하다면 여기에 스택 트레이스, 추가 context 등을 넣고,
     *   나중에 BotErrorLog와 연동해서 저장할 수 있음.
     * - 현재 알림 본문에는 사용하지 않지만, 추후 확장 대비 필드만 마련.
     */
    private String detail;
}

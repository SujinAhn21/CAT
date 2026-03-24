// src/main/java/com/example/coin/notification/NotificationController.java
// 디바이스 토큰 등록 + 알림 설정 조회/수정 + 테스트 알림 + 시스템 오류 알림 API

package com.example.coin.notification;

import com.example.coin.auth.UserPrincipal;
import com.example.coin.notification.dto.DeviceTokenRequestDto;
import com.example.coin.notification.dto.NotificationSettingsResponseDto;
import com.example.coin.notification.dto.NotificationSettingsUpdateRequestDto;
import com.example.coin.notification.dto.SystemErrorAlertRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 시나리오 6, 7 관련 API
 * - FCM 디바이스 토큰 등록
 * - 매수/매도/오류 알림 on/off 설정
 * - FCM 테스트 알림
 * - 시스템 오류(거래 봇/서버 오류) 알림
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController
{
    private final NotificationService notificationService;

    /**
     * FCM 디바이스 토큰 등록
     * POST /api/notifications/device-token
     */
    @PostMapping("/device-token")
    public void registerDeviceToken(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                    @RequestBody DeviceTokenRequestDto request)
    {
        notificationService.registerDeviceToken(userPrincipal, request);
    }

    /**
     * 알림 설정 조회
     * GET /api/notifications/settings
     */
    @GetMapping("/settings")
    public NotificationSettingsResponseDto getSettings(@AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        return notificationService.getSettings(userPrincipal);
    }

    /**
     * 알림 설정 수정
     * PUT /api/notifications/settings
     */
    @PutMapping("/settings")
    public NotificationSettingsResponseDto updateSettings(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                          @RequestBody NotificationSettingsUpdateRequestDto request)
    {
        return notificationService.updateSettings(userPrincipal, request);
    }

    /**
     * FCM 테스트 알림 발송
     * POST /api/notifications/test
     *
     * - 브라우저 콘솔에서 sendTestNotification() 호출하거나
     *   Swagger에서 직접 호출해서 실제 푸시가 오는지 확인 용도
     */
    @PostMapping("/test")
    public ResponseEntity<Void> sendTestNotification(@AuthenticationPrincipal UserPrincipal userPrincipal)
    {
        notificationService.sendTestNotification(userPrincipal);
        return ResponseEntity.ok().build();
    }

    /**
     * 시스템 오류 알림 발송 (거래 봇 / 서버 오류 등)
     * POST /api/notifications/system-error
     *
     * - Python 봇 또는 서버 내부 로직에서 오류 발생 시 호출
     * - enableError 설정이 켜져 있어야 실제 FCM 발송
     *
     * 예시 요청 바디:
     * {
     *   "severity": "CRITICAL",
     *   "errorCode": "UPBIT_API_ERROR",
     *   "message": "Upbit 주문 API 500 응답",
     *   "detail": "status=500, body=..."
     * }
     */
    @PostMapping("/system-error")
    public ResponseEntity<Void> sendSystemErrorAlert(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                     @RequestBody SystemErrorAlertRequestDto request)
    {
        Long userId = userPrincipal.getUserId();

        String severity = normalizeSeverity(request.getSeverity());
        String title;
        String body;

        // 심각도에 따라 제목/본문 문구 분기
        switch (severity)
        {
            case "CRITICAL" ->
            {
                // 정말 심각해서 봇을 중단해야 하는 경우
                title = "거래 봇 중단 - 심각한 시스템 오류";
                body = buildBody(request, "시스템을 안전하게 중단했습니다. 확인이 필요합니다.");
            }
            case "WARN" ->
            {
                // 복구 가능성이 있는 경고 수준
                title = "거래 봇 경고 - 오류 발생";
                body = buildBody(request, "자동 복구를 시도 중입니다.");
            }
            case "INFO" ->
            {
                // 정보성 알림
                title = "거래 봇 알림";
                body = buildBody(request, null);
            }
            default ->
            {
                // 기본 ERROR
                title = "거래 봇 오류 발생";
                body = buildBody(request, null);
            }
        }

        // BotErrorLog 연동은 나중에 붙일 수 있도록 일단 null 전달
        notificationService.sendErrorAlert(userId, title, body, null);

        return ResponseEntity.ok().build();
    }

    // -------------------------
    // 내부 헬퍼 메서드들
    // -------------------------

    /**
     * severity 문자열 정규화
     * - null, 공백, 알 수 없는 값 --> "ERROR"
     */
    private String normalizeSeverity(String severity)
    {
        if (severity == null)
        {
            return "ERROR";
        }
        String s = severity.trim().toUpperCase();
        return switch (s)
        {
            case "INFO", "WARN", "ERROR", "CRITICAL" -> s;
            default -> "ERROR";
        };
    }

    /**
     * 알림 메시지 본문 구성
     * - [errorCode] message (suffix)
     */
    private String buildBody(SystemErrorAlertRequestDto request, String suffix)
    {
        StringBuilder sb = new StringBuilder();

        if (request.getErrorCode() != null && !request.getErrorCode().isBlank())
        {
            sb.append("[")
                    .append(request.getErrorCode().trim())
                    .append("] ");
        }

        if (request.getMessage() != null && !request.getMessage().isBlank())
        {
            sb.append(request.getMessage().trim());
        }
        else
        {
            sb.append("시스템 오류가 발생했습니다.");
        }

        if (suffix != null && !suffix.isBlank())
        {
            sb.append(" (")
                    .append(suffix)
                    .append(")");
        }

        return sb.toString();
    }
}

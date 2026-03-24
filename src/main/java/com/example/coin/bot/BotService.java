// src/main/java/com/example/coin/bot/BotService.java

package com.example.coin.bot;

import com.example.coin.auth.UserPrincipal;
import com.example.coin.bot.dto.BotDailyConfigRequestDto;
import com.example.coin.bot.dto.BotErrorLogRequestDto;
import com.example.coin.bot.dto.BotStatusUpdateRequestDto;
import com.example.coin.notification.NotificationService;
import com.example.coin.user.User;
import com.example.coin.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotService
{
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final BotDailyConfigRepository botDailyConfigRepository;
    private final BotStatusRepository botStatusRepository;
    private final BotErrorLogRepository botErrorLogRepository;
    private final NotificationService notificationService;

    /**
     * 일일 설정(best_K, max_ror, skip_today 등) 저장/업데이트.
     * - 하루에 한 번 09:00 직후 Python 봇이 호출하는 것을 가정.
     */
    @Transactional
    public void updateDailyConfig(UserPrincipal principal, BotDailyConfigRequestDto request)
    {
        Long userId = principal.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        LocalDate tradeDate = parseTradeDateOrToday(request.getTradeDate());

        BotDailyConfig config = botDailyConfigRepository
                .findByUserAndTradeDate(user, tradeDate)
                .orElseGet(() -> BotDailyConfig.builder()
                        .user(user)
                        .tradeDate(tradeDate)
                        .build());

        config.setBestK(nonNull(request.getBestK()));
        config.setMaxRor(nonNull(request.getMaxRor()));
        config.setSkipToday(request.getSkipToday() != null && request.getSkipToday());
        config.setLossLimitRate(request.getLossLimitRate());
        config.setStartEquityKrw(request.getStartEquityKrw());

        botDailyConfigRepository.save(config);

        // 상태도 RUNNING + heartbeat 갱신
        BotStatus status = botStatusRepository.findById(userId)
                .orElseGet(() -> BotStatus.builder()
                        .userId(userId)
                        .currentStatus(BotStatus.Status.INIT)
                        .build());

        status.setCurrentStatus(BotStatus.Status.RUNNING);
        status.setLastHeartbeatAt(LocalDateTime.now(KST));
        status.setLastErrorCode(null);
        status.setLastErrorMessage(null);

        botStatusRepository.save(status);
    }

    /**
     * 봇 상태(RUNNING/STOPPED 등) 업데이트.
     */
    @Transactional
    public void updateStatus(UserPrincipal principal, BotStatusUpdateRequestDto request)
    {
        Long userId = principal.getUserId();

        BotStatus status = botStatusRepository.findById(userId)
                .orElseGet(() -> BotStatus.builder()
                        .userId(userId)
                        .currentStatus(BotStatus.Status.INIT)
                        .build());

        BotStatus.Status newStatus = parseStatus(request.getCurrentStatus());
        if (newStatus != null)
        {
            status.setCurrentStatus(newStatus);
        }

        status.setLastHeartbeatAt(LocalDateTime.now(KST));
        status.setLastErrorCode(request.getLastErrorCode());
        status.setLastErrorMessage(request.getLastErrorMessage());

        botStatusRepository.save(status);
    }

    /**
     * 에러 로그 적재 + 필요 시 상태 STOPPED + 오류 알림 발송.
     */
    @Transactional
    public void logError(UserPrincipal principal, BotErrorLogRequestDto request)
    {
        Long userId = principal.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        BotErrorLog.Severity severity = parseSeverity(request.getSeverity());
        if (severity == null)
        {
            severity = BotErrorLog.Severity.ERROR;
        }

        BotErrorLog logEntity = BotErrorLog.builder()
                .user(user)
                .occurredAt(LocalDateTime.now(KST))
                .errorCode(request.getErrorCode())
                .errorMessage(request.getErrorMessage())
                .errorDetail(request.getErrorDetail())
                .severity(severity)
                .handled(false)
                .build();

        botErrorLogRepository.save(logEntity);

        // 오류 알림 전송 (설정에 따라 off 되어 있으면 내부에서 무시)
        try
        {
            String title = "봇 오류 발생";
            String body = String.format("[%s] %s", severity.name(), request.getErrorMessage());
            notificationService.sendErrorAlert(userId, title, body, logEntity);
        }
        catch (Exception e)
        {
            // 알림 실패는 서비스 전체 실패로 보지 않고 로그만 남긴다.
            log.warn("Failed to send bot error notification. userId={}", userId, e);
        }

        // FATAL이면 상태를 STOPPED로 전환
        if (severity == BotErrorLog.Severity.FATAL)
        {
            BotStatus status = botStatusRepository.findById(userId)
                    .orElseGet(() -> BotStatus.builder()
                            .userId(userId)
                            .currentStatus(BotStatus.Status.INIT)
                            .build());

            status.setCurrentStatus(BotStatus.Status.STOPPED);
            status.setLastHeartbeatAt(LocalDateTime.now(KST));
            status.setLastErrorCode(request.getErrorCode());
            status.setLastErrorMessage(request.getErrorMessage());

            botStatusRepository.save(status);
        }
    }

    private LocalDate parseTradeDateOrToday(String tradeDateStr)
    {
        if (tradeDateStr == null || tradeDateStr.trim().isEmpty())
        {
            return LocalDate.now(KST);
        }
        return LocalDate.parse(tradeDateStr.trim());
    }

    private BigDecimal nonNull(BigDecimal value)
    {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BotStatus.Status parseStatus(String status)
    {
        if (status == null)
        {
            return null;
        }
        try
        {
            return BotStatus.Status.valueOf(status.trim().toUpperCase());
        }
        catch (IllegalArgumentException ex)
        {
            return null;
        }
    }

    private BotErrorLog.Severity parseSeverity(String severity)
    {
        if (severity == null)
        {
            return null;
        }
        try
        {
            return BotErrorLog.Severity.valueOf(severity.trim().toUpperCase());
        }
        catch (IllegalArgumentException ex)
        {
            return null;
        }
    }
}

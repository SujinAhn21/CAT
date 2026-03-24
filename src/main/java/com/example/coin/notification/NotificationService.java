// src/main/java/com/example/coin/notification/NotificationService.java
// 디바이스 토큰 등록, 알림 설정 조회/수정, 실제 FCM 발송 + 로그 기록

package com.example.coin.notification;

import com.example.coin.auth.UserPrincipal;
import com.example.coin.bot.BotErrorLog;
import com.example.coin.notification.dto.DeviceTokenRequestDto;
import com.example.coin.notification.dto.NotificationSettingsResponseDto;
import com.example.coin.notification.dto.NotificationSettingsUpdateRequestDto;
import com.example.coin.trade.Trade;
import com.example.coin.trade.TradeSide;
import com.example.coin.user.User;
import com.example.coin.user.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService
{
    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final FirebaseMessaging firebaseMessaging;

    /**
     * FCM 디바이스 토큰 등록/업데이트
     */
    @Transactional
    public void registerDeviceToken(UserPrincipal principal, DeviceTokenRequestDto request)
    {
        Long userId = principal.getUserId();

        if (request.getFcmToken() == null || request.getFcmToken().trim().isEmpty())
        {
            throw new IllegalArgumentException("fcmToken must not be blank");
        }

        String tokenValue = request.getFcmToken().trim();

        DeviceToken.Platform platform = parsePlatformOrDefault(request.getPlatform());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        deviceTokenRepository.findByFcmToken(tokenValue)
                .ifPresentOrElse(existing ->
                {
                    existing.setUser(user);
                    existing.setPlatform(platform);
                    existing.setActive(true);
                    deviceTokenRepository.save(existing);
                }, () ->
                {
                    DeviceToken token = DeviceToken.builder()
                            .user(user)
                            .fcmToken(tokenValue)
                            .platform(platform)
                            .active(true)
                            .build();
                    deviceTokenRepository.save(token);
                });
    }

    /**
     * 현재 로그인한 사용자의 알림 설정 조회
     */
    @Transactional(readOnly = true)
    public NotificationSettingsResponseDto getSettings(UserPrincipal principal)
    {
        Long userId = principal.getUserId();

        // PK = user_id 기준 조회
        return notificationSettingsRepository.findById(userId)
                .map(settings -> new NotificationSettingsResponseDto(
                        settings.isEnableBuy(),
                        settings.isEnableSell(),
                        settings.isEnableError()
                ))
                .orElseGet(() -> new NotificationSettingsResponseDto(
                        true,
                        true,
                        true
                ));
    }

    /**
     * 알림 설정 업데이트
     */
    @Transactional
    public NotificationSettingsResponseDto updateSettings(UserPrincipal principal,
                                                          NotificationSettingsUpdateRequestDto request)
    {
        Long userId = principal.getUserId();

        NotificationSettings settings = notificationSettingsRepository.findById(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        if (request.getEnableBuy() != null)
        {
            settings.setEnableBuy(request.getEnableBuy());
        }
        if (request.getEnableSell() != null)
        {
            settings.setEnableSell(request.getEnableSell());
        }
        if (request.getEnableError() != null)
        {
            settings.setEnableError(request.getEnableError());
        }

        notificationSettingsRepository.save(settings);

        return new NotificationSettingsResponseDto(
                settings.isEnableBuy(),
                settings.isEnableSell(),
                settings.isEnableError()
        );
    }

    /**
     * 매수/매도 체결 시 알림
     * TradeService.saveTrade() 에서 호출 예정
     */
    @Transactional
    public void sendTradeAlert(Trade trade)
    {
        User user = trade.getUser();
        Long userId = user.getId();

        // 알림 설정 확인 (PK = user_id 기준)
        NotificationSettings settings = notificationSettingsRepository.findById(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        TradeSide side = trade.getSide();
        if (side == TradeSide.BUY && !settings.isEnableBuy())
        {
            return; // 매수 알림 꺼져 있으면 발송 X
        }
        if (side == TradeSide.SELL && !settings.isEnableSell())
        {
            return; // 매도 알림 꺼져 있으면 발송 X
        }

        // BigDecimal 포맷팅 시에는 double 변환
        BigDecimal volume = trade.getVolume();
        BigDecimal price  = trade.getPrice();

        double volumeVal = (volume != null ? volume.doubleValue() : 0.0);
        double priceVal  = (price  != null ? price.doubleValue()  : 0.0);

        String title = (side == TradeSide.BUY) ? "매수 체결" : "매도 체결";
        String body = String.format(
                "%s %s, 수량 %.8f BTC, 체결가 %.2f원",
                trade.getSymbol(),
                side.name(),
                volumeVal,
                priceVal
        );

        NotificationLog.Type type =
                (side == TradeSide.BUY) ? NotificationLog.Type.BUY : NotificationLog.Type.SELL;

        sendNotification(user, type, title, body, trade, null);
    }

    /**
     * 오류 발생 시 알림 (봇 에러용, 나중에 BotService에서 사용)
     */
    @Transactional
    public void sendErrorAlert(Long userId, String title, String body, BotErrorLog errorLog)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        NotificationSettings settings = notificationSettingsRepository.findById(userId)
                .orElseGet(() -> createDefaultSettings(userId));

        if (!settings.isEnableError())
        {
            return; // 오류 알림 꺼져 있으면 발송 X
        }

        sendNotification(user, NotificationLog.Type.ERROR, title, body, null, errorLog);
    }

    /**
     * FCM 테스트 알림 (UI 연동 점검용)
     * - 알림 설정(enableBuy/enableSell/enableError)과는 무관하게 항상 발송
     */
    @Transactional
    public void sendTestNotification(UserPrincipal principal)
    {
        Long userId = principal.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        String title = "테스트 알림";
        String body = "Coin Auto Trader FCM 테스트 알림입니다.";

        // 단순 테스트이므로 타입은 BUY로 기록 (의미는 크지 않음)
        sendNotification(user, NotificationLog.Type.BUY, title, body, null, null);
    }

    /**
     * 공통 알림 발송 + notification_logs 기록
     */
    private void sendNotification(User user,
                                  NotificationLog.Type type,
                                  String title,
                                  String body,
                                  Trade relatedTrade,
                                  BotErrorLog relatedError)
    {
        Long userId = user.getId();

        List<DeviceToken> tokens =
                deviceTokenRepository.findAllByUserIdAndActiveIsTrue(userId);

        LocalDateTime now = LocalDateTime.now();

        if (tokens.isEmpty())
        {
            NotificationLog logEntity = NotificationLog.builder()
                    .user(user)
                    .type(type)
                    .title(title)
                    .body(body)
                    .sentAt(now)
                    .success(false)
                    .fcmToken(null)
                    .failureReason("NO_ACTIVE_DEVICE_TOKEN")
                    .relatedTrade(relatedTrade)
                    .relatedError(relatedError)
                    .build();
            notificationLogRepository.save(logEntity);
            return;
        }

        for (DeviceToken token : tokens)
        {
            String tokenValue = token.getFcmToken();
            boolean success;
            String failureReason = null;

            try
            {
                Message message = Message.builder()
                        .setToken(tokenValue)
                        .putData("type", type.name())
                        .putData("title", title)
                        .putData("body", body)
                        .build();

                String response = firebaseMessaging.send(message);
                log.info("FCM sent. userId={}, token={}, response={}", userId, tokenValue, response);
                success = true;
            }
            catch (Exception e)
            {
                log.warn("Failed to send FCM. userId={}, token={}", userId, tokenValue, e);
                success = false;
                failureReason = e.getMessage();
            }

            NotificationLog logEntity = NotificationLog.builder()
                    .user(user)
                    .type(type)
                    .title(title)
                    .body(body)
                    .sentAt(now)
                    .success(success)
                    .fcmToken(tokenValue)
                    .failureReason(failureReason)
                    .relatedTrade(relatedTrade)
                    .relatedError(relatedError)
                    .build();

            notificationLogRepository.save(logEntity);
        }
    }

    // --- 내부 유틸 메서드들 ---

    /**
     * 기본 알림 설정 생성 (모두 on)
     * - PK = user_id
     */
    private NotificationSettings createDefaultSettings(Long userId)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        NotificationSettings settings = new NotificationSettings();
        // @MapsId 덕분에 user 설정 시 user.id 가 PK로 사용됨
        settings.setUser(user);
        settings.setEnableBuy(true);
        settings.setEnableSell(true);
        settings.setEnableError(true);

        return notificationSettingsRepository.save(settings);
    }

    private DeviceToken.Platform parsePlatformOrDefault(String platform)
    {
        if (platform == null)
        {
            return DeviceToken.Platform.WEB;
        }
        try
        {
            return DeviceToken.Platform.valueOf(platform.trim().toUpperCase());
        }
        catch (IllegalArgumentException ex)
        {
            log.warn("Unknown platform '{}', fallback to WEB", platform);
            return DeviceToken.Platform.WEB;
        }
    }
}

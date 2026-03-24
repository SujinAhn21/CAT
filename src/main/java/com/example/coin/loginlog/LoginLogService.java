// src/main/java/com/example/coin/loginlog/LoginLogService.java
// 로그인 성공/실패 이력 저장.

package com.example.coin.loginlog;

import com.example.coin.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 로그인 성공/실패 이력 저장 서비스.
 * 현재는 어디에서도 호출하지 않지만,
 * OAuth2LoginSuccessHandler 등에 붙여서 사용할 수 있도록 분리해두었다.
 */
@Service
@RequiredArgsConstructor
public class LoginLogService
{
    private final LoginLogRepository loginLogRepository;

    public void logSuccess(User user, String ipAddress, String userAgent)
    {
        LoginLog log = LoginLog.builder()
                .user(user)
                .kakaoUserId(user.getKakaoUserId())
                .success(true)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loggedAt(LocalDateTime.now())
                .build();

        loginLogRepository.save(log);
    }

    public void logFailure(Long kakaoUserId, String failureReason, String ipAddress, String userAgent)
    {
        LoginLog log = LoginLog.builder()
                .user(null)
                .kakaoUserId(kakaoUserId)
                .success(false)
                .failureReason(failureReason)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loggedAt(LocalDateTime.now())
                .build();

        loginLogRepository.save(log);
    }
}


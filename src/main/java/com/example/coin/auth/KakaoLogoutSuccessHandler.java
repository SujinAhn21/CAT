// src/main/java/com/example/coin/auth/KakaoLogoutSuccessHandler.java

package com.example.coin.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoLogoutSuccessHandler implements LogoutSuccessHandler
{
    // application.yml 에 있는 client-id 그대로 사용
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    // 카카오 로그아웃 후 돌아올 주소
    // 이 주소를 카카오 개발자 콘솔의 "로그아웃 리다이렉트 URI" 에 등록해야 한다.
    @Value("${app.oauth2.logout-redirect-uri:http://localhost:8080/index.html}")
    private String logoutRedirectUri;

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException
    {
        // 우리 서버 세션/쿠키는 이미 SecurityConfig 쪽에서 정리됨.
        // 여기서는 카카오 로그아웃 URL 로 보내기만 한다.
        String kakaoLogoutUrl = UriComponentsBuilder
                .fromHttpUrl("https://kauth.kakao.com/oauth/logout")
                .queryParam("client_id", kakaoClientId)
                .queryParam("logout_redirect_uri", logoutRedirectUri)
                .build()
                .toUriString();

        log.info("Redirecting to Kakao logout: {}", kakaoLogoutUrl);
        response.sendRedirect(kakaoLogoutUrl);
    }
}

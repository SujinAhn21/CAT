// src/main/java/com/example/coin/auth/OAuth2LoginSuccessHandler.java
// 카카오 로그인 성공 시 화이트리스트 체크 후 JWT 발급 또는 접근 차단 처리.

package com.example.coin.auth;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 카카오 로그인 성공 시:
 *  (1) app.security.allowed-kakao-id (화이트리스트) 체크
 *  (2) 통과하면 JWT 발급 후 /dashboard.html?token=... 으로 리다이렉트
 *  (3) 화이트리스트에 없으면 세션/컨텍스트 비우고 /access-denied.html 로 리다이렉트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * application.yml:
     *
     * app:
     *   security:
     *     allowed-kakao-id: ${ALLOWED_KAKAO_IDS:}
     *
     * .env:
     *   ALLOWED_KAKAO_IDS=4617795482,4618729220
     */
    @Value("${app.security.allowed-kakao-id:}")
    private String allowedKakaoIdsRaw;

    // 파싱된 카카오 ID 집합
    private Set<Long> allowedKakaoIds;

    @PostConstruct
    public void init() {
        if (allowedKakaoIdsRaw == null || allowedKakaoIdsRaw.isBlank()) {
            // 비어 있으면: 화이트리스트 기능 비활성화 (모두 허용)
            allowedKakaoIds = Set.of();
            log.warn("No allowed Kakao IDs configured. Whitelist is disabled (all users allowed).");
        } else {
            try {
                allowedKakaoIds = Arrays.stream(allowedKakaoIdsRaw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .collect(Collectors.toSet());
            } catch (NumberFormatException e) {
                // 형식이 이상하면 애플리케이션이 바로 죽도록 해서 빨리 눈에 띄게 함
                throw new IllegalStateException(
                        "ALLOWED_KAKAO_IDS 형식이 잘못되었습니다: " + allowedKakaoIdsRaw, e
                );
            }
            log.info("Allowed Kakao IDs loaded: {}", allowedKakaoIds);
        }
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long kakaoId = principal.getKakaoUserId();

        boolean whitelistEnabled = !allowedKakaoIds.isEmpty();
        log.info("OAuth2 login success. kakaoId={}, whitelistEnabled={}, allowedIds={}",
                kakaoId, whitelistEnabled, allowedKakaoIds);

        // 1) 화이트리스트 체크
        if (whitelistEnabled && !allowedKakaoIds.contains(kakaoId)) {
            log.warn("Unauthorized Kakao user: {} (not in whitelist {})", kakaoId, allowedKakaoIds);

            // 이 시점에서는 이미 Authentication 이 세션/컨텍스트에 올라가 있을 수 있으므로
            // 여기서 명시적으로 "강제 로그아웃" 처리
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                try {
                    session.invalidate();
                } catch (IllegalStateException ignore) {
                    // 이미 만료된 세션이면 무시
                }
            }

            // 접근 차단 페이지로 리다이렉트
            String denyUrl = "/access-denied.html";
            log.info("Redirecting unauthorized user to {}", denyUrl);
            getRedirectStrategy().sendRedirect(request, response, denyUrl);
            return;
        }

        // 2) 화이트리스트 통과 → JWT 생성 (subject = userId)
        String token = jwtTokenProvider.createToken(principal.getUserId());

        // 토큰은 URL 인코딩해서 쿼리 파라미터에 실어 보냄
        String redirectUrl = "/dashboard.html?token=" +
                URLEncoder.encode(token, StandardCharsets.UTF_8);

        log.info("Redirecting to {}", redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}

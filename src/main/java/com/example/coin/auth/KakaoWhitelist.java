// src/main/java/com/example/coin/auth/KakaoWhitelist.java
// OAuth2LoginSuccessHandler에서 무조건 거치도록 만드는 화이트리스트 클래스


package com.example.coin.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * .env 의 ALLOWED_KAKAO_IDS 를 파싱해서 들고 있다가
 * 로그인 성공 시 현재 kakaoId 가 허용된 계정인지 검사하는 용도.
 */
@Component
public class KakaoWhitelist {

    // 파싱된 카카오 ID 집합
    private final Set<Long> allowedIds;

    /**
     * application.yml 의
     * app.security.allowed-kakao-id: ${ALLOWED_KAKAO_IDS:} 값을 주입받음. 
     */
    public KakaoWhitelist(
            @Value("${app.security.allowed-kakao-id:}") String raw
    ) {
        if (raw == null || raw.isBlank()) {
            // .env 에 아무 값도 없으면: 개발 편의용 "화이트리스트 비활성화" 상태
            this.allowedIds = Collections.emptySet();
        } else {
            // "123456789, 23452345" 같은 문자열을 Set<Long> 으로 변환
            try {
                this.allowedIds = Arrays.stream(raw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .map(Long::parseLong)
                        .collect(Collectors.toSet());
            } catch (NumberFormatException e) {
                // 형식이 잘못되면 애플리케이션이 바로 죽도록 해서 빨리 알아차리게 유도.
                throw new IllegalStateException("ALLOWED_KAKAO_IDS 형식이 잘못되었습니다: " + raw, e);
            }
        }
    }

    /**
     * .env 에 값이 설정되어 있으면 true (즉, 화이트리스트 ON)
     */
    public boolean isWhitelistEnabled() {
        return !allowedIds.isEmpty();
    }

    /**
     * 화이트리스트 기준으로 현재 kakaoId 허용 여부를 반환.
     * - 화이트리스트가 비어 있으면(dev 모드) 모두 허용
     * - 값이 있으면, 그 안에 있을 때만 허용
     */
    public boolean isAllowed(long kakaoId) {
        if (!isWhitelistEnabled()) {
            // dev 모드: 화이트리스트 비활성화 --> 모두 통과
            return true;
        }
        return allowedIds.contains(kakaoId);
    }

    public Set<Long> getAllowedIds() {
        return allowedIds;
    }
}

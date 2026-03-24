// src/main/java/com/example/coin/api/UserMeController.java

package com.example.coin.api;

import com.example.coin.auth.UserPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * JWT + SecurityContext 가 정상동작하는지 확인하는 테스트용 엔드포인트.
 *  - Authorization: Bearer <JWT> 헤더가 있어야 통과한다.
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api")
public class UserMeController
{
    /**
     * GET /api/me
     */
    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal UserPrincipal principal)
    {
        return Map.of(
                "userId", principal.getUserId(),
                "kakaoUserId", principal.getKakaoUserId(),
                "email", principal.getEmail(),
                "nickname", principal.getNickname()
        );
    }
}

// src/main/java/com/example/coin/auth/JwtTokenProvider.java
// JWT 생성/검증, 클레임 파싱.

package com.example.coin.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT 생성/검증 담당
 * jwt.secret / jwt.token-validity-in-seconds 를 application.yml 에서 읽어온다.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.token-validity-in-seconds:3600}")
    private long tokenValidityInSeconds;

    private Key key;

    @PostConstruct
    public void init() {
        // HS256 기준 256bit 이상 필요. .env 의 KAKAO_SECRET 길이를 충분히 길게 잡아야 한다.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // userId(우리 서비스 PK)를 subject 로 넣어서 토큰 생성
    public String createToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidityInSeconds * 1000);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 userId 추출
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    // 유효성 검사 (서명/만료 등)
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}


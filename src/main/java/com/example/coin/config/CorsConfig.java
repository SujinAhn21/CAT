// src/main/java/com/example/coin/config/CorsConfig.java

package com.example.coin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 전역 CORS 설정.
 * - 개발 단계에서는 일단 모든 Origin/헤더/메서드를 허용한다.
 * - Swagger UI, 프론트에서 Authorization 헤더를 같이 던져도 통과시키기 위함.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origin 패턴: 전부 허용 (개발용)
        config.setAllowedOriginPatterns(List.of("*"));

        // 허용 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 허용 헤더
        config.setAllowedHeaders(List.of("*"));

        // Authorization 헤더 등 자격증명 허용
        config.setAllowCredentials(true);

        // preflight 결과 캐시 시간 (초)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로에 CORS 설정 적용
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}

// src/main/java/com/example/coin/config/WebConfig.java

package com.example.coin.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.TimeZone;

/**
 * 공통 웹 설정
 * - CORS 허용 정책
 * - 기본 타임존(KST) 고정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer
{
    /**
     * 개발 단계에서는 모든 origin 을 허용한다.
     * 실제 배포 시에는 프론트 도메인만 명시적으로 허용하는 것이 좋다.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 애플리케이션 전체 기본 타임존을 Asia/Seoul 로 고정.
     * 서비스 전반에서 KST 기준으로 날짜를 다루기 위함.
     */
    @PostConstruct
    public void setupTimeZone()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}

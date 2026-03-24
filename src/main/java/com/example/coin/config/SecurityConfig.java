// src/main/java/com/example/coin/config/SecurityConfig.java

package com.example.coin.config;

import com.example.coin.auth.JwtAuthenticationFilter;
import com.example.coin.auth.KakaoOAuth2UserService;
import com.example.coin.auth.OAuth2LoginSuccessHandler;
import com.example.coin.auth.KakaoLogoutSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig
{
    private final KakaoOAuth2UserService kakaoOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final KakaoLogoutSuccessHandler kakaoLogoutSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
        http
                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // CORS 활성화 (CorsConfig.corsConfigurationSource() 사용)
                .cors(Customizer.withDefaults())

                // 세션은 STATELESS (JWT 기반)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Preflight(OPTIONS) 요청은 전부 허용 (CORS용)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 정적 리소스, 로그인 페이지, Swagger 등은 모두 허용
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/dashboard.html",
                                "/access-denied.html",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/oauth2/**",
                                "/login/**",
                                "/error",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // /api/** 는 JWT 인증 필요
                        .requestMatchers("/api/**").authenticated()

                        // 그 외는 일단 허용
                        .anyRequest().permitAll()
                )

                // Kakao OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/kakao")
                        .userInfoEndpoint(user -> user.userService(kakaoOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(kakaoLogoutSuccessHandler)
                );

        // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 배치
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

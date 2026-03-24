// src/main/java/com/example/coin/config/OpenApiJwtConfig.java

// Swagger/OpenAPI에 JWT 인증 스키마 추가

package com.example.coin.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI) 에 JWT Bearer 인증 스키마를 등록함.
 * - Swagger UI 오른쪽 위 "Authorize" 버튼에서 토큰 입력 가능.
 * - security = bearerAuth 를 기본으로 걸어둠.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Coin Auto Trader API",
                version = "v1"
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiJwtConfig {
}

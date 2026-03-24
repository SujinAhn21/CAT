// src/main/java/com/example/coin/config/SwaggerConfig.java

package com.example.coin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springdoc OpenAPI 추가 설정.
 * JWT 스키마는 OpenApiJwtConfig 에서 정의하고,
 * 여기서는 문서 메타 정보만 설정한다.
 */
@Configuration
public class SwaggerConfig
{
    @Bean
    public OpenAPI coinOpenAPI()
    {
        return new OpenAPI()
                .info(new Info()
                        .title("Coin Auto Trader API")
                        .version("v1")
                        .description("코인 자동매매 봇 백엔드 API 문서"));
    }
}

// src/main/java/com/example/coin/bot/BotController.java
// /api/bot 하위 엔드포인트 (일일 설정, 상태, 에러)

package com.example.coin.bot;

import com.example.coin.auth.UserPrincipal;
import com.example.coin.bot.dto.BotDailyConfigRequestDto;
import com.example.coin.bot.dto.BotErrorLogRequestDto;
import com.example.coin.bot.dto.BotStatusUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Python 봇 → SpringBoot 로 오는 내부용 API
 * (현재는 JWT 인증된 사용자 기준으로 동작하도록 구현)
 */
@RestController
@RequestMapping("/api/bot")
@RequiredArgsConstructor
public class BotController
{
    private final BotService botService;

    @PostMapping("/daily-config")
    public void updateDailyConfig(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                  @RequestBody BotDailyConfigRequestDto request)
    {
        botService.updateDailyConfig(userPrincipal, request);
    }

    @PostMapping("/status")
    public void updateStatus(@AuthenticationPrincipal UserPrincipal userPrincipal,
                             @RequestBody BotStatusUpdateRequestDto request)
    {
        botService.updateStatus(userPrincipal, request);
    }

    @PostMapping("/error")
    public void logError(@AuthenticationPrincipal UserPrincipal userPrincipal,
                         @RequestBody BotErrorLogRequestDto request)
    {
        botService.logError(userPrincipal, request);
    }
}

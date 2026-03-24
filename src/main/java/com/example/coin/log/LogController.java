// src/main/java/com/example/coin/log/LogController.java

package com.example.coin.log;

import com.example.coin.auth.UserPrincipal;
import com.example.coin.log.dto.UnifiedLogPageResponseDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 통합 로그 API
 * GET /api/logs?date=YYYY-MM-DD&size=10&cursor=...&dir=next|prev
 */
@RestController
@RequestMapping("/api/logs")
public class LogController
{
    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public UnifiedLogPageResponseDto getLogs(@AuthenticationPrincipal UserPrincipal principal,
                                             @RequestParam(name = "date", required = false)
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                             @RequestParam(name = "size", defaultValue = "10") int size,
                                             @RequestParam(name = "cursor", required = false) String cursor,
                                             @RequestParam(name = "dir", defaultValue = "next") String dir)
    {
        long userId = principal.getUserId();
        return logService.getLogs(userId, date, size, cursor, dir);
    }
}

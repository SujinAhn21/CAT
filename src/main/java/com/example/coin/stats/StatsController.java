// src/main/java/com/example/coin/stats/StatsController.java
/*
 * GET /api/stats/daily?days=7&endDate=YYYY-MM-DD
 * GET /api/stats/weekly?weeks=4
 */

package com.example.coin.stats;

import com.example.coin.auth.UserPrincipal;
import com.example.coin.stats.dto.DailyStatsResponseDto;
import com.example.coin.stats.dto.WeeklyStatsResponseDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 통계 조회 API
 * - GET /api/stats/daily?days=7&endDate=YYYY-MM-DD
 *   (endDate 생략 시 오늘 기준)
 * - GET /api/stats/weekly?weeks=4
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * 일별 통계
     * 기본 7일, 필요 시 days로 조정 가능.
     * endDate가 있으면 해당 날짜를 끝으로 하는 최근 N일 통계를 조회한다.
     */
    @GetMapping("/daily")
    public List<DailyStatsResponseDto> getDailyStats(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                     @RequestParam(name = "days", defaultValue = "7") int days,
                                                     @RequestParam(name = "endDate", required = false)
                                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return statsService.getDailyStats(userPrincipal, days, endDate);
    }

    /**
     * 주별 통계
     * 기본 4주, 필요 시 weeks로 조정 가능.
     */
    @GetMapping("/weekly")
    public List<WeeklyStatsResponseDto> getWeeklyStats(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                       @RequestParam(name = "weeks", defaultValue = "4") int weeks) {
        return statsService.getWeeklyStats(userPrincipal, weeks);
    }
}

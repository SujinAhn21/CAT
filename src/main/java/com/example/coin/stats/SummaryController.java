// src/main/java/com/example/coin/stats/SummaryController.java
// GET /api/summary: 홈 상단 좌우 박스 데이터 (선택 날짜 기준).

package com.example.coin.stats;

import com.example.coin.auth.UserPrincipal;
import com.example.coin.stats.dto.SummaryResponseDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 홈 상단 요약 API
 * GET /api/summary
 * - 옵션: ?date=YYYY-MM-DD (없으면 오늘 기준)
 */
@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    /**
     * 선택 날짜 기준 요약 조회.
     * - date가 null이면 KST 기준 오늘(LocalDate.now(KST))을 사용한다.
     */
    @GetMapping
    public SummaryResponseDto getSummary(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                         @RequestParam(name = "date", required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return summaryService.getSummary(userPrincipal, date);
    }
}

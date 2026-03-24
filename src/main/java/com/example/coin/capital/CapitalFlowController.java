// src/main/java/com/example/coin/capital/CapitalFlowController.java
/*
 * GET /api/capital-flows/today : 오늘 입출금 로그(페이지네이션)
 * GET /api/capital-flows       : date=YYYY-MM-DD 기준 입출금 로그(페이지네이션)
 */

package com.example.coin.capital;

import com.example.coin.auth.UserPrincipal;
import com.example.coin.capital.dto.CapitalFlowPageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CapitalFlowController
{
    private final CapitalFlowService capitalFlowService;

    @GetMapping("/capital-flows/today")
    public ResponseEntity<CapitalFlowPageResponseDto> getTodayFlows(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size)
    {
        Long userId = principal.getUserId();
        CapitalFlowPageResponseDto response = capitalFlowService.getTodayFlows(userId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/capital-flows")
    public ResponseEntity<CapitalFlowPageResponseDto> getFlowsByDate(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size)
    {
        Long userId = principal.getUserId();
        CapitalFlowPageResponseDto response = capitalFlowService.getFlowsByDate(userId, date, page, size);
        return ResponseEntity.ok(response);
    }
}

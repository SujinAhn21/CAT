// src/main/java/com/example/coin/trade/TradeController.java
/*
* POST /api/trades : Python 봇이 매수/매도 체결 내역 전송.
* GET /api/trades/today : 오늘 매매 기록 조회(페이지네이션).
* GET /api/trades : date=YYYY-MM-DD 기준 특정일 매매 기록 조회.
* */

package com.example.coin.trade;

import com.example.coin.auth.UserPrincipal;
import com.example.coin.trade.dto.TradePageResponseDto;
import com.example.coin.trade.dto.TradeRequestDto;
import com.example.coin.trade.dto.TradeResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 매매 로그 조회/저장용 REST 컨트롤러.
 * - POST /api/trades       : Python 봇이 체결 내역 전송
 * - GET  /api/trades/today : 오늘 매매 기록 조회 (페이지네이션)
 * - GET  /api/trades       : 특정 날짜 매매 기록 조회 (페이지네이션)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TradeController
{
    private final TradeService tradeService;

    /**
     * Python 봇이 체결 내역을 전송하는 엔드포인트.
     * - Authorization: Bearer {JWT} 헤더 필요.
     * - userId 는 JWT 에서 가져온다.
     */
    @PostMapping("/trades")
    public ResponseEntity<TradeResponseDto> createTrade(@AuthenticationPrincipal UserPrincipal principal,
                                                        @RequestBody TradeRequestDto request)
    {
        Long userId = principal.getUserId();
        TradeResponseDto response = tradeService.saveTrade(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 오늘 날짜 기준 매매 기록 조회.
     * - page 는 1부터 시작 (기본값 1), size 기본 10.
     */
    @GetMapping("/trades/today")
    public ResponseEntity<TradePageResponseDto> getTodayTrades(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size)
    {
        Long userId = principal.getUserId();
        TradePageResponseDto response = tradeService.getTodayTrades(userId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 날짜 기준 매매 기록 조회.
     * - date=YYYY-MM-DD
     * - page/size 는 오늘 조회와 동일.
     */
    @GetMapping("/trades")
    public ResponseEntity<TradePageResponseDto> getTradesByDate(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size)
    {
        Long userId = principal.getUserId();
        TradePageResponseDto response = tradeService.getTradesByDate(userId, date, page, size);
        return ResponseEntity.ok(response);
    }
}


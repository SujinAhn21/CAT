// src/main/java/com/example/coin/trade/TradeService.java
// Python 봇이 전송하는 체결 내역 저장, 당일/특정일 조회, DailyPnL 업데이트 트리거.

package com.example.coin.trade;

import com.example.coin.notification.NotificationService;
import com.example.coin.trade.dto.TradePageResponseDto;
import com.example.coin.trade.dto.TradeRequestDto;
import com.example.coin.trade.dto.TradeResponseDto;
import com.example.coin.user.User;
import com.example.coin.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 매매 로그 저장/조회 비즈니스 로직.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService
{
    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // 추가

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * Python 봇이 보낸 체결 정보를 저장.
     */
    @Transactional
    public TradeResponseDto saveTrade(Long userId, TradeRequestDto request)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        LocalDateTime tradeTime = request.getTradeTime();
        if (tradeTime == null) {
            tradeTime = LocalDateTime.now(KST);
        }

        LocalDate tradeDate = tradeTime.atZone(KST).toLocalDate();

        BigDecimal price = nonNull(request.getPrice());
        BigDecimal volume = nonNull(request.getVolume());
        BigDecimal amount = request.getAmountKrw() != null
                ? request.getAmountKrw()
                : price.multiply(volume);

        TradeSide side = TradeSide.valueOf(request.getSide().toUpperCase());

        Trade trade = Trade.builder()
                .user(user)
                .tradeTime(tradeTime)
                .tradeDate(tradeDate)
                .symbol(defaultIfBlank(request.getSymbol(), "KRW-BTC"))
                .side(side)
                .price(price)
                .volume(volume)
                .amountKrw(amount)
                .feeKrw(nonNull(request.getFeeKrw()))
                .pnlKrw(nonNull(request.getPnlKrw()))
                .pnlRate(request.getPnlRate())
                .upbitOrderId(request.getUpbitOrderId())
                .orderType(defaultIfBlank(request.getOrderType(), "MARKET"))
                .rawPayload(request.getRawPayload())
                .createdAt(LocalDateTime.now(KST))
                .build();

        Trade saved = tradeRepository.save(trade);

        // DailyPnL 반영, 통계 업데이트는 나중 단계에서 붙이고
        // 지금은 매수/매도 알림만 먼저 호출한다.
        try
        {
            notificationService.sendTradeAlert(saved);
        }
        catch (Exception e)
        {
            log.warn("Failed to send trade notification. tradeId={}", saved.getId(), e);
        }

        return TradeResponseDto.fromEntity(saved);
    }

    /**
     * 오늘 날짜 기준 매매 내역 페이징 조회.
     */
    @Transactional(readOnly = true)
    public TradePageResponseDto getTodayTrades(Long userId, int page, int size)
    {
        LocalDate todayKst = LocalDate.now(KST);
        return getTradesByDate(userId, todayKst, page, size);
    }

    /**
     * 특정 날짜 기준 매매 내역 페이징 조회.
     */
    @Transactional(readOnly = true)
    public TradePageResponseDto getTradesByDate(Long userId, LocalDate date, int page, int size)
    {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        // page 파라미터는 1-based 로 받고, JPA 에는 0-based 로 전달.
        int pageIndex = Math.max(page - 1, 0);
        PageRequest pageable = PageRequest.of(pageIndex, size);

        Page<Trade> tradePage =
                tradeRepository.findByUserAndTradeDateOrderByTradeTimeAsc(user, date, pageable);

        List<TradeResponseDto> content = tradePage.getContent().stream()
                .map(TradeResponseDto::fromEntity)
                .collect(Collectors.toList());

        return TradePageResponseDto.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(tradePage.getTotalElements())
                .totalPages(tradePage.getTotalPages())
                .last(tradePage.isLast())
                .build();
    }

    private BigDecimal nonNull(BigDecimal value)
    {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String defaultIfBlank(String value, String defaultValue)
    {
        if (value == null) {
            return defaultValue;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? defaultValue : trimmed;
    }
}

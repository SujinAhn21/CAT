// src/main/java/com/example/coin/log/LogService.java

package com.example.coin.log;

import com.example.coin.log.dto.LogCursorDto;
import com.example.coin.log.dto.UnifiedLogItemDto;
import com.example.coin.log.dto.UnifiedLogPageResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * 통합 로그 서비스 (커서 기반)
 */
@Service
public class LogService
{
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final LogJdbcRepository logJdbcRepository;
    private final ObjectMapper objectMapper;

    public LogService(LogJdbcRepository logJdbcRepository,
                      ObjectMapper objectMapper) {
        this.logJdbcRepository = logJdbcRepository;
        this.objectMapper = objectMapper;
    }

    public UnifiedLogPageResponseDto getLogs(long userId,
                                             LocalDate date,
                                             int size,
                                             String cursorStr,
                                             String dir)
    {
        int pageSize = Math.max(1, Math.min(size, 50)); // 과도한 size 방지

        LocalDate today = LocalDate.now(KST);
        LocalDate targetDate = (date != null ? date : today);
        if (targetDate.isAfter(today)) {
            targetDate = today;
        }

        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay().minusNanos(1);

        LogCursorDto cursor = decodeCursor(cursorStr);

        String direction = (dir != null ? dir.trim().toLowerCase() : "next");
        List<UnifiedLogItemDto> items;

        if ("prev".equals(direction)) {
            items = logJdbcRepository.fetchPrevPage(userId, targetDate, start, end, pageSize, cursor);
            // prev는 DESC로 가져오므로 화면 표시(ASC)를 위해 뒤집는다.
            Collections.reverse(items);
        } else {
            items = logJdbcRepository.fetchNextPage(userId, targetDate, start, end, pageSize, cursor);
        }

        if (items == null || items.isEmpty()) {
            return new UnifiedLogPageResponseDto(
                    Collections.emptyList(),
                    null,
                    null,
                    false,
                    false
            );
        }

        UnifiedLogItemDto first = items.get(0);
        UnifiedLogItemDto last = items.get(items.size() - 1);

        String prevCursor = encodeCursor(first);
        String nextCursor = encodeCursor(last);

        // 정확한 hasPrev/hasNext 계산
        boolean hasPrev = logJdbcRepository.existsBefore(userId, targetDate, start, end,
                new LogCursorDto(first.tradeTime, first.kindRank, first.id));
        boolean hasNext = logJdbcRepository.existsAfter(userId, targetDate, start, end,
                new LogCursorDto(last.tradeTime, last.kindRank, last.id));

        return new UnifiedLogPageResponseDto(
                items,
                prevCursor,
                nextCursor,
                hasPrev,
                hasNext
        );
    }

    private String encodeCursor(UnifiedLogItemDto item)
    {
        if (item == null || item.tradeTime == null) {
            return null;
        }

        LogCursorDto c = new LogCursorDto(item.tradeTime, item.kindRank, item.id);

        try {
            String json = objectMapper.writeValueAsString(c);
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            // 커서 인코딩 실패는 치명적이지 않게 null 처리
            return null;
        }
    }

    private LogCursorDto decodeCursor(String cursorStr)
    {
        if (cursorStr == null || cursorStr.trim().isEmpty()) {
            return null;
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursorStr);
            String json = new String(decoded, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, LogCursorDto.class);
        } catch (Exception e) {
            // 잘못된 커서는 그냥 무시하고 첫 페이지처럼 처리
            return null;
        }
    }
}

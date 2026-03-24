// src/main/java/com/example/coin/capital/CapitalFlowService.java

package com.example.coin.capital;

import com.example.coin.capital.dto.CapitalFlowPageResponseDto;
import com.example.coin.capital.dto.CapitalFlowResponseDto;
import com.example.coin.user.User;
import com.example.coin.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 입출금 로그 조회 서비스
 * - 선택 날짜 기준 조회 + 페이지네이션
 */
@Service
@RequiredArgsConstructor
public class CapitalFlowService
{
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final CapitalFlowRepository capitalFlowRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CapitalFlowPageResponseDto getTodayFlows(Long userId, int page, int size)
    {
        LocalDate todayKst = LocalDate.now(KST);
        return getFlowsByDate(userId, todayKst, page, size);
    }

    @Transactional(readOnly = true)
    public CapitalFlowPageResponseDto getFlowsByDate(Long userId, LocalDate date, int page, int size)
    {
        // user 존재 확인(권한/구조 동일하게 맞춤)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

        int pageIndex = Math.max(page - 1, 0);
        PageRequest pageable = PageRequest.of(pageIndex, size);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay().minusNanos(1);

        Page<CapitalFlow> flowPage =
                capitalFlowRepository.findByUser_IdAndOccurredAtBetweenOrderByOccurredAtAsc(
                        user.getId(), start, end, pageable);

        List<CapitalFlowResponseDto> content = flowPage.getContent().stream()
                .map(CapitalFlowResponseDto::fromEntity)
                .collect(Collectors.toList());

        return CapitalFlowPageResponseDto.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(flowPage.getTotalElements())
                .totalPages(flowPage.getTotalPages())
                .last(flowPage.isLast())
                .build();
    }
}

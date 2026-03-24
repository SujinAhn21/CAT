package com.example.coin.capital;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 자본 흐름 조회/집계용 Repository.
 * - 사용자/기간별 입출금 로그 조회
 * - cutoff 시점까지의 누적 입금/출금 합계 계산
 */
public interface CapitalFlowRepository extends JpaRepository<CapitalFlow, Long>
{
    /**
     * (기존 서비스에서 사용 중인 메서드)
     * 특정 사용자, 기간 내 자본 흐름을 occurredAt 오름차순으로 페이징 조회.
     * CapitalFlowService.java 에서 PageRequest를 넘기면 Pageable로 받으면 된다.
     */
    Page<CapitalFlow> findByUser_IdAndOccurredAtBetweenOrderByOccurredAtAsc(Long userId,
                                                                            LocalDateTime start,
                                                                            LocalDateTime end,
                                                                            Pageable pageable);

    /**
     * 특정 사용자, 기간 내의 입출금 로그 조회.
     * 메서드 이름 스타일: userId 사용.
     */
    List<CapitalFlow> findByUserIdAndOccurredAtBetween(Long userId,
                                                       LocalDateTime start,
                                                       LocalDateTime end);

    /**
     * 특정 사용자, 특정 시각 이하의 자본 흐름 전체 조회.
     * 메서드 이름 스타일: user.id 사용 (기존 코드 호환용).
     */
    List<CapitalFlow> findByUser_IdAndOccurredAtLessThanEqual(Long userId,
                                                              LocalDateTime occurredAt);

    /**
     * 특정 사용자, 시각 범위 내 자본 흐름 조회.
     * 메서드 이름 스타일: user.id 사용 (기존 코드 호환용).
     */
    List<CapitalFlow> findByUser_IdAndOccurredAtBetween(Long userId,
                                                        LocalDateTime start,
                                                        LocalDateTime end);

    /**
     * cutoff 시점까지의 누적 입금/출금 합계.
     * flowType 은 CapitalFlow.FlowType 의 DEPOSIT / WITHDRAWAL 값.
     */
    @Query("select coalesce(sum(cf.amountKrw), 0) " +
            "from CapitalFlow cf " +
            "where cf.user.id = :userId " +
            "and cf.flowType = :flowType " +
            "and cf.occurredAt <= :cutoff")
    BigDecimal sumAmountByUserAndFlowTypeUpTo(@Param("userId") Long userId,
                                              @Param("flowType") CapitalFlow.FlowType flowType,
                                              @Param("cutoff") LocalDateTime cutoff);

    /**
     * 특정 기간 내(선택 날짜 하루 등) 입금/출금 합계.
     * - 브리핑보드의 "오늘(=선택 날짜) 입금액/출금액" 계산용.
     */
    @Query("select coalesce(sum(cf.amountKrw), 0) " +
            "from CapitalFlow cf " +
            "where cf.user.id = :userId " +
            "and cf.flowType = :flowType " +
            "and cf.occurredAt between :start and :end")
    BigDecimal sumAmountByUserAndFlowTypeBetween(@Param("userId") Long userId,
                                                 @Param("flowType") CapitalFlow.FlowType flowType,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);
}

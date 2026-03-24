// src/main/java/com/example/coin/trade/TradeRepository.java

package com.example.coin.trade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    /**
     * 특정 사용자, 특정 날짜의 매매 내역을 페이지로 조회
     * (당일 / 특정일 상세 조회용)
     */
    Page<Trade> findByUserAndTradeDateOrderByTradeTimeAsc(com.example.coin.user.User user,
                                                          LocalDate tradeDate,
                                                          Pageable pageable);

    /**
     * 특정 사용자, 특정 날짜의 매매 내역 전체 조회 (통합 로그용)
     */
    List<Trade> findByUserIdAndTradeDateOrderByTradeTimeAsc(Long userId,
                                                            LocalDate tradeDate);

    /**
     * 특정 사용자, 날짜 범위 내의 모든 매매 내역 조회
     * (일별/주별 통계용)
     */
    List<Trade> findByUserIdAndTradeDateBetween(Long userId,
                                                LocalDate startDate,
                                                LocalDate endDate);

    /**
     * 특정 사용자, 특정 날짜의 실현 손익 합계
     * summary API에서 "해당 날짜 하루 손익" 계산용.
     */
    @Query("select coalesce(sum(t.pnlKrw), 0) " +
            "from Trade t " +
            "where t.user.id = :userId and t.tradeDate = :tradeDate")
    BigDecimal sumPnlByUserAndTradeDate(@Param("userId") Long userId,
                                        @Param("tradeDate") LocalDate tradeDate);

    /**
     * 특정 사용자의 전체 기간 누적 실현 손익 합계
     * (기존: 오늘 기준 전체 누적 손익)
     */
    @Query("select coalesce(sum(t.pnlKrw), 0) " +
            "from Trade t " +
            "where t.user.id = :userId")
    BigDecimal sumTotalPnlByUser(@Param("userId") Long userId);

    /**
     * 특정 사용자, 기준 날짜(tradeDate)까지의 누적 실현 손익 합계
     * - Summary에서 "선택한 날짜 기준 누적 손익" 계산용.
     */
    @Query("select coalesce(sum(t.pnlKrw), 0) " +
            "from Trade t " +
            "where t.user.id = :userId and t.tradeDate <= :tradeDate")
    BigDecimal sumPnlByUserUpToDate(@Param("userId") Long userId,
                                    @Param("tradeDate") LocalDate tradeDate);
}

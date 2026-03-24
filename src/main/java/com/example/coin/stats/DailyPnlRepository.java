// src/main/java/com/example/coin/stats/DailyPnlRepository.java


package com.example.coin.stats;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 일별 손익 요약 레포지토리.
 * 아직 직접 사용하지는 않지만 향후 배치/통계에 활용한다.
 */
public interface DailyPnlRepository extends JpaRepository<DailyPnl, Long>
{
}

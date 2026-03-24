// src/main/java/com/example/coin/bot/BotDailyConfigRepository.java

package com.example.coin.bot;

import com.example.coin.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface BotDailyConfigRepository extends JpaRepository<BotDailyConfig, Long> {

    Optional<BotDailyConfig> findByUserAndTradeDate(User user, LocalDate tradeDate);

    @Query("select c from BotDailyConfig c " +
            "where c.user.id = :userId and c.tradeDate = :tradeDate")
    Optional<BotDailyConfig> findByUserIdAndTradeDate(@Param("userId") Long userId,
                                                      @Param("tradeDate") LocalDate tradeDate);

    /**
     * 사용자의 '원금'으로 사용할 최초 start_equity_krw 레코드 조회.
     * - start_equity_krw 가 null 이 아닌 것들 중 trade_date 기준 가장 이른 1건
     */
    Optional<BotDailyConfig> findTopByUserIdAndStartEquityKrwIsNotNullOrderByTradeDateAsc(Long userId);
}

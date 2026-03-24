// src/main/java/com/example/coin/bot/BotErrorLogRepository.java

package com.example.coin.bot;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BotErrorLogRepository extends JpaRepository<BotErrorLog, Long>
{
}

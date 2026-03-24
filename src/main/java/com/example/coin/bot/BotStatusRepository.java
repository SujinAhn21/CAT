// src/main/java/com/example/coin/bot/BotStatusRepository.java

package com.example.coin.bot;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BotStatusRepository extends JpaRepository<BotStatus, Long>
{
    // PK = userId 이므로 기본 제공되는 findById(userId) 그대로 쓰면 된다.
}

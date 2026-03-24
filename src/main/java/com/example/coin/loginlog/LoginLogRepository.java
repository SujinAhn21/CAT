// src/main/java/com/example/coin/loginlog/LoginLogRepository.java


package com.example.coin.loginlog;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 로그인 로그 레포지토리.
 */
public interface LoginLogRepository extends JpaRepository<LoginLog, Long>
{
}

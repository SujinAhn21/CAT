// src/main/java/com/example/coin/user/UserService.java
// 카카오 user 정보 저장/조회, 화이트리스트용 ALLOWED_KAKAO_ID 비교 로직.

package com.example.coin.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 사용자 도메인 서비스.
 * 현재는 Repository thin wrapper 수준이지만,
 * 추후 사용자 관련 비즈니스 로직을 여기에 모을 수 있다.
 */
@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;

    public Optional<User> findById(Long id)
    {
        return userRepository.findById(id);
    }

    public Optional<User> findByKakaoUserId(Long kakaoUserId)
    {
        return userRepository.findByKakaoUserId(kakaoUserId);
    }

    public User save(User user)
    {
        return userRepository.save(user);
    }
}

// src/main/java/com/example/coin/auth/KakaoOAuth2UserService.java
// Kakao /v2/user/me 응답 파싱, users 테이블과 매핑. (Kakao id 추출 + users 저장)

package com.example.coin.auth;

import com.example.coin.user.User;
import com.example.coin.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 카카오에서 내려온 유저 정보를 받아
 * users 테이블에 upsert 하고 UserPrincipal 로 감싸서 반환.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        Long kakaoId = ((Number) attributes.get("id")).longValue();


        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = kakaoAccount != null ?
                (Map<String, Object>) kakaoAccount.get("profile") : null;

        String nickname = profile != null ? (String) profile.get("nickname") : null;
        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;

        log.info("KAKAO LOGIN: id={}, email={}, nickname={}", kakaoId, email, nickname); // kakaoId를 로그로 찍으려고 추가.

        // DB에 사용자 upsert. 기존 유저 있으면 가져오고, 없으면 새로 생성
        User user = userRepository.findByKakaoUserId(kakaoId)
                        .orElseGet(User::new);
        user.setKakaoUserId(kakaoId);
        user.setKakaoNickname(nickname);
        user.setKakaoEmail(email);

        User saved = userRepository.save(user); //수정함.

        // UserPrincipal 로 감싸서 반환 (OAuth2LoginSuccessHandler 에서 사용)
        return UserPrincipal.create(saved, attributes);
    }
}


// src/main/java/com/example/coin/auth/UserPrincipal.java
// 인증된 사용자 정보 표현(userId, kakaoId 등).

package com.example.coin.auth;

import com.example.coin.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Getter
public class UserPrincipal implements OAuth2User, UserDetails {

    private final Long userId; // 우리 서비스 내부 user PK
    private final Long kakaoUserId; // 카카오에서 내려온 numeric ID
    private final String email;
    private final String nickname;
    private final Collection<? extends GrantedAuthority> authorities;  // ROLE_USER 같은 권한
    private final Map<String, Object> attributes; // Kakao raw attributes (필요 시 사용)

    private UserPrincipal(Long userId,
                          Long kakaoUserId,
                          String email,
                          String nickname,
                          Collection<? extends GrantedAuthority> authorities,
                          Map<String, Object> attributes) {
        this.userId = userId;
        this.kakaoUserId = kakaoUserId;
        this.email = email;
        this.nickname = nickname;
        this.authorities = authorities;
        this.attributes = attributes != null ? attributes : Collections.emptyMap();
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        List<GrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        return new UserPrincipal(
                user.getId(),
                user.getKakaoUserId(),
                user.getKakaoEmail(),
                user.getKakaoNickname(),
                authorities,
                attributes
        );
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }

    // UserDetails 구현부 (비밀번호 로그인은 안 쓰지만 형식상 구현)
    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(userId);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}


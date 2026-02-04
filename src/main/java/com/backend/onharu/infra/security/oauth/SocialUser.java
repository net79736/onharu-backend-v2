package com.backend.onharu.infra.security.oauth;

import com.backend.onharu.domain.user.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * OAuth2 인증용 사용자 상제 정보 구현체 입니다
 * <p>
 * 소셜 로그인 성공 후, SecurityContext 에 저장될 인증 객체 입니다.
 * </p>
 */
@Getter
@RequiredArgsConstructor
public class SocialUser implements OAuth2User {

    /**
     * 사용자 도메인(User) 엔티티
     */
    private final User user;

    /**
     * 소셜 로그인 제공자(OAuth2 제공자)로부터 받은 속성 목록
     */
    private final Map<String, Object> attributes;

    /**
     * attributes 의 Getter
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * 사용자가 가진 권한 목록을 반환합니다.
     *
     * @return ROLE_사용자유형 으로 작성된 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getUserType().name())
        );
    }

    /**
     * 시큐리티가 사용하는 고유 식별자 반환
     *
     * @return 사용자 ID (String 반환)
     */
    @Override
    public String getName() {
        return user.getId().toString();
    }
}

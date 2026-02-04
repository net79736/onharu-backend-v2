package com.backend.onharu.infra.security;

import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.user.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 인증에 사용될 사용자 상세 정보 입니다.
 * <p>
 * User 엔티티를 시큐리티가 이해할 수 있도록 변환합니다.
 */
@Getter
@RequiredArgsConstructor
public class LocalUser implements UserDetails {

    /**
     * 인증 성공후 SecurityContext 에서 꺼내서 사용 가능한 User 엔티티 입니다
     */
    private final User user;

    /**
     * 시큐리티가 사용하는 고유 식별자 반환
     *
     * @return 사용자 ID
     */
    @Override
    public String getUsername() {
        return user.getLoginId();
    }

    /**
     * 인증 과정에서 입력한 사용자 비밀번호 반환
     *
     * @return 비밀번호
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자가 가진 권한 목록을 반환합니다.
     *
     * @return ROLE_사용자유형 으로 작성된 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name()));
    }

    /**
     * 사용자 계정 만료 여부 반환
     *
     * @return true: 로그인 가능 상태, false: 사용자 계정 상태가 DELETED(삭제 상태)인 경우
     */
    @Override
    public boolean isAccountNonExpired() { // 유효 기간 여부
        return !user.getStatusType().equals(StatusType.DELETED);
    }

    /**
     * 사용자 계정 잠금 여부 반환
     *
     * @return true: 로그인 가능 상태, false: 사용자 계정 상태가 LOCKED(잠금 상태), BLOCKED(차단 상태)인 경우
     */
    @Override
    public boolean isAccountNonLocked() { // 잠김 여부
        StatusType status = user.getStatusType();

        return status != StatusType.LOCKED && status != StatusType.BLOCKED;
    }

    /**
     * 사용자 비밀번호 만료 여부 반환
     *
     * @return true: 비밀번호 사용 가능한 상태, false: 비밀번호 만료된 상태이므로 비밀번호 변경 필요
     */
    @Override
    public boolean isCredentialsNonExpired() { // 비밀번호 만료 여부
        return true;
    }

    /**
     * 사용가능한 계정 여부 반환
     *
     * @return true: 사용자 계정 상태가 PENDING(대기), ACTIVE(활성) 상태인 경우 계정 활성화 상태, false:  PENDING(대기), ACTIVE(활성) 이외의 상태인 경우 계정 비활성화
     */
    @Override
    public boolean isEnabled() { // 활성화 여부
        StatusType status = user.getStatusType();

        return status.equals(StatusType.ACTIVE) || status.equals(StatusType.PENDING);
    }
}

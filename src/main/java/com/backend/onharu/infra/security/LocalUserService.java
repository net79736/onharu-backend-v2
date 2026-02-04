package com.backend.onharu.infra.security;

import com.backend.onharu.domain.user.dto.UserQuery.GetUserByLoginIdQuery;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 스프링 시큐리티의 로그인 처리 서비스 입니다
 * <p>
 * 인증 과정에서 사용자 정보를 DB 에서 조회할 수 있도록 연결해줍니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalUserService implements UserDetailsService {

    /**
     * 사용자(User) 도메인 조회 서비스
     */
    private final UserQueryService userQueryService;

    /**
     * 시큐리티가 사용하는 고유 식별자를 이용해 DB 에서 사용자 정보를 조회합니다.
     *
     * @param username 로그인 아이디(loginId)
     * @return LocalUser 객체
     * @throws UsernameNotFoundException 로그인 아이디에 해당하는 사용자를 찾을 수 없는 경우
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername 호출: {}", username);

        User user = userQueryService.getUserByLoginId(new GetUserByLoginIdQuery(username)); // 사용자 User 조회

        return new LocalUser(user); // 사용자 엔티티(User)를 시큐리티 객체(LocalUser)로 변환하여 반환합니다
    }
}

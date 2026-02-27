package com.backend.onharu.infra.security;

import com.backend.onharu.application.UserFacade;
import com.backend.onharu.application.dto.UserLogin;
import com.backend.onharu.domain.child.dto.ChildQuery;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.service.ChildQueryService;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.owner.dto.OwnerQuery;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.service.OwnerQueryService;
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

    private final UserFacade userFacade;

    private final UserQueryService userQueryService;
    private final ChildQueryService childQueryService;
    private final OwnerQueryService ownerQueryService;

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

        User user = userFacade.getUser(new GetUserByLoginIdQuery(username));// 사용자 User 조회
        UserLogin userLogin = userFacade.divideUserType(user); // 사용자 타입별 조회

        return new LocalUser(userLogin.user(), userLogin.domainId()); // 사용자 엔티티(User)를 시큐리티 객체(LocalUser)로 변환하여 반환합니다
    }
}

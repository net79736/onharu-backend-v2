package com.backend.onharu.infra.db.user;

import com.backend.onharu.domain.user.model.UserOAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 소셜 사용자 JPA Repository
 */
public interface UserOAuthJpaRepository extends JpaRepository<UserOAuth, Long> {

    /**
     * 소셜 제공자의 고유 식별값으로 소셜 로그인 유저를 조회합니다.
     *
     * @param providerId 소셜 로그인 제공자의 고유 식별값
     * @return 소셜 로그인 유저 객체
     */
    Optional<UserOAuth> getUserOAuthByProviderId(String providerId);
}
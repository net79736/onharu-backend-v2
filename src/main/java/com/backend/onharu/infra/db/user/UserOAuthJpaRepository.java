package com.backend.onharu.infra.db.user;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.user.model.UserOAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 소셜 사용자 JPA Repository
 */
public interface UserOAuthJpaRepository extends JpaRepository<UserOAuth, Long> {

    /**
     * 소셜 사용자의 소셜 로그인 타입과 식별값으로 소셜 사용자를 조회합니다.
     *
     * @param providerType 소셜 로그인 타입
     * @param providerId   소셜 로그인 식별값
     * @return 조회된 소셜 사용자 엔티티
     */
    Optional<UserOAuth> getUserOAuthByProviderTypeAndProviderId(ProviderType providerType, String providerId);
}
package com.backend.onharu.domain.user.repository;

import com.backend.onharu.domain.user.dto.UserOAuthRepositoryParam.GetUserOAuthByIdParam;
import com.backend.onharu.domain.user.dto.UserOAuthRepositoryParam.GetUserOAuthByProviderIdParam;
import com.backend.onharu.domain.user.model.UserOAuth;

import java.util.Optional;

/**
 * 소셜 사용자 Repository 인터페이스
 */
public interface UserOAuthRepository {

    /**
     * 소셜 사용자를 저장합니다.
     *
     * @param userOAuth 소셜 사용자 엔티티
     * @return 저장된 소셜 사용자 엔티티
     */
    UserOAuth save(UserOAuth userOAuth);

    /**
     * 소셜 사용자 ID로 소셜 사용자를 조회합니다.
     */
    Optional<UserOAuth> getUserOAuth(GetUserOAuthByIdParam param);

    /**
     * 소셜 제공자의 식별값으로 소셜 사용자를 조회합니다.
     */
    Optional<UserOAuth> getUserOAuthByProviderId(GetUserOAuthByProviderIdParam param);
}
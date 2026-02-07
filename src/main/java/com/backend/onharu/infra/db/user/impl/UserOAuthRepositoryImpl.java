package com.backend.onharu.infra.db.user.impl;

import com.backend.onharu.domain.user.dto.UserOAuthRepositoryParam.GetUserOAuthByIdParam;
import com.backend.onharu.domain.user.dto.UserOAuthRepositoryParam.GetUserOAuthByProviderIdParam;
import com.backend.onharu.domain.user.model.UserOAuth;
import com.backend.onharu.domain.user.repository.UserOAuthRepository;
import com.backend.onharu.infra.db.user.UserOAuthJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 소셜 사용자 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class UserOAuthRepositoryImpl implements UserOAuthRepository {

    private final UserOAuthJpaRepository userOAuthJpaRepository;

    @Override
    public UserOAuth save(UserOAuth userOAuth) {
        return userOAuthJpaRepository.save(userOAuth);
    }

    @Override
    public Optional<UserOAuth> getUserOAuth(GetUserOAuthByIdParam param) {
        return userOAuthJpaRepository.findById(param.id());
    }

    @Override
    public Optional<UserOAuth> getUserOAuthByProviderId(GetUserOAuthByProviderIdParam param) {
        return userOAuthJpaRepository.getUserOAuthByProviderId(param.providerId());
    }
}
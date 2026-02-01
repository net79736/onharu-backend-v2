package com.backend.onharu.infra.db.user.impl;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.user.dto.UserOAuthRepositoryParam.GetUserOAuthByIdParam;
import com.backend.onharu.domain.user.dto.UserOAuthRepositoryParam.GetUserOAuthByProviderTypeAndProviderIdParam;
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
    public UserOAuth getUserOAuth(GetUserOAuthByIdParam param) {
        return userOAuthJpaRepository.findById(param.id())
                .orElseThrow(() -> new CoreException(ErrorType.UserOAuth.USER_O_AUTH_NOT_FOUND));
    }

    @Override
    public Optional<UserOAuth> getUserOAuthByProviderTypeAndProviderId(GetUserOAuthByProviderTypeAndProviderIdParam param) {
        return userOAuthJpaRepository.getUserOAuthByProviderTypeAndProviderId(param.providerType(), param.providerId());
    }
}
package com.backend.onharu.domain.user.service;

import com.backend.onharu.domain.user.dto.UserOAuthQuery.GetUserByUserOAuthQuery;
import com.backend.onharu.domain.user.dto.UserOAuthRepositoryParam.GetUserOAuthByProviderIdParam;
import com.backend.onharu.domain.user.model.UserOAuth;
import com.backend.onharu.domain.user.repository.UserOAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 소셜 사용자 Query Service
 * <p>
 * 소셜 사용자 도메인의 상태를 조회하는 비즈니스 로직을 처리하는 서비스 입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserOAuthQueryService {

    private final UserOAuthRepository userOAuthRepository;

    /**
     * 소셜 사용자 조회
     */
    public Optional<UserOAuth> getUserByUserOAuthQuery(GetUserByUserOAuthQuery query) {
        return userOAuthRepository.getUserOAuthByProviderId(
                new GetUserOAuthByProviderIdParam(
                        query.providerId()
                )
        );
    }
}
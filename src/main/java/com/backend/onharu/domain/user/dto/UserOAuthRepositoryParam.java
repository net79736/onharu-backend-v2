package com.backend.onharu.domain.user.dto;

import com.backend.onharu.domain.common.enums.ProviderType;

/**
 * 소셜 사용자 Repository 파라미터
 * <p>
 * Repository 인터페이스에서 사용되는 파라미터를 정의합니다.
 * Repository 구현체에서 JPA 쿼리 메서드에 전달되는 파라미터 입니다.
 */
public class UserOAuthRepositoryParam {

    /**
     * 소셜 사용자 ID 조회 파라미터
     *
     * @param id 조회할 소셜 사용자 ID
     */
    public record GetUserOAuthByIdParam(
            Long id
    ) {
    }

    /**
     * 소셜 타입별 소셜 사용자 조회 파라미터
     *
     * @param providerType 소셜 타입(예: KAKAO)
     * @param providerId   소셜 사용자 식별값
     */
    public record GetUserOAuthByProviderTypeAndProviderIdParam(
            ProviderType providerType,
            String providerId
    ) {
    }
}
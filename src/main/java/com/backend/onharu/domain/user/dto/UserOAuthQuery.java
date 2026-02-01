package com.backend.onharu.domain.user.dto;

import com.backend.onharu.domain.common.enums.ProviderType;

/**
 * 소셜 사용자 관련 Query DTO
 * <p>
 * 소셜 사용자 도메인에서 사용되는 Query 패턴의 DTO 를 정의합니다.
 * Query 는 도메인 모델의 상태를 조회하는 작업을 나타냅니다.
 */
public class UserOAuthQuery {

    /**
     * 소셜 사용자 계정 조회
     *
     * @param providerType 소셜 제공자 종류(예: KAKAO)
     * @param providerId   소셜 제공자 고유 번호
     */
    public record GetUserByUserOAuthQuery(
            ProviderType providerType,
            String providerId
    ) {
    }
}
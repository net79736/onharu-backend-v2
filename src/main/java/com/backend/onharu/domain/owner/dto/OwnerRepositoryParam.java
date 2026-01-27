package com.backend.onharu.domain.owner.dto;

/**
 * 사업자 Repository 파라미터
 * 
 * Repository 인터페이스에서 사용되는 파라미터를 정의합니다.
 */
public class OwnerRepositoryParam {

    /**
     * 사용자 ID로 조회하는 파라미터
     */
    public record GetOwnerByUserIdParam(
            Long userId
    ) {
    }

    /**
     * 로그인 ID로 조회하는 파라미터
     */
    public record GetOwnerByLoginIdParam(
            String loginId
    ) {
    }
}

package com.backend.onharu.domain.owner.service;

/**
 * 아동 Repository 파라미터
 */
public class OwnerRepositoryParam {
    /**
     * 아동 ID로 조회하는 파라미터
     */
    public record GetOwnerByIdParam(
            Long id
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

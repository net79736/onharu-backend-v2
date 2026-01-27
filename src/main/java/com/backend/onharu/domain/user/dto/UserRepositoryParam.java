package com.backend.onharu.domain.user.dto;

/**
 * 사용자 Repository 파라미터
 * 
 * Repository 인터페이스에서 사용되는 파라미터를 정의합니다.
 * Repository 구현체에서 JPA 쿼리 메서드에 전달되는 파라미터입니다.
 */
public class UserRepositoryParam {

    /**
     * 사용자 ID로 조회하는 파라미터
     */
    public record GetUserByIdParam(
            Long id
    ) {
    }

    /**
     * 로그인 ID로 조회하는 파라미터
     */
    public record GetUserByLoginIdParam(
            String loginId
    ) {
    }
}

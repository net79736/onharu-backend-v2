package com.backend.onharu.domain.level.dto;

/**
 * 등급 Repository 파라미터
 * <p>
 * 도메인 계층의 Repository 인터페이스에서 사용되는 파라미터를 정의합니다.
 * Repository 구현체에서 JPA 쿼리 메서드에 전달되는 파라미터입니다.
 */
public class LevelRepositoryParam {

    /**
     * 등급 ID로 조회하는 파라미터
     *
     * @param id 등급 식별키
     */
    public record GetLevelByIdParam(
            Long id
    ) {
    }

    /**
     * 등급명으로 조회하는 파라미터
     *
     * @param name 등급명
     */
    public record GetLevelByNameParam(
            String name
    ) {
    }

    /**
     * 등급 수정 파라미터
     *
     * @param name 등급명
     * @param id   등급 ID
     */
    public record UpdateNameByIdParam(
            String name,
            Long id
    ) {
    }
}

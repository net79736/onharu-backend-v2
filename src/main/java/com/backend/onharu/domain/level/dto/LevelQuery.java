package com.backend.onharu.domain.level.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;

/**
 * 등급 관련 Query DTO
 *
 * 등급 도메인에서 사용되는 Query 패턴의 DTO 를 정의합니다.
 * Query 는 도메인 모델의 상태를 조회하는 작업을 나타냅니다.
 */
public class LevelQuery {

    /**
     * 등급 ID로 조회하는 Query
     */
    public record GetLevelByIdQuery(
            @NotBlank Long id
    ) {
    }

    /**
     * 등급명으로 조회하는 Query
     */
    public record GetLevelByNameQuery(
            @NotBlank @Max(30) String name
    ) {
    }
}

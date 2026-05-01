package com.backend.onharu.domain.level.dto;

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
            Long id
    ) {
    }

    /**
     * 등급명으로 조회하는 Query
     */
    public record GetLevelByNameQuery(
            String name
    ) {
    }

    /**
     * 등급 조건 횟수로 다음 등급을 조회하는 Query
     * @param distributionCount 현재 사업자의 나눔 횟수 (Owner 의 distributionCount)
     */
    public record FindFirstByConditionNumberQuery(
            int distributionCount
    ) {
    }
}

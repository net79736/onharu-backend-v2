package com.backend.onharu.domain.level.dto;

/**
 * 등급 관련 Command DTO
 */
public class LevelCommand {

    /**
     * 등급 생성 Command
     */
    public record CreateLevelCommand(
            String name,
            int conditionNumber
    ) {
    }

    /**
     * 등급 수정 Command
     *
     * @param id   등급 ID
     * @param name 등급명
     * @param conditionNumber 등급 조건 횟수
     */
    public record UpdateNameByIdCommand(
            Long id,
            String name,
            int conditionNumber
    ) {
    }

    /**
     * 등급 삭제 Command
     * @param levelId 삭제할 등급 ID
     */
    public record DeleteLevelCommand(
            Long levelId
    ) {
    }
}

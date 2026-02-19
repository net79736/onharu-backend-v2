package com.backend.onharu.domain.level.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 등급 관련 Command DTO
 */
public class LevelCommand {

    /**
     * 등급 생성 Command
     */
    public record CreateLevelCommand(
            @NotBlank @Size(max = 30) String name
    ) {
    }

    /**
     * 등급 수정 Command
     *
     * @param name 등급명
     * @param id   등급 ID
     */
    public record UpdateNameByIdCommand(
            String name,
            Long id
    ) {
    }
}

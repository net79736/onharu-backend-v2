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
}

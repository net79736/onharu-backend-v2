package com.backend.onharu.interfaces.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LevelControllerDto {

    public record GetLevelResponse(
            LevelResponse level
    ) {
    }

    public record LevelResponse(
            @Schema(description = "등급 ID", example = "1")
            Long id,

            @Schema(description = "등급명", example = "새싹")
            String name
    ) {
    }

    /**
     * 등급 생성 요청 DTO
     */
    public record CreateLevelRequest(
            @NotBlank(message = "등급명은 필수입니다.")
            @Size(max = 30, message = "등급명은 30자 이내여야 합니다.")
            @Schema(description = "등급명", example = "새싹", maxLength = 30)
            String name
    ) {
    }

    /**
     * 등급 생성 응답 DTO
     */
    public record CreateLevelResponse(
            @Schema(description = "등급 ID", example = "1")
            Long id
    ) {
    }
}

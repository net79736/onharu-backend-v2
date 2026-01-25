package com.backend.onharu.interfaces.api.dto;

import com.backend.onharu.domain.common.enums.UserType;

import io.swagger.v3.oas.annotations.media.Schema;

public class UserControllerDto {

    public record CreateUserRequest(
            @Schema(description = "사용자 유형", example = "CHILD", allowableValues = {"CHILD", "OWNER", "ADMIN"})
            UserType userType,

            @Schema(description = "비밀번호", example = "password123!")
            String password,

            @Schema(description = "비밀번호 확인", example = "password123!")
            String passwordConfirm,

            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone
    ) {
    }

    public record CreateUserResponse(
            @Schema(description = "사용자 ID", example = "1")
            Long userId
    ) {
    }

    public record GetUserResponse(
            UserResponse user
    ) {
    }

    public record UpdateUserRequest(
            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone
    ) {
    }

    public record UserResponse(
            @Schema(description = "사용자 ID", example = "1")
            Long id,

            @Schema(description = "역할 ID", example = "1")
            String roleId,

            @Schema(description = "로그인 ID", example = "user123")
            String loginId,

            @Schema(description = "사용자 유형", example = "CHILD", allowableValues = {"CHILD", "OWNER", "ADMIN"})
            UserType userType,

            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @Schema(description = "상태", example = "ACTIVE", allowableValues = {"ACTIVE", "DELETED", "BLOCKED"})
            String status
    ) {
    }
}

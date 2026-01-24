package com.backend.onharu.interfaces.api.dto;

import com.backend.onharu.domain.common.enums.StatusType;

import io.swagger.v3.oas.annotations.media.Schema;

public class OwnerControllerDto {

    public record CreateOwnerRequest(
            @Schema(description = "가게 사장 사용자 ID", example = "child123")
            String userId,

            @Schema(description = "비밀번호", example = "password123!")
            String password,

            @Schema(description = "비밀번호 확인", example = "password123!")
            String passwordConfirm,

            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @Schema(description = "사업자 번호", example = "1234567890")
            String businessNumber
    ) {
    }

    public record CreateOwnerResponse(
            @Schema(description = "ID", example = "1")
            Long id,

            @Schema(description = "가게 사장 사용자 ID", example = "child123")
            String userId
    ) {
    }

    public record UpdateOwnerRequest(
            @Schema(description = "레벨 ID", example = "1")
            String levelId,

            @Schema(description = "사업자 번호", example = "1234567890")
            String businessNumber
    ) {
    }

    public record GetOwnerResponse(
            OwnerResponse owner
    ) {
    }

    public record OwnerResponse(
            @Schema(description = "사업자 ID", example = "1")
            Long id,

            @Schema(description = "사용자 ID", example = "1")
            Long userId,

            @Schema(description = "이름", example = "김길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @Schema(description = "계정 상태", example = "ACTIVE", allowableValues = {"PENDING", "ACTIVE", "LOCKED", "DELETED", "BLOCKED"})
            StatusType statusType,

            @Schema(description = "레벨 ID", example = "1")
            String levelId,

            @Schema(description = "사업자 번호", example = "1234567890")
            String businessNumber
    ) {
    }

    /**
     * 매장 회원가입 요청 DTO
     * 사용자 정보, 사업자 정보, 사업자 등록 서류 파일을 함께 받습니다.
     */
    public record SignUpOwnerRequest(
            @Schema(description = "비밀번호", example = "password123!")
            String password,

            @Schema(description = "비밀번호 확인", example = "password123!")
            String passwordConfirm,

            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @Schema(description = "매장명", example = "따뜻한 식당")
            String storeName,

            @Schema(description = "사업자 번호", example = "1234567890")
            String businessNumber,

            @Schema(description = "레벨 ID", example = "1")
            String levelId
    ) {
    }

    /**
     * 매장 회원가입 응답 DTO
     */
    public record SignUpOwnerResponse(
            @Schema(description = "사용자 ID", example = "1")
            Long userId,

            @Schema(description = "사업자 ID", example = "1")
            Long ownerId
    ) {
    }
}

package com.backend.onharu.interfaces.api.dto;

import com.backend.onharu.domain.common.enums.UserType;

import io.swagger.v3.oas.annotations.media.Schema;

public class UserControllerDto {


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

    /**
     * 아동 회원가입 요청 DTO
     * 사용자 정보와 증명서 파일을 함께 받습니다.
     */
    public record SignUpChildRequest(
            @Schema(description = "로그인 ID", example = "user123")
            String loginId,

            @Schema(description = "비밀번호", example = "password123!")
            String password,

            @Schema(description = "비밀번호 확인", example = "password123!")
            String passwordConfirm,

            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @Schema(description = "아동 증명서 파일 경로", example = "/certificates/certificate.pdf")
            String certificate
    ) {
    }

    /**
     * 아동 회원가입 응답 DTO
     */
    public record SignUpChildResponse(
            @Schema(description = "ID", example = "1")
            Long id,

            @Schema(description = "아동 사용자 ID", example = "child123")
            String userId
    ) {
    }

    /**
     * 매장 회원가입 요청 DTO
     * 사용자 정보, 사업자 정보, 사업자 등록 서류 파일을 함께 받습니다.
     */
    public record SignUpOwnerRequest(
            @Schema(description = "로그인 ID", example = "user123")
            String loginId,

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
            Long userId
    ) {
    }

    /**
     * 아동 프로필 수정 요청 DTO
     * 공통 정보(이름, 전화번호)와 아동 전용 정보(닉네임)를 함께 받습니다.
     */
    public record UpdateChildProfileRequest(
            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @Schema(description = "닉네임", example = "온하루친구")
            String nickname
    ) {
    }

    /**
     * 사업자 프로필 수정 요청 DTO
     * 공통 정보(이름, 전화번호)와 사업자 전용 정보를 함께 받습니다.
     */
    public record UpdateOwnerProfileRequest(
            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @Schema(description = "레벨 ID", example = "1")
            String levelId,

            @Schema(description = "사업자 번호", example = "1234567890")
            String businessNumber
    ) {
    }
}

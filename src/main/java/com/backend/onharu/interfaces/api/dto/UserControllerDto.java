package com.backend.onharu.interfaces.api.dto;

import com.backend.onharu.domain.common.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
            @NotBlank(message = "아이디는 필수 입력 값 입니다.")
            @Size(min = 8, max = 50, message = "아이디는 최소 8자 이상이어야 합니다.")
            @Schema(description = "로그인 ID", example = "user123")
            String loginId,

            @NotBlank(message = "비밀번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[~!@#$%^&*()_+\\-=]).{8,}$",
                    message = "비밀번호는 영문 대문자, 소문자, 숫자, 특수문자를 각각 최소 하나 이상 포함해야 하며 8자 이상이어야 합니다.")
            @Schema(description = "비밀번호", example = "password123!")
            String password,

            @NotBlank(message = "비밀번호 확인은 필수입니다.")
            @Schema(description = "비밀번호 확인", example = "password123!")
            String passwordConfirm,

            @NotBlank(message = "이름은 필수 입력 값 입니다.")
            @Size(max = 30, message = "이름은 30자 이내여야 합니다.")
            @Schema(description = "이름", example = "홍길동")
            String name,

            @NotBlank(message = "전화번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^01(?:0|[1-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 01012345678)")
            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @NotBlank(message = "닉네임은 필수 입력 값 입니다.")
            @Size(max = 100, message = "닉네임은 최대 100자를 넘을 수 없습니다.")
            @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
            @Schema(description = "닉네임", example = "코끼리땃쥐")
            String nickname,

            @NotBlank(message = "증명서 파일 경로는 필수입니다.")
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
            @NotBlank(message = "아이디는 필수 입력 값 입니다.")
            @Size(min = 8, max = 50, message = "아이디는 최소 8자 이상이어야 합니다.")
            @Schema(description = "로그인 ID", example = "user123")
            String loginId,

            @NotBlank(message = "비밀번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[~!@#$%^&*()_+\\-=]).{8,}$",
                    message = "비밀번호는 영문 대문자, 소문자, 숫자, 특수문자를 각각 최소 하나 이상 포함해야 하며 8자 이상이어야 합니다.")
            @Schema(description = "비밀번호", example = "password123!")
            String password,

            @NotBlank(message = "비밀번호 확인은 필수입니다.")
            @Schema(description = "비밀번호 확인", example = "password123!")
            String passwordConfirm,

            @NotBlank(message = "이름은 필수 입력 값 입니다.")
            @Size(max = 30, message = "이름은 30자 이내여야 합니다.")
            @Schema(description = "이름", example = "홍길동")
            String name,

            @NotBlank(message = "전화번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^01(?:0|[1-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 01012345678)")
            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @NotBlank(message = "매장명은 필수 입력 값 입니다.")
            @Size(max = 30, message = "매장명은 30자 이내여야 합니다.")
            @Schema(description = "매장명", example = "새마을 식당")
            String storeName,

            @NotBlank(message = "사업자 번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^\\d{10}$", message = "사업자 번호는 숫자 10자리여야 합니다. (예: 1234567890)")
            @Schema(description = "사업자 번호", example = "1234567890")
            String businessNumber,

            @NotNull(message = "등급 정보는 필수 입력 값 입니다.")
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
            @NotBlank(message = "이름은 필수 입력 값 입니다.")
            @Size(max = 30, message = "이름은 30자 이내여야 합니다.")
            @Schema(description = "이름", example = "홍길동")
            String name,

            @NotBlank(message = "전화번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^01(?:0|[1-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 01012345678)")
            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @NotBlank(message = "닉네임은 필수 입력 값 입니다.")
            @Size(max = 100, message = "닉네임은 최대 100자를 넘을 수 없습니다.")
            @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
            @Schema(description = "닉네임", example = "온하루친구")
            String nickname
    ) {
    }

    /**
     * 사업자 프로필 수정 요청 DTO
     * 공통 정보(이름, 전화번호)와 사업자 전용 정보를 함께 받습니다.
     */
    public record UpdateOwnerProfileRequest(
            @NotBlank(message = "이름은 필수 입력 값 입니다.")
            @Size(max = 30, message = "이름은 30자 이내여야 합니다.")
            @Schema(description = "이름", example = "홍길동")
            String name,

            @NotBlank(message = "전화번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^01(?:0|[1-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 01012345678)")
            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @NotNull(message = "등급 정보는 필수 입력 값 입니다.")
            @Schema(description = "레벨 ID", example = "1")
            String levelId,

            @NotBlank(message = "사업자 번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^\\d{10}$", message = "사업자 번호는 숫자 10자리여야 합니다. (예: 1234567890)")
            @Schema(description = "사업자 번호", example = "1234567890")
            String businessNumber
    ) {
    }
}

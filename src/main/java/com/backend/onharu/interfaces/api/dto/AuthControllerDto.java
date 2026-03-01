package com.backend.onharu.interfaces.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * AuthController 에서 사용되는 요청/응답 DTO 입니다.
 */
public class AuthControllerDto {

    public record BusinessNumberRequest(
            @NotBlank(message = "사업자 등록번호는 필수 입니다.")
            @Pattern(regexp = "\\d{10}", message = "사업자 등록번호는 숫자 10 자리여야 합니다.")
            @Schema(description = "사업자 등록번호", example = "1234567890")
            String businessNumber
    ) {
    }

    public record FindIdRequest(
            @Schema(description = "사용자 이름", example = "홍길동")
            String name,
            @Schema(description = "전화번호", example = "01012345678")
            String phone
    ) {
    }

    public record FindIdResponse(
            @Schema(description = "아이디", example = "user1234@test.com")
            String loginId
    ) {
    }

    public record ResetPasswordRequest(
            @Schema(description = "사용자 이름", example = "홍길동")
            String name,
            @Schema(description = "아이디", example = "user1234@test.com")
            String loginId,
            @Schema(description = "전화번호", example = "01012345678")
            String phone
    ) {
    }

    public record SendEmailCodeRequest(
            @Schema(description = "수신자 이메일 주소", example = "skhrnt2945@naver.com")
            String email
    ) {
    }

    public record VerifyEmailCodeRequest(
            @Schema(description = "이메일", example = "skhrnt2945@naver.com")
            String email,

            @Schema(description = "인증 코드", example = "f0bb2780-14b9-4c15-9161-c533f8b5b398")
            String code
    ) {
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "현재 비밀번호는 필수 입력 값 입니다.")
            @Schema(description = "현재 비밀번호", example = "random123!")
            String currentPassword,

            @NotBlank(message = "변경 비밀번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\W).{8,}$",
                    message = "비밀번호는 최소 영문 대소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 하며, 8자 이상이어야 합니다.")
            @Schema(description = "비밀번호", example = "password123!")
            String newPassword,

            @NotBlank(message = "변경 비밀번호 확인은 필수입니다.")
            @Schema(description = "비밀번호 확인", example = "password123!")
            String newPasswordConfirm
    ) {
    }

    public record ValidatePasswordRequest(
            @NotBlank(message = "현재 비밀번호는 필수 입력 값 입니다.")
            @Schema(description = "현재 비밀번호", example = "password123!")
            String password
    ) {
    }

//    public record SendSmsCodeRequest(
//            @Schema(description = "전화번호", example = "010-1234-5678")
//            String phoneNumber,
//
//            @Schema(description = "인증 목적", example = "SIGNUP")
//            String purpose
//    ) {
//    }

//    public record VerifySmsCodeRequest(
//            @Schema(description = "전화번호", example = "010-1234-5678")
//            String phoneNumber,
//
//            @Schema(description = "인증 코드", example = "123456")
//            String verificationCode
//    ) {
//    }
}

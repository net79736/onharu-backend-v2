package com.backend.onharu.interfaces.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class AuthControllerDto {

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
            @Schema(description = "이메일", example = "user1234@test.com")
            String email,
            @Schema(description = "사용자 이름", example = "홍길동")
            String name,
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

    public record SendSmsCodeRequest(
            @Schema(description = "전화번호", example = "010-1234-5678")
            String phoneNumber,

            @Schema(description = "인증 목적", example = "SIGNUP")
            String purpose
    ) {
    }

    public record VerifySmsCodeRequest(
            @Schema(description = "전화번호", example = "010-1234-5678")
            String phoneNumber,

            @Schema(description = "인증 코드", example = "123456")
            String verificationCode
    ) {
    }
}

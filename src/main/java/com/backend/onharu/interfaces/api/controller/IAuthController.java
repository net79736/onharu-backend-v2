package com.backend.onharu.interfaces.api.controller;

import org.springframework.http.ResponseEntity;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.AuthControllerDto.FindIdRequest;
import com.backend.onharu.interfaces.api.dto.AuthControllerDto.FindIdResponse;
import com.backend.onharu.interfaces.api.dto.AuthControllerDto.ResetPasswordRequest;
import com.backend.onharu.interfaces.api.dto.AuthControllerDto.SendEmailCodeRequest;
import com.backend.onharu.interfaces.api.dto.AuthControllerDto.SendSmsCodeRequest;
import com.backend.onharu.interfaces.api.dto.AuthControllerDto.VerifyEmailCodeRequest;
import com.backend.onharu.interfaces.api.dto.AuthControllerDto.VerifySmsCodeRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth", description = "인증 API")
public interface IAuthController {
    @Operation(summary = "아이디 찾기", description = "이메일 또는 전화번호로 아이디를 찾습니다.")
    ResponseEntity<ResponseDTO<FindIdResponse>> findId(
            @Schema(description = "아이디 찾기 요청")
            FindIdRequest request
    );

    @Operation(summary = "비밀번호 재설정", description = "이메일 또는 전화번호로 비밀번호 재설정을 요청합니다.")
    ResponseEntity<ResponseDTO<Void>> resetPassword(
            @Schema(description = "비밀번호 재설정 요청")
            ResetPasswordRequest request
    );

    @Operation(summary = "이메일 인증 코드 발송", description = "이메일로 인증 코드를 발송합니다.")
    ResponseEntity<ResponseDTO<Void>> sendEmailCode(
            @Schema(description = "이메일 인증 발송 요청")
            SendEmailCodeRequest request
    );

    @Operation(summary = "이메일 인증 코드 검증", description = "발송된 이메일 인증 코드를 검증합니다.")
    ResponseEntity<ResponseDTO<Void>> verifyEmailCode(
            @Schema(description = "이메일 인증 검증 요청")
            VerifyEmailCodeRequest request
    );

    @Operation(summary = "SMS 인증 코드 발송", description = "전화번호로 SMS 인증 코드를 발송합니다.")
    ResponseEntity<ResponseDTO<Void>> sendSmsCode(
            @Schema(description = "SMS 인증 발송 요청")
            SendSmsCodeRequest request
    );

    @Operation(summary = "SMS 인증 코드 검증", description = "발송된 SMS 인증 코드를 검증합니다.")
    ResponseEntity<ResponseDTO<Void>> verifySmsCode(
            @Schema(description = "SMS 인증 검증 요청")
            VerifySmsCodeRequest request
    );

    // @Operation(summary = "이메일 인증 번호 만료 처리", description = "이메일 인증 번호를 만료 처리합니다.")
    // ResponseEntity<ResponseDTO<Void>> expireEmailVerification(
    //         @Schema(description = "이메일", example = "user@example.com")
    //         String email
    // );

    // @Operation(summary = "SMS 인증 번호 만료 처리", description = "SMS 인증 번호를 만료 처리합니다.")
    // ResponseEntity<ResponseDTO<Void>> expireSmsVerification(
    //         @Schema(description = "전화번호", example = "010-1234-5678")
    //         String phoneNumber
    // );
}

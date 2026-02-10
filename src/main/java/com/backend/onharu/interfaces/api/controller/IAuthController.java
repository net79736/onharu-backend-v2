package com.backend.onharu.interfaces.api.controller;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.AuthControllerDto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

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

    /**
     * 이메일 인증 코드를 발송합니다.
     * <p>
     * POST /api/auth/email/send-code
     * 이메일 인증 코드를 수신자의 메일로 보냅니다. 수신자의 이메일 주소를 받습니다.
     */
    @Operation(summary = "이메일 인증 코드 발송", description = "이메일로 인증 코드를 발송합니다.")
    ResponseEntity<ResponseDTO<Void>> sendEmailCode(
            @RequestBody(
                    description = "이메일 인증 코드 발송 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SendEmailCodeRequest.class),
                            examples = @ExampleObject(
                                    name = "이메일 인증 코드 발송 요청 예시",
                                    value = "{\n" +
                                            "  \"email\": \"skhrnt2945@naver.com\"\n" +
                                            "}"
                    )
                )
            )
            SendEmailCodeRequest request
    );

    /**
     * 이메일 인증 코드 검증 요청을 수행합니다.
     * <p>
     * POST /api/auth/email/verify-code
     * 발송된 인증코드를 검증합니다. 이메일과 인증코드를 받습니다.
     */
    @Operation(summary = "이메일 인증 코드 검증", description = "발송된 이메일 인증 코드를 검증합니다.")
    ResponseEntity<ResponseDTO<Void>> verifyEmailCode(
            @RequestBody(
                    description = "이메일 인증 코드 검증",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SendEmailCodeRequest.class),
                            examples = @ExampleObject(
                                    name = "이메일 인증 코드 검증 요청 예시",
                                    value = "{\n" +
                                            "  \"email\": \"skhrnt2945@naver.com\",\n" +
                                            "  \"code\": \"f0bb2780-14b9-4c15-9161-c533f8b5b398\",\n" +
                                            "}"
                            )
                    )
            )
            VerifyEmailCodeRequest request
    );

//    @Operation(summary = "SMS 인증 코드 발송", description = "전화번호로 SMS 인증 코드를 발송합니다.")
//    ResponseEntity<ResponseDTO<Void>> sendSmsCode(
//            @Schema(description = "SMS 인증 발송 요청")
//            SendSmsCodeRequest request
//    );
//
//    @Operation(summary = "SMS 인증 코드 검증", description = "발송된 SMS 인증 코드를 검증합니다.")
//    ResponseEntity<ResponseDTO<Void>> verifySmsCode(
//            @Schema(description = "SMS 인증 검증 요청")
//            VerifySmsCodeRequest request
//    );

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

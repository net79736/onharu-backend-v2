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

    /**
     * 사업자 등록번호 확인(국세청 API 호출)
     * <p>
     * POST /api/auth/business-number
     */
    @Operation(summary = "사업자 등록번호 확인", description = "국세청 API 를 호출하여 사업자 등록번호를 확인합니다.")
    ResponseEntity<ResponseDTO<Boolean>> checkBusinessNumber(
            @RequestBody(
                    description = "사업자 등록번호를 포함한 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BusinessNumberRequest.class),
                            examples = @ExampleObject(
                                    name = "사업자 등록번호 확인 요청 예시 (쿠팡 사업자 등록번호)",
                                    value = """
                                            {
                                              "businessNumber": "1208800767"
                                            }
                                            """
                            )
                    )
            )
            BusinessNumberRequest request
    );

    /**
     * 아이디 찾기를 수행합니다.
     * <p>
     * POST /api/auth/find-id
     * 사용자의 아이디를 찾아서 반환합니다. 사용자의 이름, 전화번호를 받습니다.
     */
    @Operation(summary = "아이디 찾기", description = "이름 또는 전화번호로 아이디를 찾습니다.")
    ResponseEntity<ResponseDTO<FindIdResponse>> findId(
            @RequestBody(
                    description = "아이디 찾기 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FindIdRequest.class),
                            examples = @ExampleObject(
                                    name = "아이디 찾기 요청 예시",
                                    value = "{\n" +
                                            "  \"name\": \"홍길동\",\n" +
                                            "  \"phone\": \"01012345678\"\n" +
                                            "}"
                            )
                    )
            )
            FindIdRequest request
    );

    @Operation(summary = "비밀번호 재설정", description = "이름, 아이디, 전화번호로 비밀번호 재설정을 요청합니다.")
    ResponseEntity<ResponseDTO<Void>> resetPassword(
            @RequestBody(
                    description = "비밀번호 재설정 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResetPasswordRequest.class),
                            examples = @ExampleObject(
                                    name = "비밀번호 재설정 요청 예시",
                                    value = "{\n" +
                                            "  \"name\": \"홍길동\",\n" +
                                            "  \"loginId\": \"user1234@naver.com\",\n" +
                                            "  \"phone\": \"01012345678\"\n" +
                                            "}"
                            )
                    )
            )
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
                                    value = """
                                            {
                                              "email": "skhrnt2945@naver.com"
                                            }
                                            """
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
    @Operation(summary = "이메일 인증 코드 검증", description = "발송된 이메일 인증 코드(랜덤 숫자 6자리)를 검증합니다.")
    ResponseEntity<ResponseDTO<Void>> verifyEmailCode(
            @RequestBody(
                    description = "이메일 인증 코드 검증",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SendEmailCodeRequest.class),
                            examples = @ExampleObject(
                                    name = "이메일 인증 코드 검증 요청 예시",
                                    value = """
                                            {
                                              "email": "skhrnt2945@naver.com",
                                              "code": "123456"
                                            }
                                            """
                            )
                    )
            )
            VerifyEmailCodeRequest request
    );

    /**
     * 새 비밀번호로 변경 요청을 수행합니다.
     * <p>
     * POST /api/auth/change-password
     */
    @Operation(summary = "비밀번호 변경", description = "새 비밀번호로 변경합니다.")
    ResponseEntity<ResponseDTO<Void>> changePassword(
            @RequestBody(
                    description = "새 비밀번호로 변경합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChangePasswordRequest.class),
                            examples = @ExampleObject(
                                    name = "비밀번호 변경 요청 예시",
                                    value = """
                                            {
                                              "currentPassword": "f0bb2780!",
                                              "newPassword": "password123!",
                                              "newPasswordConfirm": "password123!"
                                            }
                                            """
                            )
                    )
            )
            ChangePasswordRequest request
    );

    /**
     * 입력한 비밀번호를 검증합니다.
     *
     * POST /api/auth/validate-password
     */
    @Operation(summary = "현재 비밀번호 검증", description = "현재 비밀번호가 DB 에 저장된 비밀번호와 일치하는지 확인합니다.")
    ResponseEntity<ResponseDTO<Boolean>> validatePassword(
            @RequestBody(
                    description = "현재 비밀번호가 맞는지 확인합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidatePasswordRequest.class),
                            examples = @ExampleObject(
                                    name = "현재 비밀번호 검증 요청 예시",
                                    value = """
                                            {
                                              "password": "password123!"
                                            }
                                            """
                            )
                    )
            )
            ValidatePasswordRequest request
    );

}

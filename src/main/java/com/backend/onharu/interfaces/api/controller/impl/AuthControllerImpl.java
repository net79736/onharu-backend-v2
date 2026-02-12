package com.backend.onharu.interfaces.api.controller.impl;

import com.backend.onharu.application.AuthFacade;
import com.backend.onharu.domain.email.dto.EmailAuthenticationCommand.CreateEmailAuthenticationCommand;
import com.backend.onharu.domain.email.dto.EmailAuthenticationCommand.CompleteEmailAuthenticationCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IAuthController;
import com.backend.onharu.interfaces.api.dto.AuthControllerDto.FindIdRequest;
import com.backend.onharu.interfaces.api.dto.AuthControllerDto.FindIdResponse;
import com.backend.onharu.interfaces.api.dto.AuthControllerDto.ResetPasswordRequest;
import com.backend.onharu.interfaces.api.dto.AuthControllerDto.SendEmailCodeRequest;
import com.backend.onharu.interfaces.api.dto.AuthControllerDto.VerifyEmailCodeRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * 인증 관련 API를 제공하는 컨트롤러 구현체입니다.
 * <p>
 * 아이디 찾기, 비밀번호 찾기, 이메일/SMS 인증 코드 발송 및 검증 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthControllerImpl implements IAuthController {

    private final AuthFacade authFacade;

    /**
     * 아이디 찾기
     * <p>
     * 전화번호로 아이디를 찾습니다.
     *
     * @param request 아이디 찾기 요청
     * @return 찾은 아이디 정보
     */
    @Override
    @PostMapping("/find-id")
    public ResponseEntity<ResponseDTO<FindIdResponse>> findId(
            @RequestBody FindIdRequest request
    ) {
        log.info("아이디 찾기 요청: {}", request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 비밀번호 재설정
     * <p>
     * 이메일 또는 전화번호로 비밀번호 재설정을 요청합니다.
     *
     * @param request 비밀번호 재설정 요청
     * @return 비밀번호 재설정 결과
     */
    @Override
    @PostMapping("/reset-password")
    public ResponseEntity<ResponseDTO<Void>> resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {
        log.info("비밀번호 재설정 요청: {}", request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 이메일 인증 코드 발송
     * <p>
     * POST /api/auth/email/send-code
     * 이메일로 인증 코드를 발송합니다.
     *
     * @param request 이메일 인증 발송 요청
     * @return 발송 결과
     */
    @Override
    @PostMapping("/email/send-code")
    public ResponseEntity<ResponseDTO<Void>> sendEmailCode(
            @RequestBody SendEmailCodeRequest request
    ) {
        log.info("이메일 인증 코드 발송 요청: {}", request);

        authFacade.createEmailAuthentication(new CreateEmailAuthenticationCommand(
                request.email(),
                LocalDateTime.now().plusMinutes(5))
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 이메일 인증 코드 검증
     * <p>
     * POST /api/auth/email/verify-code
     * 발송된 이메일 인증 코드를 검증합니다.
     *
     * @param request 이메일 인증 검증 요청
     * @return 검증 결과
     */
    @Override
    @PostMapping("/email/verify-code")
    public ResponseEntity<ResponseDTO<Void>> verifyEmailCode(
            @RequestBody VerifyEmailCodeRequest request
    ) {
        log.info("이메일 인증 코드 검증 요청: {}", request);

        authFacade.completeEmailAuthentication(new CompleteEmailAuthenticationCommand(
                request.email(),
                request.code(),
                LocalDateTime.now()
        ));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * SMS 인증 코드 발송
     *
     * POST /api/auth/sms/send-code
     * 전화번호로 SMS 인증 코드를 발송합니다.
     *
     * @param request SMS 인증 발송 요청
     * @return 발송 결과
     */
//    @Override
//    @PostMapping("/sms/send-code")
//    public ResponseEntity<ResponseDTO<Void>> sendSmsCode(
//            @RequestBody SendSmsCodeRequest request
//    ) {
//        log.info("SMS 인증 코드 발송 요청: {}", request);
//
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(ResponseDTO.success(null));
//    }

    /**
     * SMS 인증 코드 검증
     *
     * POST /api/auth/sms/verify-code
     * 발송된 SMS 인증 코드를 검증합니다.
     *
     * @param request SMS 인증 검증 요청
     * @return 검증 결과
     */
//    @Override
//    @PostMapping("/sms/verify-code")
//    public ResponseEntity<ResponseDTO<Void>> verifySmsCode(
//            @RequestBody VerifySmsCodeRequest request
//    ) {
//        log.info("SMS 인증 코드 검증 요청: {}", request);
//
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(ResponseDTO.success(null));
//    }

    /**
     * 이메일 인증 번호 만료 처리
     *
     * POST /api/auth/email/expire
     * 이메일 인증 번호를 만료 처리합니다.
     *
     * @param email 이메일
     * @return 만료 처리 결과
     */
    // @Override
    // @PostMapping("/email/expire")
    // public ResponseEntity<ResponseDTO<Void>> expireEmailVerification(
    //         @RequestParam String email
    // ) {
    //     log.info("이메일 인증 번호 만료 처리 요청: email={}", email);

    //     return ResponseEntity.status(HttpStatus.OK)
    //             .body(ResponseDTO.success(null));
    // }

    /**
     * SMS 인증 번호 만료 처리
     *
     * POST /api/auth/sms/expire
     * SMS 인증 번호를 만료 처리합니다.
     *
     * @param phoneNumber 전화번호
     * @return 만료 처리 결과
     */
    // @Override
    // @PostMapping("/sms/expire")
    // public ResponseEntity<ResponseDTO<Void>> expireSmsVerification(
    //         @RequestParam String phoneNumber
    // ) {
    //     log.info("SMS 인증 번호 만료 처리 요청: phoneNumber={}", phoneNumber);

    //     return ResponseEntity.status(HttpStatus.OK)
    //             .body(ResponseDTO.success(null));
    // }
}

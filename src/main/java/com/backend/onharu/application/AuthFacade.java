package com.backend.onharu.application;

import com.backend.onharu.domain.email.dto.EmailAuthenticationCommand.CompleteEmailAuthenticationCommand;
import com.backend.onharu.domain.email.dto.EmailAuthenticationCommand.CreateEmailAuthenticationCommand;
import com.backend.onharu.domain.email.dto.EmailAuthenticationQuery.ExistsVerifiedByEmailQuery;
import com.backend.onharu.domain.email.model.EmailAuthentication;
import com.backend.onharu.domain.email.service.EmailAuthenticationCommandService;
import com.backend.onharu.domain.email.service.EmailAuthenticationQueryService;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.email.EmailSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.backend.onharu.domain.support.error.ErrorType.EmailAuthentication.EMAIL_NOT_VERIFIED;

/**
 * 사용자의 이메일 인증 및 처리를 수행하는 Facade 입니다.
 */
@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final EmailAuthenticationCommandService emailAuthenticationCommandService;
    private final EmailAuthenticationQueryService emailAuthenticationQueryService;
    private final EmailSendService emailSendService;

    /**
     * 이메일 인증 코드 생성/재인증 메서드
     */
    public void createEmailAuthentication(CreateEmailAuthenticationCommand command) {
        EmailAuthentication emailAuthentication = emailAuthenticationCommandService.create(command); // 이메일 인증 생성

        // 인증 이메일 전송
        emailSendService.sendEmail(
                emailAuthentication.getEmail(),
                emailAuthentication.getToken()
        );
    }

    /**
     * 이메일 인증 완료 메서드
     */
    public void completeEmailAuthentication(CompleteEmailAuthenticationCommand command) {
        emailAuthenticationCommandService.verify(command);
    }

    /**
     * 이메일 인증 확인 메서드
     */
    public void verifyCode(ExistsVerifiedByEmailQuery query) {
        boolean verified = emailAuthenticationQueryService.existsByEmailAndIsVerifiedTrue(query);

        if (!verified) { // 이메일 인증이 안된 경우
            throw new CoreException(EMAIL_NOT_VERIFIED);
        }
    }
}

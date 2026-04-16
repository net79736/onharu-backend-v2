package com.backend.onharu.infra.email;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SMTP 로 보낼 이메일 전송을 담당합니다.
 * <p>
 * 본문은 Thymeleaf HTML 템플릿으로 렌더링합니다(reddit-clone 스타일의 레이아웃을 온하루 브랜딩에 맞게 이식).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSendService {

    private static final String TPL_VERIFICATION = "onharu-email-verification";
    private static final String TPL_TEMP_PASSWORD = "onharu-temporary-password";

    private final EmailSender emailSender;

    @Qualifier("mailTemplateEngine")
    private final SpringTemplateEngine mailTemplateEngine;

    /**
     * 이메일 인증 메일 전송
     */
    public void sendEmail(String email, String token) {
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("code", token);
        String content = mailTemplateEngine.process(TPL_VERIFICATION, context);

        String subject = "[온하루] 이메일 인증 번호가 도착했습니다.";
        log.info("이메일 인증 메일 발송 준비: to={}", email);
        emailSender.send(email, subject, content);
    }

    /**
     * 임시 비밀번호 메일 전송
     */
    public void sendResetPasswordEmail(String email, String tempPassword) {
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("tempPassword", tempPassword);
        String content = mailTemplateEngine.process(TPL_TEMP_PASSWORD, context);

        String subject = "[온하루] 임시 비밀번호 발급";
        log.info("임시 비밀번호 메일 발송 준비: to={}", email);
        emailSender.send(email, subject, content);
    }
}

package com.backend.onharu.infra.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * SMTP 설정에 사용되는 코드 입니다.
 */
@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from; // 이메일 발신 계정 소유자

    /**
     *
     * @param to 수신자 이메일 주소
     * @param subject 이메일 제목
     * @param content 이메일 내용
     */
    public void send(String to, String subject, String content) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage(); // 메시지 객체 생성

        try {
            // 전송 보낼 메시지 설정
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            javaMailSender.send(mimeMessage); // 이메일 전송
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송에 실패했습니다." + e);
        }
    }
}

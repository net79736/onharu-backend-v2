package com.backend.onharu.infra.email.impl;

import com.backend.onharu.infra.email.EmailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * SMTP 설정에 사용되는 코드 입니다.
 */
@Component
@RequiredArgsConstructor
public class EmailSenderImpl implements EmailSender {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String username;

    /**
     * {@code spring.mail.from} 이 비어 있으면 {@code spring.mail.username} 을 발신자로 사용합니다.
     */
    @Value("${spring.mail.from:}")
    private String fromOverride;

    /**
     * @param to      수신자 이메일 주소
     * @param subject 이메일 제목
     * @param content 이메일 내용
     */
    @Override
    public void send(String to, String subject, String content) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(resolveFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        } catch (MailException e) {
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }

    private String resolveFrom() {
        if (StringUtils.hasText(fromOverride)) {
            return fromOverride.trim();
        }
        return username;
    }
}

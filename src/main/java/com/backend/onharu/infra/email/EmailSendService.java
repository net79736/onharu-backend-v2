package com.backend.onharu.infra.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * SMTP 로 보낼 이메일 전송을 담당하는 서비스 입니다.
 */
@Service
@RequiredArgsConstructor
public class EmailSendService {

    private final EmailSender emailSender;

    /**
     * 이메일 인증 메일 전송
     */
    public void sendEmail(String email, String token) {
        String subject = "[온하루] 이메일 인증 번호가 도착했습니다.";
        String content = """
                <h2>이메일 인증 안내</h2>
                <p>아래 인증 코드를 입력해 주세요.</p>
                <p><strong>인증 코드:</strong> %s</p>
                """.formatted(token);

        emailSender.send(email, subject, content);
    }
}

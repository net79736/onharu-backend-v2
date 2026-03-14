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

    /**
     * 임시 비밀번호 메일 전송
     */
    public void sendResetPasswordEmail(String email, String tempPassword) {
        String subject = "[온하루] 임시 비밀번호 발급";
        String content = """
                <h2>임시 비밀번호 안내</h2>
                <p>요청하신 임시 비밀번호가 발급되었습니다.</p>
                <p>임시 비밀번호로 로그인 후, 비밀번호를 변경해 주세요.</p>
                <p><strong>임시 비밀번호:</strong> <code>%s</code></p>
                """.formatted(tempPassword);

        emailSender.send(email, subject, content);
    }
}

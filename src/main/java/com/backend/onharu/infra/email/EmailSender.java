package com.backend.onharu.infra.email;

/**
 * 이메일 전송 설정 인터페이스 입니다.
 */
public interface EmailSender {

    void send(String to, String subject, String content);
}

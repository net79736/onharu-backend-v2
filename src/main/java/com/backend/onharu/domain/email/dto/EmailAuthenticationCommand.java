package com.backend.onharu.domain.email.dto;

import java.time.LocalDateTime;

/**
 * 이메일 인증 관련 Command DTO
 */
public class EmailAuthenticationCommand {

    /**
     * 이메일 인증 생성 Command
     */
    public record CreateEmailAuthenticationCommand(
            String email,
            LocalDateTime expiredAt
    ) {
    }

    /**
     * 이메일 인증 완료 처리 Command
     */
    public record CompleteEmailAuthenticationCommand(
            String email,
            String token,
            LocalDateTime now
    ) {
    }

    /**
     * 미인증 토큰을 만료처리 Command
     */
    public record ExpireTokensCommand(
            String email,
            LocalDateTime now
    ) {
    }

    /**
     * 이메일 인증 횟수 카운트 Command
     */
    public record CountEmailAuthenticationCommand(
            String email,
            LocalDateTime now
    ) {
    }
}

package com.backend.onharu.domain.email.dto;

import java.time.LocalDateTime;

/**
 * 이메일 인증 관련 Query DTO
 */
public class EmailAuthenticationQuery {

    /**
     * 이메일 인증 ID 로 인증 정보 단건 조회 Query
     */
    public record GetEmailAuthenticationByIdQuery(
            Long emailAuthenticationId
    ) {
    }

    /**
     * 이메일로 인증 정보 단건 조회 Query
     */
    public record FindByEmailQuery(
            String email
    ) {
    }

    /**
     * 이메일과 토큰으로 인증 정보 단건 조회 Query
     */
    public record FindByEmailAndTokenQuery(
            String email,
            String token
    ) {
    }

    /**
     * 현재 시간 기준으로 만료된 인증 정보 목록 조회 Query
     */
    public record FindByExpiredQuery(
            LocalDateTime now
    ) {
    }

    /**
     * 이메일 인증 완료 여부를 확인 Query
     */
    public record ExistsVerifiedByEmailQuery(
            String email,
            boolean isVerify
    ) {
    }
}

package com.backend.onharu.domain.email.dto;

import java.time.LocalDateTime;

/**
 * 이메일 인증 정보 Repository 파라미터 DTO 입니다.
 */
public class EmailAuthenticationRepositoryParam {

    /**
     * 이메일 인증 ID 로 인증 정보 단건 조회용 파라미터
     */
    public record GetEmailAuthenticationByIdParam(
            Long emailAuthenticationId
    ) {
    }

    /**
     * 이메일로 인증 정보 단건 조회용 파라미터
     */
    public record FindByEmailParam(
            String email
    ) {
    }

    /**
     * 이메일과 토큰으로 인증 정보 단건 조회용 파라미터
     */
    public record FindByEmailAndTokenParam(
            String email,
            String token
    ) {
    }

    /**
     * 현재 시간을 기준으로 만료된 인증 정보 목록을 조회용 파라미터
     */
    public record FindByExpiredParam(
            LocalDateTime now
    ) {
    }

    /**
     * 이메일 인증 완료 여부를 확인용 파라미터
     */
    public record ExistsVerifiedByEmailParam(
            String email
    ) {
    }

    /**
     * 현재 시간을 기준으로 미인증 토큰을 만료처리합니다.
     */
    public record ExpireTokensParam(
            String email,
            LocalDateTime now
    ) {
    }

    /**
     * 현재 시각을 기준으로 이메일 인증 호출 횟수를 셉니다.
     */
    public record CountEmailAuthenticationParam(
            String email,
            LocalDateTime now
    ) {
    }
}

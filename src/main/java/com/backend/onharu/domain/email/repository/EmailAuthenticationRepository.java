package com.backend.onharu.domain.email.repository;

import com.backend.onharu.domain.email.dto.EmailAuthenticationRepositoryParam.*;
import com.backend.onharu.domain.email.model.EmailAuthentication;

import java.util.List;
import java.util.Optional;

/**
 * 이메일 인증 Repository 인터페이스
 */
public interface EmailAuthenticationRepository {

    /**
     * 이메일 인증을 생성합니다.
     */
    EmailAuthentication save(EmailAuthentication emailAuthentication);

    /**
     * 이메일 인증 ID 로 인증 정보를 조회합니다.
     */
    EmailAuthentication findEmailAuthenticationById(GetEmailAuthenticationByIdParam param);

    /**
     * 이메일로 인증 정보를 조회합니다.
     */
    Optional<EmailAuthentication> findEmailAuthenticationByEmail(FindByEmailParam param);

    /**
     * 이메일과 토큰으로 인증 정보를 조회합니다.
     */
    EmailAuthentication findEmailAuthenticationByEmailAndToken(FindByEmailAndTokenParam param);

    /**
     * 현재 시간을 기준으로 만료된 인증 정보 목록을 조회합니다.
     */
    List<EmailAuthentication> findAllByExpiredAtBefore(FindByExpiredParam param);

    /**
     * 이메일 인증 완료 여부를 확인합니다.
     */
    boolean existsByEmailAndIsVerifiedTrue(ExistsVerifiedByEmailParam param);
}

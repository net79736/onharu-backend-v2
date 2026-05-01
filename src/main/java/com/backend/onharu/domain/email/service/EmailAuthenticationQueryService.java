package com.backend.onharu.domain.email.service;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.email.dto.EmailAuthenticationQuery.GetEmailAuthenticationByIdQuery;
import com.backend.onharu.domain.email.dto.EmailAuthenticationQuery.FindByEmailAndTokenQuery;
import com.backend.onharu.domain.email.dto.EmailAuthenticationQuery.FindByEmailQuery;
import com.backend.onharu.domain.email.dto.EmailAuthenticationQuery.FindByExpiredQuery;
import com.backend.onharu.domain.email.dto.EmailAuthenticationQuery.ExistsVerifiedByEmailQuery;
import com.backend.onharu.domain.email.dto.EmailAuthenticationRepositoryParam.GetEmailAuthenticationByIdParam;
import com.backend.onharu.domain.email.dto.EmailAuthenticationRepositoryParam.FindByEmailParam;
import com.backend.onharu.domain.email.dto.EmailAuthenticationRepositoryParam.FindByExpiredParam;
import com.backend.onharu.domain.email.dto.EmailAuthenticationRepositoryParam.FindByEmailAndTokenParam;
import com.backend.onharu.domain.email.dto.EmailAuthenticationRepositoryParam.ExistsVerifiedByEmailParam;
import com.backend.onharu.domain.email.model.EmailAuthentication;
import com.backend.onharu.domain.email.repository.EmailAuthenticationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 이메일 인증 Query Service
 */
@Service
@Transactional
@RequiredArgsConstructor
public class EmailAuthenticationQueryService {

    private final EmailAuthenticationRepository emailAuthenticationRepository;

    /**
     * 이메일 인증 ID 로 인증 정보를 조회합니다.
     *
     * @param query 이메일 인증 ID 를 포함한 query
     * @return 조회된 이메일 인증 엔티티
     * @throws CoreException 이메일 인증을 찾을 수 없는 경우
     */
    public EmailAuthentication getEmailAuthentication(GetEmailAuthenticationByIdQuery query) {
        return emailAuthenticationRepository.findEmailAuthenticationById(new GetEmailAuthenticationByIdParam(
                query.emailAuthenticationId()
        ));
    }

    /**
     * 이메일로 인증 정보를 조회합니다.
     *
     * @param query 이메일을 포함한 query
     * @return 조회된 이메일 인증 엔티티
     */
    public Optional<EmailAuthentication> findEmailAuthenticationByEmail(FindByEmailQuery query) {
        return emailAuthenticationRepository.findEmailAuthenticationByEmail(new FindByEmailParam(
                query.email()
        ));
    }

    /**
     * 이메일과 토큰으로 인증 정보를 조회합니다.
     *
     * @param query 이메일과 토큰 정보를 포함한 query
     * @return 조회된 이메일 인증 엔티티
     * @throws CoreException 이메일 인증을 찾을 수 없는 경우
     */
    public EmailAuthentication findEmailAuthenticationByEmailAndToken(FindByEmailAndTokenQuery query) {
        return emailAuthenticationRepository.findEmailAuthenticationByEmailAndToken(new FindByEmailAndTokenParam(
                query.email(),
                query.token()
        ));
    }

    /**
     * 현재 시각 기준으로 만료된 인증 정보 목록 조회
     *
     * @param query 현재 시간이 포함된 Query
     * @return 조회된 이메일 인증 리스트
     */
    public List<EmailAuthentication> findAllByExpiredAtBefore(FindByExpiredQuery query) {
        return emailAuthenticationRepository.findAllByExpiredAtBefore(new FindByExpiredParam(
                query.now()
        ));
    }

    /**
     * 이메일 인증 완료 여부 확인
     *
     * @param query 이메일과 검증여부를 포함한 Query
     */
    public boolean existsByEmailAndIsVerifiedTrue(ExistsVerifiedByEmailQuery query) {
        return emailAuthenticationRepository.existsByEmailAndIsVerifiedTrue(new ExistsVerifiedByEmailParam(
                query.email()
        ));
    }
}

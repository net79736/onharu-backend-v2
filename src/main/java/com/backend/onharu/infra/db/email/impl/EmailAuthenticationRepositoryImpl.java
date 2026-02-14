package com.backend.onharu.infra.db.email.impl;

import com.backend.onharu.domain.email.dto.EmailAuthenticationRepositoryParam.*;
import com.backend.onharu.domain.email.model.EmailAuthentication;
import com.backend.onharu.domain.email.repository.EmailAuthenticationRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.email.EmailAuthenticationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.backend.onharu.domain.support.error.ErrorType.EmailAuthentication.EMAIL_AUTHENTICATION_NOT_FOUND;

/**
 * 이메일 인증 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class EmailAuthenticationRepositoryImpl implements EmailAuthenticationRepository {

    private final EmailAuthenticationJpaRepository emailAuthenticationJpaRepository;

    @Override
    public EmailAuthentication save(EmailAuthentication emailAuthentication) {
        return emailAuthenticationJpaRepository.save(emailAuthentication);
    }

    @Override
    public EmailAuthentication findEmailAuthenticationById(GetEmailAuthenticationByIdParam param) {
        return emailAuthenticationJpaRepository.findById(param.emailAuthenticationId())
                .orElseThrow(() -> new CoreException(EMAIL_AUTHENTICATION_NOT_FOUND));
    }

    @Override
    public Optional<EmailAuthentication> findEmailAuthenticationByEmail(FindByEmailParam param) {
        return emailAuthenticationJpaRepository.findByEmail(param.email());
    }

    @Override
    public EmailAuthentication findEmailAuthenticationByEmailAndToken(FindByEmailAndTokenParam param) {
        return emailAuthenticationJpaRepository.findByEmailAndToken(param.email(), param.token())
                .orElseThrow(() -> new CoreException(EMAIL_AUTHENTICATION_NOT_FOUND));
    }

    @Override
    public List<EmailAuthentication> findAllByExpiredAtBefore(FindByExpiredParam param) {
        return emailAuthenticationJpaRepository.findAllByExpiredAtBefore(param.now());
    }

    @Override
    public boolean existsByEmailAndIsVerifiedTrue(ExistsVerifiedByEmailParam param) {
        return emailAuthenticationJpaRepository.existsByEmailAndIsVerifiedTrue(param.email());
    }
}

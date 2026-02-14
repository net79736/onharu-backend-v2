package com.backend.onharu.domain.email.service;

import com.backend.onharu.domain.email.dto.EmailAuthenticationCommand.CompleteEmailAuthenticationCommand;
import com.backend.onharu.domain.email.dto.EmailAuthenticationCommand.CreateEmailAuthenticationCommand;
import com.backend.onharu.domain.email.dto.EmailAuthenticationRepositoryParam.FindByEmailAndTokenParam;
import com.backend.onharu.domain.email.dto.EmailAuthenticationRepositoryParam.FindByEmailParam;
import com.backend.onharu.domain.email.model.EmailAuthentication;
import com.backend.onharu.domain.email.repository.EmailAuthenticationRepository;
import com.backend.onharu.domain.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.backend.onharu.domain.support.error.ErrorType.EmailAuthentication.EMAIL_ALREADY_VERIFIED;

/**
 * 이메일 인증 Command Service
 */
@Service
@Transactional
@RequiredArgsConstructor
public class EmailAuthenticationCommandService {

    private final EmailAuthenticationRepository emailAuthenticationRepository;


    /**
     * 이메일 인증을 생성하거나, 재인증을 처리합니다.
     */
    public EmailAuthentication create(CreateEmailAuthenticationCommand command) {
        // 기존 인증이 있는지 조회
        EmailAuthentication emailAuthentication = emailAuthenticationRepository.findEmailAuthenticationByEmail(
                new FindByEmailParam(command.email())
        ).map(existing -> {
            if (existing.isVerified()) { // 이미 검증 완료된 이메일 인증 정보가 존재하는 경우
                throw new CoreException(EMAIL_ALREADY_VERIFIED);
            }
            // 아직 검증이 완료되지 않은 경우 인증 재발급
            existing.reissue(command.expiredAt());

            return existing;
        }).orElseGet(() -> EmailAuthentication.builder()
                .email(command.email())
                .expiredAt(command.expiredAt())
                .build()
        );
        // 이메일 인증 저장
        return emailAuthenticationRepository.save(emailAuthentication);
    }

    /**
     * 이메일 인증 완료 처리
     */
    public void verify(CompleteEmailAuthenticationCommand command) {
        // 이메일과 토큰으로 이메일 인증 엔티티 조회
        EmailAuthentication emailAuthentication = emailAuthenticationRepository.findEmailAuthenticationByEmailAndToken(
                new FindByEmailAndTokenParam(command.email(), command.token())
        );
        // 이메일 인증 검증 및 처리
        emailAuthentication.verify(command.now());
        // isVerified = true 로 변경한 이메일 인증 엔티티 저장
        emailAuthenticationRepository.save(emailAuthentication);
    }
}

package com.backend.onharu.domain.email.service;

import com.backend.onharu.domain.email.dto.EmailAuthenticationCommand.CompleteEmailAuthenticationCommand;
import com.backend.onharu.domain.email.dto.EmailAuthenticationCommand.CreateEmailAuthenticationCommand;
import com.backend.onharu.domain.email.model.EmailAuthentication;
import com.backend.onharu.domain.email.repository.EmailAuthenticationRepository;
import com.backend.onharu.domain.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.backend.onharu.domain.email.dto.EmailAuthenticationRepositoryParam.*;
import static com.backend.onharu.domain.support.error.ErrorType.EmailAuthentication.EMAIL_AUTHENTICATION_TOO_MANY_REQUEST;

/**
 * 이메일 인증 Command Service
 */
@Service
@Transactional
@RequiredArgsConstructor
public class EmailAuthenticationCommandService {

    private final EmailAuthenticationRepository emailAuthenticationRepository;

    /**
     * 이메일 인증을 생성
     */
    public EmailAuthentication create(CreateEmailAuthenticationCommand command) {
        String email = command.email();
        LocalDateTime expiredAt = command.expiredAt();
        LocalDateTime now = LocalDateTime.now();

        // 이메일 인증 요청 횟수 조회
        long countEmailAuthentication = emailAuthenticationRepository.countEmailAuthentication(
                new CountEmailAuthenticationParam(email, now.minusMinutes(1)) // 1분을 기준으로 카운트
        );

        // 1분 내 요청이 10번 이상일 경우 예외 발생
        if (countEmailAuthentication >= 10) {
            throw new CoreException(EMAIL_AUTHENTICATION_TOO_MANY_REQUEST);
        }

        // 기존의 이메일 인증 토큰 만료 처리
        emailAuthenticationRepository.expireTokens(
                new ExpireTokensParam(email, now)
        );

        // 새 이메일 인증 생성
        EmailAuthentication emailAuthentication = EmailAuthentication.builder()
                .email(email)
                .expiredAt(expiredAt)
                .build();

        // 이메일 인증 저장
        return emailAuthenticationRepository.save(emailAuthentication);
    }

    /**
     * 이메일 인증 완료 처리
     */
    public void verify(CompleteEmailAuthenticationCommand command) {
        // 이메일로 마지막에 생성된 이메일 인증 엔티티 조회
        EmailAuthentication emailAuthentication = emailAuthenticationRepository.findTopByEmailOrderByCreatedAtDesc(
                new FindByEmailParam(command.email())
        );

        // 토큰과 현재시각을 기준으로 이메일 인증 검증 (검증성공시 isVerified 를 true 로 변경)
        emailAuthentication.verify(command.token(), command.now());

        // isVerified = true 로 변경한 이메일 인증 엔티티 저장
        emailAuthenticationRepository.save(emailAuthentication);
    }
}

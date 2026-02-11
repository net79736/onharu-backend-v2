package com.backend.onharu.domain.email.service;

import com.backend.onharu.domain.email.dto.EmailAuthenticationCommand.CreateEmailAuthenticationCommand;
import com.backend.onharu.domain.email.model.EmailAuthentication;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.email.EmailAuthenticationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static com.backend.onharu.domain.email.dto.EmailAuthenticationCommand.CompleteEmailAuthenticationCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 이메일 인증 Command Service 테스트 코드 입니다.
 */
@SpringBootTest
class EmailAuthenticationCommandServiceTest {

    @Autowired
    private EmailAuthenticationCommandService emailAuthenticationCommandService;

    @Autowired
    private EmailAuthenticationJpaRepository emailAuthenticationJpaRepository;

    @BeforeEach
    void setUp() {
        emailAuthenticationJpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("이메일 인증 생성 테스트")
    class createEmailAuthentication {

        @Test
        @DisplayName("처음 이메일 인증 수행시 성공")
        void shouldCreateEmailAuthentication() {
            // GIVEN
            String email = "test1234@test.com";
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(5);

            CreateEmailAuthenticationCommand command = new CreateEmailAuthenticationCommand(email, expiredAt);
            // WHEN
            EmailAuthentication emailAuthentication = emailAuthenticationCommandService.create(command);
            // THEN
            assertThat(emailAuthentication.getEmail()).isEqualTo(email);
            assertThat(emailAuthentication.isVerified()).isFalse();
            assertThat(emailAuthentication.getExpiredAt()).isEqualTo(expiredAt);
        }

        @Test
        @DisplayName("이미 인증 완료된 경우 예외 발생")
        void shouldCreateEmailAuthenticationWhenAlreadyComplete() {
            // GIVEN
            String email = "test2222@test.com";
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(1);

            EmailAuthentication savedEmailAuthentication = emailAuthenticationJpaRepository.save(
                    EmailAuthentication.builder()
                            .email(email)
                            .expiredAt(expiredAt)
                            .build()
            );

            savedEmailAuthentication.verify(LocalDateTime.now()); // 인증 처리 완료
            emailAuthenticationJpaRepository.save(savedEmailAuthentication);

            CreateEmailAuthenticationCommand command = new CreateEmailAuthenticationCommand(email, expiredAt);
            // WHEN
            assertThatThrownBy(() -> emailAuthenticationCommandService.create(command))
                    .isInstanceOf(CoreException.class);
        }
    }

    @Nested
    @DisplayName("이메일 인증 완료 테스트")
    class verifyEmailAuthentication {

        @Test
        @DisplayName("이메일 인증 완료시, isVerified 의 상태가 true 가 되는지 테스트")
        void shouldCreateEmailAuthenticationWhenVerifyIsTrue() {
            // GIVEN
            String email = "test9999@test.com";
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(5);

            EmailAuthentication saved = emailAuthenticationJpaRepository.save(
                    EmailAuthentication.builder()
                            .email(email)
                            .expiredAt(expiredAt)
                            .build()
            );

            CompleteEmailAuthenticationCommand command = new CompleteEmailAuthenticationCommand(
                    email,
                    saved.getToken(),
                    LocalDateTime.now()
            );

            // WHEN
            emailAuthenticationCommandService.verify(command); // 검증 완료

            // THEN
            EmailAuthentication emailAuthentication = emailAuthenticationJpaRepository.findByEmailAndToken(email, saved.getToken())
                    .orElseThrow();

            assertThat(emailAuthentication.isVerified()).isTrue();
        }
    }

}
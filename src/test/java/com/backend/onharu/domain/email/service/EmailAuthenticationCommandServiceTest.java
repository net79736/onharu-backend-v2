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
import com.backend.onharu.domain.common.TestDataHelper;

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


    private TestDataHelper testDataHelper;


    @Autowired
    private EmailAuthenticationJpaRepository emailAuthenticationJpaRepository;

    @BeforeEach
    void setUp() {

        testDataHelper.cleanAll();

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
        @DisplayName("재요청시 기존 미인증 토큰은 만료")
        void shouldExpirePreviousTokenWhenReissue() {
            // GIVEN
            String email = "test2222@test.com";

            EmailAuthentication emailAuthentication = emailAuthenticationJpaRepository.save(
                    EmailAuthentication.builder()
                            .email(email)
                            .expiredAt(LocalDateTime.now().plusMinutes(5))
                            .build()
            );

            CreateEmailAuthenticationCommand command = new CreateEmailAuthenticationCommand(
                    email,
                    LocalDateTime.now().plusMinutes(5)
            );

            // WHEN
            EmailAuthentication result = emailAuthenticationCommandService.create(command);

            // THEN
            EmailAuthentication expiredAuth = emailAuthenticationJpaRepository.findById(emailAuthentication.getId()).orElseThrow();

            assertThat(expiredAuth.getExpiredAt()).isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.isVerified()).isFalse();
        }

        @Test
        @DisplayName("1분 내 10번 이상 요청 시 예외 발생")
        void shouldThrowExceptionWhenTooManyRequests() {
            // GIVEN
            String email = "test3333@test.com";

            for (int i = 0; i < 10; i++) {
                emailAuthenticationJpaRepository.save(
                        EmailAuthentication.builder()
                                .email(email)
                                .expiredAt(LocalDateTime.now().plusMinutes(5))
                                .build()
                );
            }

            CreateEmailAuthenticationCommand command = new CreateEmailAuthenticationCommand(
                            email,
                            LocalDateTime.now().plusMinutes(5)
                    );

            // WHEN
            assertThatThrownBy(() -> emailAuthenticationCommandService.create(command))
                    .isInstanceOf(CoreException.class)
                    .hasMessageContaining("짧은 시간 내 많은 이메일 인증 요청입니다.");
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
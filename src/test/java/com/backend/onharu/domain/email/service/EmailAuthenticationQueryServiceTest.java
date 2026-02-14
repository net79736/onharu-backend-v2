package com.backend.onharu.domain.email.service;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.backend.onharu.domain.email.dto.EmailAuthenticationQuery.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 이메일 인증 Query Service 테스트 코드 입니다.
 */
@SpringBootTest
class EmailAuthenticationQueryServiceTest {

    @Autowired
    private EmailAuthenticationQueryService emailAuthenticationQueryService;

    @Autowired
    private EmailAuthenticationJpaRepository emailAuthenticationJpaRepository;

    @BeforeEach
    void setUp() {
        emailAuthenticationJpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("이메일 인증 ID 로 인증 정보를 조회")
    class GetEmailAuthentication {

        @Test
        @DisplayName("ID 로 이메일 인증 조회 성공")
        void getEmailAuthentication() {
            // GIVEN
            String email = "test1234@test.com";
            LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);
            EmailAuthentication saved = emailAuthenticationJpaRepository.save(
                    EmailAuthentication.builder()
                            .email(email)
                            .expiredAt(expiredAt)
                            .build()
            );

            GetEmailAuthenticationByIdQuery query = new GetEmailAuthenticationByIdQuery(saved.getId());

            // WHEN
            EmailAuthentication result = emailAuthenticationQueryService.getEmailAuthentication(query);

            // THEN
            assertThat(result.getId()).isEqualTo(saved.getId());
            assertThat(result.getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회할 경우 예외 발생")
        void ShouldThrowExceptionWhenWrongId() {
            // GIVEN
            Long id = 123456789L;
            GetEmailAuthenticationByIdQuery query = new GetEmailAuthenticationByIdQuery(id);
            // WHEN
            assertThatThrownBy(() -> emailAuthenticationQueryService.getEmailAuthentication(query))
                    .isInstanceOf(CoreException.class);
        }
    }

    @Nested
    @DisplayName("이메일 인증 조회")
    class FindEmailAuthenticationByEmail {

        @Test
        @DisplayName("이메일 인증 조회 성공")
        void findEmailAuthenticationByEmail() {
            // GIVEN
            String email = "test12345@test.com";
            LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);
            emailAuthenticationJpaRepository.save(
                    EmailAuthentication.builder()
                            .email(email)
                            .expiredAt(expiredAt)
                            .build()
            );

            FindByEmailQuery query = new FindByEmailQuery(email);
            // WHEN
            Optional<EmailAuthentication> emailAuthentication = emailAuthenticationQueryService.findEmailAuthenticationByEmail(query);
            // THEN
            assertThat(emailAuthentication).isPresent();
            assertThat(emailAuthentication.get().getEmail()).isEqualTo(email);
        }
    }

    @Nested
    @DisplayName("이메일과 토큰으로 인증 정보 조회")
    class FindEmailAuthenticationByEmailAndToken {

        @Test
        @DisplayName("이메일과 토큰으로 조회 성공")
        void findEmailAuthenticationByEmailAndToken() {
            // GIVEN
            String email = "test123456@test.com";
            LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);
            EmailAuthentication saved = emailAuthenticationJpaRepository.save(
                    EmailAuthentication.builder()
                            .email(email)
                            .expiredAt(expiredAt)
                            .build()
            );

            FindByEmailAndTokenQuery query = new FindByEmailAndTokenQuery(saved.getEmail(), saved.getToken());
            // WHEN
            EmailAuthentication emailAuthentication = emailAuthenticationQueryService.findEmailAuthenticationByEmailAndToken(query);

            // then
            assertThat(emailAuthentication.getId()).isEqualTo(saved.getId());
        }

        @Test
        @DisplayName("잘못된 토큰으로 조회할 경우 예외 발생")
        void ShouldThrowExceptionWhenIsWrongToken() {
            // GIVEN
            String email = "test1234567@test.com";
            LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);
            EmailAuthentication saved = emailAuthenticationJpaRepository.save(
                    EmailAuthentication.builder()
                            .email(email)
                            .expiredAt(expiredAt)
                            .build()
            );

            FindByEmailAndTokenQuery query = new FindByEmailAndTokenQuery(saved.getEmail(), UUID.randomUUID().toString());
            // WHEN
            assertThatThrownBy(() -> emailAuthenticationQueryService.findEmailAuthenticationByEmailAndToken(query))
                    .isInstanceOf(CoreException.class);
        }
    }

    @Nested
    @DisplayName("만료된 이메일 인증 목록 조회")
    class FindAllByExpiredAtBefore {

        @Test
        @DisplayName("현재 시간 기준으로 만료된 인증 조회 성공")
        void findAllByExpiredAtBefore() {
            // GIVEN
            // 만료된 케이스
            String email1 = "test1@test.com";
            LocalDateTime expiredAt1 = LocalDateTime.now().minusDays(1);
            emailAuthenticationJpaRepository.save(
                    EmailAuthentication.builder()
                            .email(email1)
                            .expiredAt(expiredAt1)
                            .build()
            );

            // 만료되지 않는 케이스
            String email2 = "test2@test.com";
            LocalDateTime expiredAt2 = LocalDateTime.now().plusDays(1);
            emailAuthenticationJpaRepository.save(
                    EmailAuthentication.builder()
                            .email(email2)
                            .expiredAt(expiredAt2)
                            .build()
            );

            FindByExpiredQuery query = new FindByExpiredQuery(LocalDateTime.now());

            // WHEN
            List<EmailAuthentication> result = emailAuthenticationQueryService.findAllByExpiredAtBefore(query);

            // THEN
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo(email1);
        }
    }

    @Nested
    @DisplayName("이메일 인증 완료 확인")
    class ExistsByEmailAndIsVerifiedTrue {

        @Test
        @DisplayName("인증 완료시, isVerified 가 true")
        void shouldIsVerifiedTrueWhenVerify() {
            // GIVEN
            String email = "test999@test.com";
            LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);
            EmailAuthentication saved = emailAuthenticationJpaRepository.save(
                    EmailAuthentication.builder()
                            .email(email)
                            .expiredAt(expiredAt)
                            .build()
            );

            saved.verify(LocalDateTime.now()); // 인증 완료
            emailAuthenticationJpaRepository.save(saved);

            ExistsVerifiedByEmailQuery query = new ExistsVerifiedByEmailQuery(email, true);

            // WHEN
            boolean result = emailAuthenticationQueryService.existsByEmailAndIsVerifiedTrue(query);

            // THEN
            assertThat(result).isTrue();
        }
    }
}
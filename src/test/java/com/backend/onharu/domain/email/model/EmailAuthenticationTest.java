package com.backend.onharu.domain.email.model;

import com.backend.onharu.domain.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.backend.onharu.domain.support.error.ErrorType.EmailAuthentication.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 이메일 인증 도메인의 단위 테스트 코드 입니다.
 */
class EmailAuthenticationTest {

    @Nested
    @DisplayName("이메일 인증 생성")
    class CreateEmailAuthenticationTest {

        @Test
        @DisplayName("이메일 인증 생성 성공")
        void shouldCreateEmailAuthentication() {
            // GIVEN
            LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);

            // WHEN
            EmailAuthentication emailAuthentication = EmailAuthentication.builder()
                    .email("test1234@test.com")
                    .expiredAt(expiredAt)
                    .build();

            // THEN
            assertThat(emailAuthentication.getEmail()).isEqualTo("test1234@test.com");
            assertThat(emailAuthentication.getToken()).isNotNull();
            assertThat(emailAuthentication.isVerified()).isFalse();
            assertThat(emailAuthentication.getExpiredAt()).isEqualTo(expiredAt);
        }
    }

    @Nested
    @DisplayName("이메일 인증 재발급")
    class ReissueEmailAuthenticationTest {

        @Test
        @DisplayName("토큰 재발급 성공")
        void shouldReissueToken() {
            // GIVEN
            LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);

            EmailAuthentication emailAuthentication = EmailAuthentication.builder()
                    .email("test1234@test.com")
                    .expiredAt(expiredAt)
                    .build();

            String oldToken = emailAuthentication.getToken(); // 재발급 전 토큰
            LocalDateTime newExpiredAt = LocalDateTime.now().plusDays(10);

            // WHEN
            emailAuthentication.reissue(newExpiredAt); // 인증 재발급

            // THEN
            assertThat(emailAuthentication.getToken()).isNotEqualTo(oldToken);
            assertThat(emailAuthentication.isVerified()).isFalse();
            assertThat(emailAuthentication.getExpiredAt()).isEqualTo(newExpiredAt);
        }
    }

    @Nested
    @DisplayName("이메일 인증 검증")
    class VerifyEmailAuthenticationTest {

        @Test
        @DisplayName("이메일 인증 검증 성공")
        void shouldVerifyEmail() {
            // GIVEN
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiredAt = now.plusDays(5);

            EmailAuthentication emailAuthentication = EmailAuthentication.builder()
                    .email("test1234@test.com")
                    .expiredAt(expiredAt)
                    .build();

            String token = emailAuthentication.getToken();

            // WHEN
            emailAuthentication.verify(token, now);

            // THEN
            assertThat(emailAuthentication.isVerified()).isTrue();
        }

        @Test
        @DisplayName("이미 이메일 인증이 완료된 경우 예외 발생")
        void shouldThrowExceptionWhenAlreadyVerified() {
            // GIVEN
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiredAt = now.plusDays(5);

            EmailAuthentication emailAuthentication = EmailAuthentication.builder()
                    .email("test1234@test.com")
                    .expiredAt(expiredAt)
                    .build();

            String token = emailAuthentication.getToken();
            emailAuthentication.verify(token, now); // 1차 인증

            // WHEN
            assertThatThrownBy(() -> emailAuthentication.verify(token, now)) // 2차 인증
                    .isInstanceOf(CoreException.class)
                    .extracting(e -> ((CoreException) e).getErrorType())
                    .isEqualTo(EMAIL_ALREADY_VERIFIED);
        }

        @Test
        @DisplayName("인증 토큰이 만료된 경우 예외 발생")
        void shouldThrowExceptionWhenTokenExpired() {
            // GIVEN
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiredAt = now.minusDays(1);

            EmailAuthentication emailAuthentication = EmailAuthentication.builder()
                    .email("test1234@test.com")
                    .expiredAt(expiredAt)
                    .build();

            String token = emailAuthentication.getToken();

            // WHEN
            assertThatThrownBy(() -> emailAuthentication.verify(token, now))
                    .isInstanceOf(CoreException.class)
                    .extracting(e -> ((CoreException) e).getErrorType())
                    .isEqualTo(EMAIL_TOKEN_EXPIRED);
        }

        @Test
        @DisplayName("인증 토큰이 일치하지 않는 경우 예외 발생")
        void shouldThrowExceptionWhenTokenMismatch() {
            // GIVEN
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiredAt = now.plusDays(5);

            EmailAuthentication emailAuthentication = EmailAuthentication.builder()
                    .email("test1234@test.com")
                    .expiredAt(expiredAt)
                    .build();

            String invalidToken = "999999";

            // WHEN & THEN
            assertThatThrownBy(() -> emailAuthentication.verify(invalidToken, now))
                    .isInstanceOf(CoreException.class)
                    .extracting(e -> ((CoreException) e).getErrorType())
                    .isEqualTo(EMAIL_TOKEN_MISMATCH);
        }
    }
}
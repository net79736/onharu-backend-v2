package com.backend.onharu.domain.email.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.backend.onharu.domain.email.dto.EmailAuthenticationCommand.CompleteEmailAuthenticationCommand;
import static com.backend.onharu.domain.email.dto.EmailAuthenticationCommand.CreateEmailAuthenticationCommand;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * EmailAuthentication 도메인의 Command DTO 테스트 코드 입니다
 */
@SpringBootTest
class EmailAuthenticationCommandTest {

    @Nested
    @DisplayName("이메일 인증 생성 테스트")
    class CreateEmailAuthenticationCommandTest {

        @Test
        @DisplayName("이메일 인증 생성 Command 성공")
        void createEmailAuthenticationCommand() {
            // GIVEN
            String email = "test1234@test.com";
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(5);

            // WHEN
            CreateEmailAuthenticationCommand command = new CreateEmailAuthenticationCommand(email, expiredAt);

            // THEN
            assertThat(command.email()).isEqualTo(email);
            assertThat(command.expiredAt()).isEqualTo(expiredAt);
        }
    }

    @Nested
    @DisplayName("이메일 인증 처리 테스트")
    class CompleteEmailAuthenticationCommandTest {

        @Test
        @DisplayName("이메일 인증 처리 Command 성공")
        void completeEmailAuthenticationCommand() {
            // GIVEN
            String email = "test1234@test.com";
            String token = UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();

            // WHEN
            CompleteEmailAuthenticationCommand command = new CompleteEmailAuthenticationCommand(email, token, now);

            // THEN
            assertThat(command.email()).isEqualTo(email);
            assertThat(command.token()).isEqualTo(token);
            assertThat(command.now()).isEqualTo(now);
        }
    }
}
package com.backend.onharu.domain.email.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EmailAuthentication 도메인의 Query DTO 테스트 코드 입니다
 */
@SpringBootTest
class EmailAuthenticationQueryTest {

    @Nested
    @DisplayName("이메일 인증 ID 조회 Query")
    class GetEmailAuthenticationByIdQueryTest {

        @Test
        @DisplayName("이메일 인증 ID 로 인증 정보 단건 조회 Query 성공")
        void getEmailAuthenticationByIdQuery() {
            // GIVEN
            Long emailAuthenticationId = 1L;
            // WHEN
            EmailAuthenticationQuery.GetEmailAuthenticationByIdQuery query = new EmailAuthenticationQuery.GetEmailAuthenticationByIdQuery(emailAuthenticationId);
            // THEN
            assertThat(query.emailAuthenticationId()).isEqualTo(emailAuthenticationId);
        }
    }

    @Nested
    @DisplayName("이메일로 인증 정보 단건 조회 Query")
    class FindByEmailQueryTest {

        @Test
        @DisplayName("이메일로 인증 정보 단건 조회 Query 성공")
        void findByEmailQuery() {
            // GIVEN
            String email = "test1234@test.com";
            // WHEN
            EmailAuthenticationQuery.FindByEmailQuery query = new EmailAuthenticationQuery.FindByEmailQuery(email);
            // THEN
            assertThat(query.email()).isEqualTo(email);
        }
    }

    @Nested
    @DisplayName("이메일과 토큰으로 인증 정보 단건 조회 Query")
    class FindByEmailAndTokenQueryTest {

        @Test
        @DisplayName("이메일, 토큰으로 조회 Query 성공")
        void findByEmailAndTokenQuery() {
            // GIVEN
            String email = "test1234@test.com";
            String token = UUID.randomUUID().toString();
            // WHEN
            EmailAuthenticationQuery.FindByEmailAndTokenQuery query = new EmailAuthenticationQuery.FindByEmailAndTokenQuery(email, token);
            // THEN
            assertThat(query.email()).isEqualTo(email);
            assertThat(query.token()).isEqualTo(token);
        }
    }

    @Nested
    @DisplayName("현재 시간 기준으로 만료된 인증 정보 목록 조회 Query")
    class FindByExpiredQuery {

        @Test
        @DisplayName("만료된 인증 Query 성공")
        void findByExpiredQuery() {
            // GIVEN
            LocalDateTime now = LocalDateTime.now();
            // WHEN
            EmailAuthenticationQuery.FindByExpiredQuery query = new EmailAuthenticationQuery.FindByExpiredQuery(now);
            // THEN
            assertThat(query.now()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("이메일 인증 완료 여부 확인 Query")
    class ExistsVerifiedByEmailQuery {

        @Test
        @DisplayName("인증 완료 여부 Query 성공")
        void existsVerifiedByEmailQuery() {
            // GIVEN
            String email = "test1234@test.com";
            boolean isVerify = true;
            // WHEN
            EmailAuthenticationQuery.ExistsVerifiedByEmailQuery query = new EmailAuthenticationQuery.ExistsVerifiedByEmailQuery(email, isVerify);
            // THEN
            assertThat(query.email()).isEqualTo(email);
            assertThat(query.isVerify()).isEqualTo(isVerify);
        }
    }
}
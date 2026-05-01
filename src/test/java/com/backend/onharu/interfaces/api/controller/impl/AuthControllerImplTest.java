package com.backend.onharu.interfaces.api.controller.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import com.backend.onharu.infra.email.EmailSendService;
import com.backend.onharu.infra.nts.NtsBusinessNumber;
import com.backend.onharu.infra.security.LocalUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController MockMvc 테스트")
class AuthControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private NtsBusinessNumber ntsBusinessNumber;

    @MockBean
    private EmailSendService emailSendService;

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    private User createActiveUser(String name, String phone, String rawPassword) {
        return userJpaRepository.save(
                User.builder()
                        .loginId("auth-c-" + UUID.randomUUID().toString().substring(0, 8))
                        .password(passwordEncoder.encode(rawPassword))
                        .name(name)
                        .phone(phone)
                        .userType(UserType.CHILD)
                        .statusType(StatusType.ACTIVE)
                        .providerType(ProviderType.LOCAL)
                        .build());
    }

    @Nested
    @DisplayName("POST /api/auth/business-number")
    class CheckBusinessNumber {

        @Test
        @DisplayName("NtsBusinessNumber 가 true 반환 시 응답 true")
        void checkBusinessNumber_true() throws Exception {
            given(ntsBusinessNumber.isValid("1208800767")).willReturn(true);

            mockMvc.perform(post("/api/auth/business-number")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"businessNumber\":\"1208800767\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/find-id")
    class FindId {

        @Test
        @DisplayName("이름 + 전화번호로 사용자 찾으면 loginId 반환")
        void findId_success() throws Exception {
            User user = createActiveUser("홍길동", "01011112222", "password123");

            String body = """
                    {
                      "name": "홍길동",
                      "phone": "01011112222"
                    }
                    """;

            mockMvc.perform(post("/api/auth/find-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.loginId").value(user.getLoginId()));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/reset-password")
    class ResetPassword {

        @Test
        @DisplayName("사용자가 존재하면 200 + 임시 비밀번호 메일 발송")
        void resetPassword_success() throws Exception {
            User user = createActiveUser("김리셋", "01022223333", "oldPass1!");
            willDoNothing().given(emailSendService).sendResetPasswordEmail(any(), any());

            String body = """
                    {
                      "loginId": "%s",
                      "name": "김리셋",
                      "phone": "01022223333"
                    }
                    """.formatted(user.getLoginId());

            mockMvc.perform(post("/api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/email/send-code")
    class SendEmailCode {

        @Test
        @DisplayName("이메일 인증 생성 + 이메일 발송 200")
        void sendEmailCode_success() throws Exception {
            willDoNothing().given(emailSendService).sendEmail(any(), any());

            mockMvc.perform(post("/api/auth/email/send-code")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"test@example.com\"}"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/validate-password")
    class ValidatePassword {

        @Test
        @DisplayName("인증된 사용자의 올바른 비밀번호는 true 응답")
        void validatePassword_correct_returnsTrue() throws Exception {
            User user = createActiveUser("김밸리", "01033334444", "MyPass123!");

            LocalUser principal = new LocalUser(user, 1L);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

            mockMvc.perform(post("/api/auth/validate-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"password\":\"MyPass123!\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));
        }
    }
}

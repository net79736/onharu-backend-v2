package com.backend.onharu.interfaces.api.controller.impl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import com.backend.onharu.infra.security.LocalUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("UserController MockMvc 테스트")
class UserControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private LevelJpaRepository levelJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    private User createLocalUser(UserType type, String rawPassword) {
        return userJpaRepository.save(
                User.builder()
                        .loginId("user-" + UUID.randomUUID().toString().substring(0, 8))
                        .password(passwordEncoder.encode(rawPassword))
                        .name("테스트" + type.name())
                        .phone("01011112222")
                        .userType(type)
                        .statusType(StatusType.ACTIVE)
                        .providerType(ProviderType.LOCAL)
                        .build());
    }

    private void authenticateAs(User user, Long domainId) {
        LocalUser principal = new LocalUser(user, domainId);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Nested
    @DisplayName("POST /api/users/signup/owner")
    class SignUpOwner {

        @Test
        @DisplayName("비기너 등급 존재 시 사업자 회원가입 성공 201")
        void signUpOwner_success() throws Exception {
            levelJpaRepository.save(Level.builder().name("비기너").conditionNumber(1).build());

            String body = """
                    {
                      "loginId": "owner123@naver.com",
                      "password": "Pass1234!",
                      "passwordConfirm": "Pass1234!",
                      "name": "사업자",
                      "phone": "01011112222",
                      "businessNumber": "1208800767"
                    }
                    """;

            mockMvc.perform(post("/api/users/signup/owner")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.userId").exists());
        }

        @Test
        @DisplayName("비밀번호 확인 불일치 시 예외 발생 (400 또는 500)")
        void signUpOwner_passwordMismatch() throws Exception {
            levelJpaRepository.save(Level.builder().name("비기너").conditionNumber(1).build());

            String body = """
                    {
                      "loginId": "owner999@naver.com",
                      "password": "Pass1234!",
                      "passwordConfirm": "DIFFERENT999!",
                      "name": "사업자",
                      "phone": "01011112222",
                      "businessNumber": "1208800767"
                    }
                    """;

            mockMvc.perform(post("/api/users/signup/owner")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(result -> {
                        int s = result.getResponse().getStatus();
                        if (s < 400) {
                            throw new AssertionError("expected 4xx/5xx, got " + s);
                        }
                    });
        }
    }

    @Nested
    @DisplayName("POST /api/users/signup/child")
    class SignUpChild {

        @Test
        @DisplayName("아동 회원가입 성공 201")
        void signUpChild_success() throws Exception {
            String body = """
                    {
                      "loginId": "child123@naver.com",
                      "password": "Pass1234!",
                      "passwordConfirm": "Pass1234!",
                      "name": "아동",
                      "phone": "01011112222",
                      "nickname": "꼬마"
                    }
                    """;

            mockMvc.perform(post("/api/users/signup/child")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.userId").exists())
                    .andExpect(jsonPath("$.data.loginId").value("child123@naver.com"));
        }

        @Test
        @DisplayName("loginId 가 이메일 형식이 아니면 400")
        void signUpChild_invalidEmail() throws Exception {
            String body = """
                    {
                      "loginId": "not-an-email",
                      "password": "Pass1234!",
                      "passwordConfirm": "Pass1234!",
                      "name": "아동",
                      "phone": "01011112222",
                      "nickname": "꼬마"
                    }
                    """;

            mockMvc.perform(post("/api/users/signup/child")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/users/login")
    class Login {

        @Test
        @DisplayName("올바른 아이디/비밀번호로 로그인 200")
        void login_success() throws Exception {
            Level level = levelJpaRepository.save(Level.builder().name("비기너").conditionNumber(1).build());
            User owner = createLocalUser(UserType.OWNER, "CorrectPass1!");
            ownerJpaRepository.save(
                    Owner.builder().user(owner).level(level).businessNumber("1208800767").build());

            String body = """
                    {
                      "loginId": "%s",
                      "password": "CorrectPass1!"
                    }
                    """.formatted(owner.getLoginId());

            mockMvc.perform(post("/api/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("로그인 성공"));
        }
    }

    @Nested
    @DisplayName("GET /api/users/me")
    class GetMe {

        @Test
        @DisplayName("인증된 사용자는 본인의 정보를 반환받는다")
        void getMe_authenticated_returnsUser() throws Exception {
            User user = createLocalUser(UserType.OWNER, "Pass1234!");
            authenticateAs(user, 1L);

            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.loginId").value(user.getLoginId()));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users")
    class DeleteUser {

        @Test
        @DisplayName("인증된 사용자의 상태를 DELETED 로 전환")
        void deleteUser_success() throws Exception {
            User user = createLocalUser(UserType.CHILD, "Pass1234!");
            childJpaRepository.save(Child.builder().user(user).nickname("꼬마").build());
            authenticateAs(user, 1L);

            mockMvc.perform(delete("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("회원 탈퇴 성공"));
        }
    }

    @Nested
    @DisplayName("POST /api/users/logout")
    class Logout {

        @Test
        @DisplayName("인증된 사용자의 로그아웃 200")
        void logout_authenticated() throws Exception {
            User user = createLocalUser(UserType.CHILD, "Pass1234!");
            authenticateAs(user, 1L);

            mockMvc.perform(post("/api/users/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value("로그아웃 성공"));
        }

        @Test
        @DisplayName("인증 없이 로그아웃 요청도 200 반환")
        void logout_unauthenticated() throws Exception {
            mockMvc.perform(post("/api/users/logout"))
                    .andExpect(status().isOk());
        }
    }
}

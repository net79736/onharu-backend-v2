package com.backend.onharu.interfaces.api.controller.impl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.notification.model.Notification;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.notification.NotificationJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import com.backend.onharu.infra.security.LocalUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("NotificationController MockMvc 테스트")
class NotificationControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private NotificationJpaRepository notificationJpaRepository;

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    private User createUserAndAuth() {
        User user = userJpaRepository.save(
                User.builder()
                        .loginId("noti-c-" + UUID.randomUUID().toString().substring(0, 8))
                        .password("encoded")
                        .name("알림유저")
                        .phone("01011112222")
                        .userType(UserType.CHILD)
                        .statusType(StatusType.ACTIVE)
                        .providerType(ProviderType.LOCAL)
                        .build());

        LocalUser principal = new LocalUser(user, 1L);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        return user;
    }

    @Test
    @DisplayName("GET /api/notifications/me — 알림 설정이 있으면 반환 200")
    void getNotification_existing_returnsOk() throws Exception {
        User user = createUserAndAuth();
        notificationJpaRepository.save(
                Notification.builder().user(user).isSystemEnabled(true).build());

        mockMvc.perform(get("/api/notifications/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.notificationResponse").exists());
    }

    @Test
    @DisplayName("PUT /api/notifications/me — 알림 설정 변경 200")
    void updateNotification_success() throws Exception {
        User user = createUserAndAuth();
        notificationJpaRepository.save(
                Notification.builder().user(user).isSystemEnabled(false).build());

        String body = """
                {
                  "isSystemEnabled": true
                }
                """;

        mockMvc.perform(put("/api/notifications/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/notifications/histories — 알림 이력 빈 목록 200")
    void getNotificationHistories_empty() throws Exception {
        createUserAndAuth();

        mockMvc.perform(get("/api/notifications/histories")
                        .param("pageNum", "1")
                        .param("perPage", "10")
                        .param("sortField", "createdAt")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/notifications/histories/read/all — 일괄 읽음 처리 200")
    void markAllAsRead_returnsOk() throws Exception {
        createUserAndAuth();

        mockMvc.perform(put("/api/notifications/histories/read/all"))
                .andExpect(status().isOk());
    }
}

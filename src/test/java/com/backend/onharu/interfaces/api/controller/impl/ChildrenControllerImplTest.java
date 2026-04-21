package com.backend.onharu.interfaces.api.controller.impl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import com.backend.onharu.infra.security.LocalUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ChildrenController MockMvc 테스트")
class ChildrenControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    private Child createChildAndAuth() {
        User user = userJpaRepository.save(
                User.builder()
                        .loginId("c-" + UUID.randomUUID().toString().substring(0, 8))
                        .password("encoded")
                        .name("아동")
                        .phone("01011112222")
                        .userType(UserType.CHILD)
                        .statusType(StatusType.ACTIVE)
                        .providerType(ProviderType.LOCAL)
                        .build());
        Child child = childJpaRepository.save(Child.builder().user(user).nickname("꼬마").build());

        LocalUser principal = new LocalUser(user, child.getId());
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
        return child;
    }

    @Nested
    @DisplayName("cards stub 엔드포인트")
    class CardsEndpoints {

        @Test
        @DisplayName("POST /api/childrens/cards — 201")
        void issueCard() throws Exception {
            mockMvc.perform(post("/api/childrens/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("PUT /api/childrens/cards/{id} — 200")
        void updateCard() throws Exception {
            mockMvc.perform(put("/api/childrens/cards/{id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("DELETE /api/childrens/cards/{id} — 200")
        void deleteCard() throws Exception {
            mockMvc.perform(delete("/api/childrens/cards/{id}", 1L))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/childrens/cards/{id}/reissue — 200")
        void reissueCard() throws Exception {
            mockMvc.perform(post("/api/childrens/cards/{id}/reissue", 1L))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/childrens/cards/{id} — 200")
        void getMyCard() throws Exception {
            mockMvc.perform(get("/api/childrens/cards/{id}", 1L))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("certificate stub 엔드포인트")
    class CertificateEndpoints {

        @Test
        @DisplayName("POST /api/childrens/certificate — 201")
        void uploadCertificate() throws Exception {
            MockMultipartFile req = new MockMultipartFile(
                    "request", "", MediaType.APPLICATION_JSON_VALUE, "{}".getBytes());
            MockMultipartFile file = new MockMultipartFile(
                    "file", "cert.pdf", "application/pdf", "x".getBytes());

            mockMvc.perform(multipart("/api/childrens/certificate").file(req).file(file))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("DELETE /api/childrens/certificate/{id} — 200")
        void removeCertificate() throws Exception {
            mockMvc.perform(delete("/api/childrens/certificate/{id}", 1L))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/childrens/certificate/{id} — 200")
        void getCertificate() throws Exception {
            mockMvc.perform(get("/api/childrens/certificate/{id}", 1L))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/childrens/reservations")
    class MyBookings {

        @Test
        @DisplayName("예약 없으면 빈 목록 200")
        void getMyBookings_empty() throws Exception {
            createChildAndAuth();

            mockMvc.perform(get("/api/childrens/reservations")
                            .param("pageNum", "1")
                            .param("perPage", "10")
                            .param("statusFilter", "ALL")
                            .param("sortField", "createdAt")
                            .param("sortDirection", "desc"))
                    .andExpect(status().isOk());
        }
    }
}

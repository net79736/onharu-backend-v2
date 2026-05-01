package com.backend.onharu.interfaces.api.controller.impl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import com.backend.onharu.infra.security.LocalUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("OwnerController MockMvc 테스트")
class OwnerControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private LevelJpaRepository levelJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    private Owner createOwnerAndAuth() {
        Level level = levelJpaRepository.save(Level.builder().name("비기너").conditionNumber(1).build());
        User user = userJpaRepository.save(
                User.builder()
                        .loginId("owner-" + UUID.randomUUID().toString().substring(0, 8))
                        .password("encoded-password")
                        .name("사업자")
                        .phone("01011112222")
                        .userType(UserType.OWNER)
                        .statusType(StatusType.ACTIVE)
                        .providerType(ProviderType.LOCAL)
                        .build());
        Owner owner = ownerJpaRepository.save(
                Owner.builder().user(user).level(level).businessNumber("1208800767").build());

        LocalUser principal = new LocalUser(user, owner.getId());
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
        return owner;
    }

    @Nested
    @DisplayName("stub 엔드포인트")
    class StubEndpoints {

        @Test
        @DisplayName("POST /api/owners/business — 201 반환 (stub)")
        void registerBusiness_returnsCreated() throws Exception {
            createOwnerAndAuth();

            MockMultipartFile request = new MockMultipartFile(
                    "request", "", MediaType.APPLICATION_JSON_VALUE,
                    "{\"businessNumber\":\"1208800767\"}".getBytes());
            MockMultipartFile file = new MockMultipartFile(
                    "businessRegistrationFile", "biz.pdf", "application/pdf", "dummy".getBytes());

            mockMvc.perform(multipart("/api/owners/business").file(request).file(file))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("PUT /api/owners/{id}/business — 200 반환 (stub)")
        void updateBusiness_returnsOk() throws Exception {
            Owner owner = createOwnerAndAuth();

            MockMultipartFile request = new MockMultipartFile(
                    "request", "", MediaType.APPLICATION_JSON_VALUE,
                    "{\"businessNumber\":\"1208800767\"}".getBytes());
            MockMultipartFile file = new MockMultipartFile(
                    "businessRegistrationFile", "biz.pdf", "application/pdf", "dummy".getBytes());

            mockMvc.perform(multipart("/api/owners/{id}/business", owner.getId())
                            .file(request).file(file)
                            .with(m -> { m.setMethod("PUT"); return m; }))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("DELETE /api/owners/business/{id} — 200 반환 (stub)")
        void closeBusiness_returnsOk() throws Exception {
            Owner owner = createOwnerAndAuth();

            mockMvc.perform(delete("/api/owners/business/{id}", owner.getId()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/owners/business/{id} — 200 반환 (stub)")
        void getMyBusiness_returnsOk() throws Exception {
            Owner owner = createOwnerAndAuth();

            mockMvc.perform(get("/api/owners/business/{id}", owner.getId()))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/owners/stores")
    class GetMyStores {

        @Test
        @DisplayName("사업자에게 가게가 없으면 빈 목록 + 200")
        void getMyStores_emptyList() throws Exception {
            createOwnerAndAuth();

            mockMvc.perform(get("/api/owners/stores")
                            .param("pageNum", "1")
                            .param("perPage", "10")
                            .param("sortField", "createdAt")
                            .param("sortDirection", "desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.stores").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/owners/reservations/summary")
    class GetBookingSummary {

        @Test
        @DisplayName("예약 없는 사업자는 빈 summary 응답 200")
        void getBookingSummary_empty() throws Exception {
            createOwnerAndAuth();

            mockMvc.perform(get("/api/owners/reservations/summary"))
                    .andExpect(status().isOk());
        }
    }
}

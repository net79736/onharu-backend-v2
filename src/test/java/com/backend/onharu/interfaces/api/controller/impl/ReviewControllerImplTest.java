package com.backend.onharu.interfaces.api.controller.impl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
@DisplayName("ReviewController MockMvc 테스트")
class ReviewControllerImplTest {

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
                        .loginId("child-rev-" + UUID.randomUUID().toString().substring(0, 8))
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
    @DisplayName("GET /api/reviews")
    class GetAllReviews {

        @Test
        @DisplayName("리뷰 없으면 빈 페이지 응답 200")
        void getAllReviews_empty_returnsOk() throws Exception {
            mockMvc.perform(get("/api/reviews")
                            .param("pageNum", "1")
                            .param("perPage", "10")
                            .param("sortField", "createdAt")
                            .param("sortDirection", "desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.reviews").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/reviews/stores/{storeId}")
    class GetStoreReviews {

        @Test
        @DisplayName("존재하지 않는 가게 ID 라도 빈 목록 + 200")
        void getStoreReviews_nonExistentStore_returnsOk() throws Exception {
            mockMvc.perform(get("/api/reviews/stores/{storeId}", 999_999L)
                            .param("pageNum", "1")
                            .param("perPage", "10")
                            .param("sortField", "createdAt")
                            .param("sortDirection", "desc"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/reviews/my")
    class GetMyReviews {

        @Test
        @DisplayName("인증된 아동의 리뷰 목록 조회 (빈 목록) 200")
        void getMyReviews_empty_returnsOk() throws Exception {
            createChildAndAuth();

            mockMvc.perform(get("/api/reviews/my")
                            .param("pageNum", "1")
                            .param("perPage", "10")
                            .param("sortField", "createdAt")
                            .param("sortDirection", "desc"))
                    .andExpect(status().isOk());
        }
    }
}

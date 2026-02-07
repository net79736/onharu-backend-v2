package com.backend.onharu.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.user.dto.UserQuery.GetUserByIdQuery;
import com.backend.onharu.domain.user.dto.UserQuery.GetUserByLoginIdQuery;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@SpringBootTest
@DisplayName("UserQueryService 단위 테스트")
class UserQueryServiceTest {

    @Autowired
    private UserQueryService userQueryService;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @BeforeEach
    public void setUp() {
        userJpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("사용자 ID로 조회 테스트")
    class GetUserTest {

        @Test
        @DisplayName("조회 실패 - 사용자 ID가 존재하지 않는 경우")
        public void shouldThrowExceptionWhenUserIsNotFound() {
            // given
            Long userId = 99L;

            // when
            CoreException coreException = Assertions.assertThrows(
                CoreException.class,
                () -> userQueryService.getUser(new GetUserByIdQuery(userId))
            );

            // then
            assertThat(coreException.getErrorType()).isEqualTo(ErrorType.User.USER_NOT_FOUND);
            assertThat(coreException.getMessage()).isEqualTo(ErrorType.User.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("조회 성공")
        @Rollback(value = false)
        public void shouldGetUser() {
            // given
            User savedUser = userJpaRepository.save(
                User.builder()
                    .loginId("test_user_query")
                    .password("password123")
                    .name("테스트 사용자 조회")
                    .phone("01012345678")
                    .userType(UserType.CHILD)
                    .providerType(ProviderType.LOCAL)
                    .statusType(StatusType.ACTIVE)
                    .build()
            ); // 테스트용 사용자 생성

            // when
            User user = userQueryService.getUser(
                new GetUserByIdQuery(savedUser.getId())
            ); // 사용자 ID로 사용자 조회

            // then
            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(savedUser.getId());
            assertThat(user.getLoginId()).isEqualTo("test_user_query");
            assertThat(user.getName()).isEqualTo("테스트 사용자 조회");

            System.out.println("✅ 사용자 조회 성공 - User ID: " + user.getId());
            System.out.println("   - 로그인 ID: " + user.getLoginId());
            System.out.println("   - 이름: " + user.getName());
            System.out.println("   - 전화번호: " + user.getPhone());
            System.out.println("   - 사용자 유형: " + user.getUserType());
            System.out.println("   - 상태: " + user.getStatusType());
        }
    }

    @Nested
    @DisplayName("로그인 ID로 조회 테스트")
    class GetUserByLoginIdTest {

        @Test
        @DisplayName("조회 성공 - 로그인 ID로 사용자 조회")
        @Rollback(value = false)
        public void shouldGetUserByLoginId() {
            // given
            userJpaRepository.save(
                User.builder()
                    .loginId("test_login_id")
                    .password("password123")
                    .name("테스트 사용자 로그인")
                    .phone("01087654321")
                    .userType(UserType.OWNER)
                    .providerType(ProviderType.LOCAL)
                    .statusType(StatusType.ACTIVE)
                    .build()
            ); // 테스트용 사용자 생성

            // when
            User user = userQueryService.getUserByLoginId(
                new GetUserByLoginIdQuery("test_login_id")
            ); // 로그인 ID로 사용자 조회

            // then
            assertThat(user).isNotNull();
            assertThat(user.getLoginId()).isEqualTo("test_login_id");
            assertThat(user.getName()).isEqualTo("테스트 사용자 로그인");

            System.out.println("✅ 로그인 ID로 사용자 조회 성공");
            System.out.println("   - 로그인 ID: " + user.getLoginId());
            System.out.println("   - 사용자 ID: " + user.getId());
            System.out.println("   - 이름: " + user.getName());
            System.out.println("   - 사용자 유형: " + user.getUserType());
        }

        @Test
        @DisplayName("조회 실패 - 존재하지 않는 로그인 ID")
        public void shouldThrowExceptionWhenLoginIdNotFound() {
            // given
            String loginId = "non_existent_id";

            // when
            CoreException coreException = Assertions.assertThrows(
                CoreException.class,
                () -> userQueryService.getUserByLoginId(new GetUserByLoginIdQuery(loginId))
            );

            // then
            assertThat(coreException.getErrorType()).isEqualTo(ErrorType.User.USER_NOT_FOUND);
            assertThat(coreException.getMessage()).isEqualTo(ErrorType.User.USER_NOT_FOUND.getMessage());
        }
    }
}

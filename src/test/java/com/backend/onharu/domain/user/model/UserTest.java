package com.backend.onharu.domain.user.model;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.backend.onharu.domain.support.error.ErrorType.User.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("User 단위 테스트")
class UserTest {

    /**
     * 테스트용 User 엔티티 생성
     */
    private User createUser() {
        return User.builder()
                .loginId("test123@test.com")
                .password("password123!")
                .name("김이박")
                .phone("01033335555")
                .userType(UserType.CHILD)
                .statusType(StatusType.ACTIVE)
                .providerType(ProviderType.LOCAL)
                .build();
    }

    @Nested
    @DisplayName("사용자 정보 업데이트 테스트")
    class verifyUpdate {

        @Test
        @DisplayName("이름이 비어있으면 예외 발생")
        void shouldThrowExceptionWhenNameBlank() {
            // GIVEN
            User user = createUser();

            // WHEN
            CoreException exception = assertThrows(CoreException.class, () -> user.verifyUpdate("", "01012345678"));

            // THEN
            assertEquals(USER_NAME_MUST_NOT_BE_BLANK, exception.getErrorType());
        }

        @Test
        @DisplayName("전화번호 형식이 올바르지 않으면 예외 발생")
        void shouldThrowExceptionWhenPhoneInvalid() {
            // GIVEN
            User user = User.builder()
                    .loginId("test1234@test.com")
                    .password("password123!")
                    .name("김이박2")
                    .phone("01022224444")
                    .userType(UserType.CHILD)
                    .statusType(StatusType.ACTIVE)
                    .providerType(ProviderType.LOCAL)
                    .build();

            // WHEN
            CoreException exception = assertThrows(CoreException.class, () -> user.verifyUpdate("홍길동", "1234"));

            // THEN
            assertEquals(PHONE_INVALID_FORMAT, exception.getErrorType());
        }

        @Test
        @DisplayName("사용자 정보를 업데이트 성공")
        void shouldUpdate() {
            User user = User.builder()
                    .loginId("test12345@test.com")
                    .password("password123!")
                    .name("김이박3")
                    .phone("01055557777")
                    .userType(UserType.CHILD)
                    .statusType(StatusType.ACTIVE)
                    .providerType(ProviderType.LOCAL)
                    .build();

            user.verifyUpdate("김철수", "01099998888");

            assertEquals("김철수", user.getName());
            assertEquals("01099998888", user.getPhone());
        }
    }

    @Nested
    @DisplayName("비밀번호 변경 테스트")
    class changePassword {

        @Test
        @DisplayName("비밀번호 변경시 인코딩되어 저장된다")
        void shouldEncodePasswordWhenChangePassword() {
            // GIVEN
            User user = createUser();

            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class); // 비밀번호 암호화 mock 객체 생성

            // WHEN
            when(passwordEncoder.encode("password123!")).thenReturn("encodedPassword123!");
            user.changePassword("password123!", passwordEncoder);

            // THEN
            assertEquals("encodedPassword123!", user.getPassword());
        }
    }

    @Nested
    @DisplayName("비밀번호 검증 테스트")
    class verifyPassword {

        @Test
        @DisplayName("비밀번호가 일치하지 않으면 예외 발생")
        void shouldThrowExceptionWhenPasswordMismatch() {
            // GIVEN
            User user = createUser();

            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class); // 비밀번호 암호화 mock 객체 생성

            // WHEN
            when(passwordEncoder.matches("password123!", "encodedPassword123!")).thenReturn(false);

            // THEN
            CoreException exception = assertThrows(CoreException.class,
                    () -> user.verifyPassword("password123!", passwordEncoder)
            );

            assertEquals(LOGIN_ID_OR_PASSWORD_MISMATCH, exception.getErrorType());
        }
    }

    @Nested
    @DisplayName("계정 상태 검증 테스트")
    class verifyStatus {

        @Test
        @DisplayName("DELETED 상태이면 예외 발생")
        void shouldThrowExceptionWhenDeleted() {
            // GIVEN
            User user = createUser();

            // 사용자 상태를 DELETED 로 만듦
            user.changeStatus(StatusType.DELETED);

            // WHEN
            CoreException exception = assertThrows(CoreException.class, user::verifyStatus);

            // THEN
            assertEquals(USER_STATUS_DELETED, exception.getErrorType());
        }

        @Test
        @DisplayName("LOCKED 상태를 가진 사용자 계정 검증시 예외 발생")
        public void verifyStatus_fail_whenStatusTypeIsLOCKED() {
            // GIVEN
            User lockedUser = User.builder()
                    .loginId("test12345@test.com")
                    .password("test1234!")
                    .name("테스트")
                    .phone("01011112222")
                    .userType(UserType.CHILD)
                    .statusType(StatusType.LOCKED)
                    .build();

            // WHEN, THEN
            assertThatThrownBy(lockedUser::verifyStatus).isInstanceOf(CoreException.class);
        }

        @Test
        @DisplayName("BLOCKED 상태를 가진 사용자 계정 검증시 예외 발생")
        public void verifyStatus_fail_whenStatusTypeIsBLOCKED() {
            // GIVEN
            User blockedUser = User.builder()
                    .loginId("test12346@test.com")
                    .password("test1234!")
                    .name("테스트")
                    .phone("01011112222")
                    .userType(UserType.CHILD)
                    .statusType(StatusType.BLOCKED)
                    .build();

            // WHEN, THEN
            assertThatThrownBy(blockedUser::verifyStatus).isInstanceOf(CoreException.class);
        }
    }

    @Nested
    @DisplayName("사용자 타입 변경 테스트")
    class changeUserType {

        @Test
        @DisplayName("NONE 상태에서 CHILD로 변경 성공")
        void shouldChangeToChildWhenNone() {
            // GIVEN
            User user = User.builder()
                    .loginId("test1234@test.com")
                    .password("test1234!")
                    .name("테스트1")
                    .phone("01011112222")
                    .userType(UserType.NONE)
                    .statusType(StatusType.PENDING)
                    .build();

            // WHEN
            user.changeUserTypeToChild();

            // THEN
            assertEquals(UserType.CHILD, user.getUserType());
        }

        @Test
        @DisplayName("NONE이 아닐 경우 타입 변경시 예외 발생")
        void shouldThrowExceptionWhenUserTypeNotNone() {
            // GIVEN
            User user = User.builder()
                    .loginId("test12345@test.com")
                    .password("test1234!")
                    .name("테스트2")
                    .phone("01033334444")
                    .userType(UserType.NONE)
                    .statusType(StatusType.PENDING)
                    .build();

            user.changeUserTypeToChild(); // CHILD로 변경

            // WHEN
            CoreException exception = assertThrows(CoreException.class, user::changeUserTypeToOwner); // OWNER 로 변경

            // THEN
            assertEquals(USER_TYPE_NOT_CHANGE, exception.getErrorType());
        }
    }
}
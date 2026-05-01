package com.backend.onharu.domain.user.dto;

import com.backend.onharu.domain.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.backend.onharu.domain.support.error.ErrorType.Child.CHILD_ID_MUST_NOT_BE_NULL;
import static com.backend.onharu.domain.support.error.ErrorType.Owner.OWNER_ID_MUST_NOT_BE_NULL;
import static com.backend.onharu.domain.support.error.ErrorType.User.*;
import static com.backend.onharu.domain.user.dto.UserQuery.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserQuery 단위 테스트")
class UserQueryTest {

    @Nested
    @DisplayName("GetUserByIdQuery 생성 테스트")
    class GetUserByIdQueryTest {

        @Test
        @DisplayName("id가 null이면 예외 발생")
        void shouldThrowExceptionWhenIdNull() {
            CoreException exception = assertThrows(CoreException.class, () -> new GetUserByIdQuery(null));

            assertEquals(USER_ID_MUST_NOT_BE_NULL, exception.getErrorType());
        }

        @Test
        @DisplayName("GetUserByIdQuery 생성 성공")
        void shouldCreate() {
            Long userId = 1L;

            GetUserByIdQuery query = new GetUserByIdQuery(userId);

            assertEquals(userId, query.id());
        }
    }

    @Nested
    @DisplayName("GetUserByLoginIdQuery 생성 테스트")
    class GetUserByLoginIdQueryTest {

        @Test
        @DisplayName("loginId가 null이면 예외 발생")
        void shouldThrowExceptionWhenLoginIdNull() {
            CoreException exception = assertThrows(CoreException.class, () -> new GetUserByLoginIdQuery(null));

            assertEquals(LOGIN_ID_MUST_NOT_BE_BLANK, exception.getErrorType());
        }

        @Test
        @DisplayName("loginId가 blank이면 예외 발생")
        void shouldThrowExceptionWhenLoginIdBlank() {
            CoreException exception = assertThrows(CoreException.class, () -> new GetUserByLoginIdQuery(" "));

            assertEquals(LOGIN_ID_MUST_NOT_BE_BLANK, exception.getErrorType());
        }

        @Test
        @DisplayName("GetUserByLoginIdQuery 생성 성공")
        void shouldCreate() {
            String loginId = "test1234@test.com";

            GetUserByLoginIdQuery query = new GetUserByLoginIdQuery(loginId);

            assertEquals(loginId, query.loginId());
        }
    }

    @Nested
    @DisplayName("GetUserByNameAndPhoneQuery 생성 테스트")
    class GetUserByNameAndPhoneQueryTest {

        @Test
        @DisplayName("name이 blank이면 예외 발생")
        void shouldThrowExceptionWhenNameBlank() {
            CoreException exception = assertThrows(CoreException.class, () -> new GetUserByNameAndPhoneQuery(" ", "01011112222"));

            assertEquals(USER_NAME_MUST_NOT_BE_BLANK, exception.getErrorType());
        }

        @Test
        @DisplayName("phone이 blank이면 예외 발생")
        void shouldThrowExceptionWhenPhoneBlank() {
            CoreException exception = assertThrows(CoreException.class, () -> new GetUserByNameAndPhoneQuery("테스트이름", " "));

            assertEquals(PHONE_MUST_NOT_BE_BLANK, exception.getErrorType());
        }

        @Test
        @DisplayName("GetUserByNameAndPhoneQuery 생성 성공")
        void shouldCreate() {
            String name = "테스트이름";
            String phone = "01011112222";

            GetUserByNameAndPhoneQuery query = new GetUserByNameAndPhoneQuery(name, phone);

            assertEquals(name, query.name());
            assertEquals(phone, query.phone());
        }
    }

    @Nested
    @DisplayName("GetChildProfileQuery 생성 테스트")
    class GetChildProfileQueryTest {

        @Test
        @DisplayName("userId가 null이면 예외 발생")
        void shouldThrowExceptionWhenUserIdNull() {
            CoreException exception = assertThrows(CoreException.class, () -> new GetChildProfileQuery(null, 1L));

            assertEquals(USER_ID_MUST_NOT_BE_NULL, exception.getErrorType());
        }

        @Test
        @DisplayName("childId가 null이면 예외 발생")
        void shouldThrowExceptionWhenChildIdNull() {
            CoreException exception = assertThrows(CoreException.class, () -> new GetChildProfileQuery(1L, null));

            assertEquals(CHILD_ID_MUST_NOT_BE_NULL, exception.getErrorType());
        }

        @Test
        @DisplayName("GetChildProfileQuery 생성 성공")
        void shouldCreate() {
            Long userId = 1L;
            Long childId = 2L;

            GetChildProfileQuery query = new GetChildProfileQuery(userId, childId);

            assertEquals(userId, query.userId());
            assertEquals(childId, query.childId());
        }
    }

    @Nested
    @DisplayName("GetOwnerProfileQuery 생성 테스트")
    class GetOwnerProfileQueryTest {

        @Test
        @DisplayName("ownerId가 null이면 예외 발생")
        void shouldThrowExceptionWhenOwnerIdNull() {
            CoreException exception = assertThrows(CoreException.class, () -> new GetOwnerProfileQuery(1L, null));

            assertEquals(OWNER_ID_MUST_NOT_BE_NULL, exception.getErrorType());
        }

        @Test
        @DisplayName("GetOwnerProfileQuery 생성 성공")
        void shouldCreate() {
            Long userId = 1L;
            Long ownerId = 3L;

            GetOwnerProfileQuery query = new GetOwnerProfileQuery(userId, ownerId);

            assertEquals(userId, query.userId());
            assertEquals(ownerId, query.ownerId());
        }
    }

    @Nested
    @DisplayName("ValidatePasswordQuery 생성 테스트")
    class ValidatePasswordQueryTest {

        @Test
        @DisplayName("password가 blank이면 예외 발생")
        void shouldThrowExceptionWhenPasswordBlank() {
            CoreException exception = assertThrows(CoreException.class, () -> new ValidatePasswordQuery(1L, " "));

            assertEquals(PASSWORD_MUST_NOT_BE_BLANK, exception.getErrorType());
        }

        @Test
        @DisplayName("ValidatePasswordQuery 생성 성공")
        void shouldCreate() {
            Long userId = 1L;
            String password = "password123!";

            ValidatePasswordQuery query = new ValidatePasswordQuery(userId, password);

            assertEquals(userId, query.userId());
            assertEquals(password, query.password());
        }
    }
}
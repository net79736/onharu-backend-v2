package com.backend.onharu.domain.user.dto;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.backend.onharu.domain.support.error.ErrorType.Child.NICKNAME_MUST_NOT_BE_BLANK;
import static com.backend.onharu.domain.support.error.ErrorType.Owner.BUSINESS_NUMBER_MUST_NOT_BE_BLANK;
import static com.backend.onharu.domain.support.error.ErrorType.User.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("UserCommand 단위 테스트")
class UserCommandTest {

    @Nested
    @DisplayName("SignUpChildCommand 생성 테스트")
    class SignUpChildCommandTest {

        @Test
        @DisplayName("passwordConfirm 불일치 시 예외 발생")
        void shouldThrowWhenPasswordMismatch() {
            CoreException exception = assertThrows(CoreException.class,
                    () -> new UserCommand.SignUpChildCommand(
                            "test@test.com",
                            "password123!",
                            "password999!",
                            "홍길동",
                            "01011112222",
                            "닉네임",
                            List.of()
                    ));

            assertEquals(PASSWORD_CONFIRM_MISMATCH, exception.getErrorType());
        }

        @Test
        @DisplayName("nickname blank 시 예외 발생")
        void shouldThrowWhenNicknameBlank() {
            CoreException exception = assertThrows(CoreException.class,
                    () -> new UserCommand.SignUpChildCommand(
                            "test@test.com",
                            "password123!",
                            "password123!",
                            "홍길동",
                            "01011112222",
                            " ",
                            List.of()
                    ));

            assertEquals(NICKNAME_MUST_NOT_BE_BLANK, exception.getErrorType());
        }

        @Test
        @DisplayName("SignUpChildCommand 생성 성공")
        void shouldCreate() {
            UserCommand.SignUpChildCommand command =
                    new UserCommand.SignUpChildCommand(
                            "test@test.com",
                            "password123!",
                            "password123!",
                            "홍길동",
                            "01011112222",
                            "닉네임",
                            List.of()
                    );

            assertEquals("test@test.com", command.loginId());
        }
    }

    @Nested
    @DisplayName("SignUpOwnerCommand 생성 테스트")
    class SignUpOwnerCommandTest {

        @Test
        @DisplayName("businessNumber blank 시 예외 발생")
        void shouldThrowWhenBusinessNumberBlank() {
            CoreException exception = assertThrows(CoreException.class,
                    () -> new UserCommand.SignUpOwnerCommand(
                            "owner@test.com",
                            "password123!",
                            "password123!",
                            "사업자",
                            "01022223333",
                            " "
                    ));

            assertEquals(BUSINESS_NUMBER_MUST_NOT_BE_BLANK, exception.getErrorType());
        }

        @Test
        @DisplayName("SignUpOwnerCommand 생성 성공")
        void shouldCreate() {
            UserCommand.SignUpOwnerCommand command =
                    new UserCommand.SignUpOwnerCommand(
                            "owner@test.com",
                            "password123!",
                            "password123!",
                            "사업자",
                            "01022223333",
                            "1234567890"
                    );

            assertEquals("1234567890", command.businessNumber());
        }
    }

    @Nested
    @DisplayName("CreateUserCommand 생성 테스트")
    class CreateUserCommandTest {

        @Test
        @DisplayName("userType null 시 예외 발생")
        void shouldThrowWhenUserTypeNull() {
            CoreException exception = assertThrows(CoreException.class,
                    () -> new UserCommand.CreateUserCommand(
                            "test@test.com",
                            "password123!",
                            "홍길동",
                            "01011112222",
                            null,
                            StatusType.ACTIVE,
                            ProviderType.LOCAL
                    ));

            assertEquals(USER_TYPE_MUST_NOT_BE_NULL, exception.getErrorType());
        }

        @Test
        @DisplayName("CreateUserCommand 생성 성공")
        void shouldCreate() {
            UserCommand.CreateUserCommand command =
                    new UserCommand.CreateUserCommand(
                            "test@test.com",
                            "password123!",
                            "홍길동",
                            "01011112222",
                            UserType.CHILD,
                            StatusType.ACTIVE,
                            ProviderType.LOCAL
                    );

            assertEquals(UserType.CHILD, command.userType());
        }
    }

    @Nested
    @DisplayName("LoginUserCommand 생성 테스트")
    class LoginUserCommandTest {

        @Test
        @DisplayName("password blank 시 예외 발생")
        void shouldThrowWhenPasswordBlank() {
            CoreException exception = assertThrows(CoreException.class,
                    () -> new UserCommand.LoginUserCommand("test@test.com", " "));

            assertEquals(PASSWORD_MUST_NOT_BE_BLANK, exception.getErrorType());
        }

        @Test
        @DisplayName("LoginUserCommand 생성 성공")
        void shouldCreate() {
            UserCommand.LoginUserCommand command =
                    new UserCommand.LoginUserCommand("test@test.com", "password");

            assertEquals("test@test.com", command.loginId());
        }
    }

    @Nested
    @DisplayName("ChangePasswordCommand 생성 테스트")
    class ChangePasswordCommandTest {

        @Test
        @DisplayName("새 비밀번호 불일치 시 예외 발생")
        void shouldThrowWhenNewPasswordMismatch() {
            CoreException exception = assertThrows(CoreException.class,
                    () -> new UserCommand.ChangePasswordCommand(
                            1L,
                            "oldPass",
                            "newPass1",
                            "newPass2"
                    ));

            assertEquals(PASSWORD_CONFIRM_MISMATCH, exception.getErrorType());
        }

        @Test
        @DisplayName("ChangePasswordCommand 생성 성공")
        void shouldCreate() {
            UserCommand.ChangePasswordCommand command =
                    new UserCommand.ChangePasswordCommand(
                            1L,
                            "oldPass",
                            "newPass",
                            "newPass"
                    );

            assertEquals(1L, command.userId());
        }
    }
}
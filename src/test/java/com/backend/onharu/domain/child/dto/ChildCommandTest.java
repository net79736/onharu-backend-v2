package com.backend.onharu.domain.child.dto;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.backend.onharu.domain.child.dto.ChildCommand.CreateChildCommand;
import static com.backend.onharu.domain.support.error.ErrorType.Child.NICKNAME_MUST_NOT_BE_BLANK;
import static com.backend.onharu.domain.support.error.ErrorType.User.USER_ID_MUST_NOT_BE_NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ChildCommand 단위 테스트")
class ChildCommandTest {

    /**
     * User 엔티티 생성
     */
    private User createUser() {
        return User.builder()
                .loginId("test1234@test.com")
                .password("password123!")
                .name("김이박")
                .phone("01011112222")
                .userType(UserType.CHILD)
                .statusType(StatusType.ACTIVE)
                .providerType(ProviderType.LOCAL)
                .build();
    }

    @Nested
    @DisplayName("CreateChildCommand 생성 테스트")
    class CreateChildCommandTest {

        @Test
        @DisplayName("user가 null이면 예외 발생")
        void shouldThrowExceptionWhenUserNull() {
            // GIVEN, WHEN
            CoreException exception = assertThrows(CoreException.class,
                    () -> new CreateChildCommand(
                            null,
                            "테스트닉네임"
                    ));

            // THEN
            assertEquals(USER_ID_MUST_NOT_BE_NULL, exception.getErrorType());
        }

        @Test
        @DisplayName("nickname이 null이면 예외 발생")
        void shouldThrowExceptionWhenNicknameNull() {
            User user = createUser();

            CoreException exception = assertThrows(CoreException.class,
                    () -> new CreateChildCommand(
                            user,
                            null
                    ));

            assertEquals(NICKNAME_MUST_NOT_BE_BLANK, exception.getErrorType());
        }

        @Test
        @DisplayName("정상 값이면 생성 성공")
        void shouldCreate() {
            User user = User.builder()
                    .loginId("test123456@test.com")
                    .password("password123!")
                    .name("김이박최정")
                    .phone("01033334444")
                    .userType(UserType.CHILD)
                    .statusType(StatusType.ACTIVE)
                    .providerType(ProviderType.LOCAL)
                    .build();

            CreateChildCommand command =
                    new CreateChildCommand(
                            user,
                            "온하루"
                    );

            assertEquals(user, command.user());
            assertEquals("온하루", command.nickname());
        }
    }
}
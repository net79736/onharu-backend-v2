package com.backend.onharu.domain.owner.dto;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.backend.onharu.domain.support.error.ErrorType.Owner.BUSINESS_NUMBER_MUST_NOT_BE_BLANK;
import static com.backend.onharu.domain.support.error.ErrorType.Owner.LEVEL_ID_MUST_NOT_BE_NULL;
import static com.backend.onharu.domain.support.error.ErrorType.User.USER_ID_MUST_NOT_BE_NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("OwnerCommand 단위 테스트")
class OwnerCommandTest {

    @Nested
    @DisplayName("CreateOwnerCommand 생성 테스트")
    class CreateOwnerCommandTest {

        @Test
        @DisplayName("user가 null이면 예외 발생")
        void shouldThrowExceptionWhenUserNull() {
            // GIVEN
            Level level = Level.builder()
                    .name("비기너")
                    .build();
            String businessNumber = "1234567890";

            // WHEN
            CoreException exception = assertThrows(CoreException.class, () -> new OwnerCommand.CreateOwnerCommand(null, level, businessNumber));

            // THEN
            assertEquals(USER_ID_MUST_NOT_BE_NULL, exception.getErrorType());
        }

        @Test
        @DisplayName("level이 null이면 예외 발생")
        void shouldThrowExceptionWhenLevelNull() {
            // GIVEN
            User user = User.builder()
                    .loginId("test123@test.com")
                    .password("password123!")
                    .name("이름테스트1")
                    .phone("01011112222")
                    .userType(UserType.OWNER)
                    .providerType(ProviderType.LOCAL)
                    .statusType(StatusType.ACTIVE)
                    .build();
            String businessNumber = "1234567890";

            // WHEN
            CoreException exception = assertThrows(CoreException.class, () -> new OwnerCommand.CreateOwnerCommand(user, null, businessNumber));

            // THEN
            assertEquals(LEVEL_ID_MUST_NOT_BE_NULL, exception.getErrorType());
        }

        @Test
        @DisplayName("businessNumber가 null이면 예외 발생")
        void shouldThrowExceptionWhenBusinessNumberNull() {
            // GIVEN
            User user = User.builder()
                    .loginId("test1234@test.com")
                    .password("password123!")
                    .name("이름테스트2")
                    .phone("01022223333")
                    .userType(UserType.OWNER)
                    .providerType(ProviderType.LOCAL)
                    .statusType(StatusType.ACTIVE)
                    .build();

            Level level = Level.builder()
                    .name("새싹")
                    .build();

            // WHEN
            CoreException exception = assertThrows(CoreException.class,
                    () -> new OwnerCommand.CreateOwnerCommand(user, level, null));

            // THEN
            assertEquals(BUSINESS_NUMBER_MUST_NOT_BE_BLANK, exception.getErrorType());
        }

        @Test
        @DisplayName("businessNumber가 blank이면 예외 발생")
        void shouldThrowExceptionWhenBusinessNumberBlank() {
            // GIVEN
            User user = User.builder()
                    .loginId("test12345@test.com")
                    .password("password123!")
                    .name("이름테스트3")
                    .phone("01033334444")
                    .userType(UserType.OWNER)
                    .providerType(ProviderType.LOCAL)
                    .statusType(StatusType.ACTIVE)
                    .build();

            Level level = Level.builder()
                    .name("새싹2")
                    .build();

            // WHEN
            CoreException exception = assertThrows(CoreException.class, () -> new OwnerCommand.CreateOwnerCommand(user, level, " "));

            // THEN
            assertEquals(BUSINESS_NUMBER_MUST_NOT_BE_BLANK, exception.getErrorType());
        }

        @Test
        @DisplayName("CreateOwnerCommand 생성 성공")
        void shouldCreate() {
            // GIVEN
            User user = User.builder()
                    .loginId("test123456@test.com")
                    .password("password123!")
                    .name("이름테스트4")
                    .phone("01044445555")
                    .userType(UserType.OWNER)
                    .providerType(ProviderType.LOCAL)
                    .statusType(StatusType.ACTIVE)
                    .build();

            Level level = Level.builder()
                    .name("새싹3")
                    .build();

            String businessNumber = "1234567890";

            // WHEN
            OwnerCommand.CreateOwnerCommand command = new OwnerCommand.CreateOwnerCommand(user, level, businessNumber);

            // THEN
            assertEquals(user, command.user());
            assertEquals(level, command.level());
            assertEquals(businessNumber, command.businessNumber());
        }
    }

    @Nested
    @DisplayName("checkBusinessNumberCommand 생성 테스트")
    class CheckBusinessNumberCommandTest {

        @Test
        @DisplayName("checkBusinessNumberCommand 생성 성공")
        void shouldCreate() {
            // GIVEN
            String businessNumber = "1234567890";

            // WHEN
            OwnerCommand.checkBusinessNumberCommand command = new OwnerCommand.checkBusinessNumberCommand(businessNumber);

            // THEN
            assertEquals(businessNumber, command.businessNumber());
        }
    }
}
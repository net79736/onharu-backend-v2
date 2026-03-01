package com.backend.onharu.domain.owner.model;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.backend.onharu.domain.support.error.ErrorType.Owner.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 도메인 Owner 의 단위 테스트 코드 입니다.
 */
@DisplayName("Owner 단위 테스트")
class OwnerTest {

    /**
     * 테스트용 User 엔티티 생성
     */
    private User createUser() {
        return User.builder()
                .loginId("test1234@test.com")
                .password("password123!")
                .name("테스트")
                .phone("01011112222")
                .userType(UserType.OWNER)
                .statusType(StatusType.ACTIVE)
                .providerType(ProviderType.LOCAL)
                .build();
    }

    /**
     * 테스트용 Level 엔티티 생성
     */
    private Level createLevel() {
        return Level.builder()
                .name("비기너")
                .build();
    }

    /**
     * 테스트용 Owner 엔티티 생성
     */
    private Owner createOwner() {
        User user = createUser();
        Level level = createLevel();

        return Owner.builder()
                .user(user)
                .level(level)
                .businessNumber("0000000000")
                .build();
    }

    @Nested
    @DisplayName("사업자 정보 업데이트 테스트")
    class verifyUpdate {

        @Test
        @DisplayName("사업자 번호가 비어있는 경우 예외 발생")
        void shouldThrowExceptionWhenVerifyUpdateWhenBusinessNumberBlank() {
            // GIVEN
            Owner owner = createOwner();

            // WHEN
            CoreException exception = assertThrows(CoreException.class, () -> owner.verifyAndUpdate(""));

            // THEN
            assertEquals(BUSINESS_NUMBER_MUST_NOT_BE_BLANK, exception.getErrorType());
        }

        @Test
        @DisplayName("사업자 번호가 10자리가 아니면 예외 발생")
        void verifyUpdate_fail_whenNotTenDigits() {
            // GIVEN
            Owner owner = createOwner();

            // WHEN
            CoreException exception = assertThrows(CoreException.class, () -> owner.verifyAndUpdate("12345"));

            // THEN
            assertEquals(BUSINESS_NUMBER_MUST_BE_TEN_DIGITS, exception.getErrorType());
        }
    }


    @Nested
    @DisplayName("사업자 등급 변경 테스트")
    class changeLevel {

        @Test
        @DisplayName("사업자에 있는 등급과 같은 등급으로 변경시 예외 발생")
        void shouldThrowExceptionWhenChangeLevelIsSameLevel() {
            // GIVEN
            User user = createUser();

            Level level = createLevel();

            Owner owner = Owner.builder()
                    .user(user)
                    .level(level)
                    .businessNumber("0000000000")
                    .build();

            // WHEN
            CoreException exception = assertThrows(CoreException.class, () -> owner.changeLevel(level));

            // THEN
            assertEquals(SAME_LEVEL_CAN_NOT_BE_ASSIGNED, exception.getErrorType());
        }

        @Test
        @DisplayName("사업자에 있는 등급과 다른 등급으로 변경시 정상 동작")
        void shouldWhenChangeLevelIsDifferentLevel() {
            // GIVEN
            User user = createUser();

            Level oldLevel = createLevel();

            Level newLevel = Level.builder()
                    .name("새싹")
                    .build();

            Owner owner = Owner.builder()
                    .user(user)
                    .level(oldLevel)
                    .businessNumber("0000000000")
                    .build();

            // WHEN
            owner.changeLevel(newLevel);

            // THEN
            assertEquals(newLevel, owner.getLevel());
        }
    }
}
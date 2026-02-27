package com.backend.onharu.domain.child.model;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.backend.onharu.domain.support.error.ErrorType.Child.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 도메인 Child 의 단위 테스트 코드 입니다.
 */
@DisplayName("Child 단위 테스트")
class ChildTest {

    /**
     * 테스트용 User 엔티티 생성
     */
    private User createUser() {
        return User.builder()
                .loginId("childTest123@test.com")
                .password("password123!")
                .name("아동테스트")
                .phone("01022223333")
                .userType(UserType.CHILD)
                .statusType(StatusType.ACTIVE)
                .providerType(ProviderType.LOCAL)
                .build();
    }

    /**
     * 테스트용 Child 엔티티 생성
     */
    private Child createChild() {
        User user = createUser();

        return Child.builder()
                .user(user)
                .nickname("닉네임테스트")
                .isVerified(false)
                .build();
    }

    @Nested
    @DisplayName("아동 도메인 생성 테스트")
    class create {
        @Test
        @DisplayName("아동 도메인 생성시 승인 여부가 없을 경우")
        void shouldCreateChildWhenIsVerifiedNull() {
            User testUser = User.builder()
                    .loginId("test1234@test.com")
                    .password("test1234!")
                    .name("테스트")
                    .phone("01011112222")
                    .userType(UserType.CHILD)
                    .statusType(StatusType.ACTIVE)
                    .build();

            Child child = Child.builder()
                    .user(testUser)
                    .nickname("테스트닉네임")
                    .isVerified(null)
                    .build();

            assertThat(child.getIsVerified()).isFalse();
        }
    }

    @Nested
    @DisplayName("아동 닉네임 업데이트 테스트")
    class verifyUpdate {

        @Test
        @DisplayName("닉네임이 비어있으면 예외 발생")
        void shouldThrowExceptionWhenNicknameBlank() {
            // GIVEN
            Child child = createChild();

            // WHEN
            CoreException exception = assertThrows(CoreException.class, () -> child.verifyAndUpdate(""));

            // THEN
            assertEquals(NICKNAME_MUST_NOT_BE_BLANK, exception.getErrorType());
        }

        @Test
        @DisplayName("닉네임이 100자를 초과하면 예외 발생")
        void shouldThrowExceptionWhenNicknameTooLong() {
            // GIVEN
            Child child = createChild();
            String longNickname = "1".repeat(101);

            // WHEN
            CoreException exception = assertThrows(CoreException.class, () -> child.verifyAndUpdate(longNickname));

            // THEN
            assertEquals(NICKNAME_MUST_BE_NO_MORE_THAN_100_CHARACTERS_LONG, exception.getErrorType());
        }

        @Test
        @DisplayName("닉네임 형식이 올바르지 않으면 예외 발생")
        void shouldThrowExceptionWhenNicknameInvalidFormat() {
            // GIVEN
            Child child = createChild();

            // WHEN
            CoreException exception = assertThrows(CoreException.class, () -> child.verifyAndUpdate("특수문자!@#"));

            // THEN
            assertEquals(NICKNAME_INVALID_FORMAT, exception.getErrorType());
        }

        @Test
        @DisplayName("닉네임 변경 성공")
        void shouldUpdateNickname() {
            // GIVEN
            Child child = createChild();

            // WHEN
            child.verifyAndUpdate("닉네임테스트123");

            // THEN
            assertEquals("닉네임테스트123", child.getNickname());
        }
    }

    @Nested
    @DisplayName("아동 승인 여부 변경 테스트")
    class verify {

        @Test
        @DisplayName("승인 여부를 true로 변경하면 정상 동작")
        void shouldUpdateIsVerifiedToTrue() {
            // GIVEN
            Child child = createChild();

            assertFalse(child.getIsVerified());

            // WHEN
            child.verify(true);

            // THEN
            assertEquals(true, child.getIsVerified());
        }
    }
}
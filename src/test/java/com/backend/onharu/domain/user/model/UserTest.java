package com.backend.onharu.domain.user.model;

import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserTest {

    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("사용자 비밀번호가 일치하지 않을 경우 예외 발생")
    public void verifyPassword_fail_whenPasswordNotMatch() {
        User testUser = User.builder()
                .loginId("test1234@test.com")
                .password("test1234!")
                .name("테스트")
                .phone("01011112222")
                .userType(UserType.CHILD)
                .statusType(StatusType.ACTIVE)
                .build();

        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(
                () -> testUser.verifyPassword("password", passwordEncoder)
        ).isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("DELETE 상태를 가진 사용자 계정 검증시 예외 발생")
    public void verifyStatus_fail_whenStatusTypeIsDELETE() {
        User deletedUser = User.builder()
                .loginId("test1234@test.com")
                .password("test1234!")
                .name("테스트")
                .phone("01011112222")
                .userType(UserType.CHILD)
                .statusType(StatusType.DELETED)
                .build();

        assertThatThrownBy(deletedUser::verifyStatus)
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("LOCKED 상태를 가진 사용자 계정 검증시 예외 발생")
    public void verifyStatus_fail_whenStatusTypeIsLOCKED() {
        User lockedUser = User.builder()
                .loginId("test1234@test.com")
                .password("test1234!")
                .name("테스트")
                .phone("01011112222")
                .userType(UserType.CHILD)
                .statusType(StatusType.LOCKED)
                .build();

        assertThatThrownBy(lockedUser::verifyStatus)
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("BLOCKED 상태를 가진 사용자 계정 검증시 예외 발생")
    public void verifyStatus_fail_whenStatusTypeIsBLOCKED() {
        User blockedUser = User.builder()
                .loginId("test1234@test.com")
                .password("test1234!")
                .name("테스트")
                .phone("01011112222")
                .userType(UserType.CHILD)
                .statusType(StatusType.BLOCKED)
                .build();

        assertThatThrownBy(blockedUser::verifyStatus)
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("NONE 상태가 아닌 임시 사용자를 아동 타입으로 전환시 예외 발생")
    public void changeUserTypeToChild_fail_whenUserTypeNotNone() {
        User testUser = User.builder()
                .loginId("test1234@test.com")
                .password("test1234!")
                .name("테스트")
                .phone("01011112222")
                .userType(UserType.CHILD)
                .statusType(StatusType.PENDING)
                .build();

        assertThatThrownBy(testUser::changeUserTypeToOwner)
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("NONE 상태가 아닌 임시 사용자를 사업자 타입으로 전환시 예외 발생")
    public void changeUserTypeToOwner_fail_whenUserTypeNotNone() {
        User testUser = User.builder()
                .loginId("test1234@test.com")
                .password("test1234!")
                .name("테스트")
                .phone("01011112222")
                .userType(UserType.OWNER)
                .statusType(StatusType.PENDING)
                .build();

        assertThatThrownBy(testUser::changeUserTypeToOwner)
                .isInstanceOf(CoreException.class);
    }
}
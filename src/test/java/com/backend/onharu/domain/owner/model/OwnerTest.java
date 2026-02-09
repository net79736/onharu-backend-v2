package com.backend.onharu.domain.owner.model;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.backend.onharu.domain.support.error.ErrorType.Owner.SAME_LEVEL_CAN_NOT_BE_ASSIGNED;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 도메인 Owner 의 테스트 코드 입니다.
 */
class OwnerTest {

    @Test
    @DisplayName("사업자에 있는 등급과 같은 등급으로 변경시 예외 발생")
    void changeLevel_fail_whenSameLevel() {

        User user = User.builder()
                .loginId("test1234@test.com")
                .password("password123!")
                .name("테스트")
                .phone("01011112222")
                .userType(UserType.OWNER)
                .statusType(StatusType.ACTIVE)
                .providerType(ProviderType.LOCAL)
                .build();

        Level level = Level.builder()
                .name("새싹")
                .build();

        Owner owner = Owner.builder()
                .user(user)
                .level(level)
                .businessNumber("0000000000")
                .build();

        CoreException exception = assertThrows(
                CoreException.class,
                () -> {
                    owner.changeLevel(level);
                }
        );

        Assertions.assertEquals(
                SAME_LEVEL_CAN_NOT_BE_ASSIGNED,
                exception.getErrorType()
        );
    }

}
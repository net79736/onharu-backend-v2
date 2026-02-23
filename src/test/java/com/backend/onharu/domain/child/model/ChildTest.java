package com.backend.onharu.domain.child.model;

import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 도메인 Child 의 테스트 코드 입니다.
 */
class ChildTest {

    @Test
    @DisplayName("아동 도메인 생성시 승인 여부가 없을 경우")
    void create_child_whenIsVerifiedNull() {
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
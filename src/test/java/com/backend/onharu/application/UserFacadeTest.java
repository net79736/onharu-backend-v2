package com.backend.onharu.application;

import com.backend.onharu.domain.child.service.ChildCommandService;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.service.LevelQueryService;
import com.backend.onharu.domain.owner.service.OwnerCommandService;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.user.dto.UserCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpChildCommand;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.domain.user.service.UserCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.backend.onharu.domain.user.dto.UserCommand.SignUpOwnerCommand;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 사용자 Facade 의 테스트 코드 입니다.
 */
@ExtendWith(MockitoExtension.class)
class UserFacadeTest {

    @Mock
    private UserCommandService userCommandService;

    @Mock
    private OwnerCommandService ownerCommandService;

    @Mock
    private ChildCommandService childCommandService;

    @Mock
    private LevelQueryService levelQueryService;

    @InjectMocks
    private UserFacade userFacade;

    @Test
    @DisplayName("아동 회원가입중 User 생성 실패시 Child 객체가 생성되지 않는다.")
    void signUpChild_fail_whenUserNotFound() {
        SignUpChildCommand command = new SignUpChildCommand("test1234@test.com",
                "password123!",
                "password123!",
                "사업자테스트",
                "01011112222",
                "코끼리땃쥐",
                "/certification/file.pdf"
        );

        when(userCommandService.signUpChild(command))
                .thenThrow(new CoreException(ErrorType.User.USER_NOT_FOUND));

        assertThatThrownBy(() -> userFacade.signUpChild(command))
                .isInstanceOf(CoreException.class);

        verify(childCommandService, never()).createChild(any());
    }

    @Test
    @DisplayName("사업자 회원가입중 Level 조회 실패시 Owner 객체가 생성되지 않는다.")
    void signUpOwner_fail_whenLevelNotFound() {
        SignUpOwnerCommand command = new SignUpOwnerCommand("test1234@test.com",
                "password123!",
                "password123!",
                "사업자테스트",
                "01011112222",
                "테스트가게명",
                "1234567890",
                "1"
        );

        User user = User.builder()
                .loginId("test1234@test.com")
                .password("password123!")
                .name("테스트")
                .phone("01011112222")
                .providerType(ProviderType.LOCAL)
                .userType(UserType.OWNER)
                .statusType(StatusType.ACTIVE)
                .build();

        when(userCommandService.signUpOwner(command))
                .thenReturn(user);

        when(levelQueryService.getLevel(any()))
                .thenThrow(new CoreException(ErrorType.Level.LEVEL_NOT_FOUND));

        assertThatThrownBy(() -> userFacade.signUpOwner(command))
                .isInstanceOf(CoreException.class);

        verify(ownerCommandService, never()).createOwner(any());
    }
}
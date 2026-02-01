package com.backend.onharu.application;

import com.backend.onharu.domain.child.service.ChildCommandService;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.service.LevelQueryService;
import com.backend.onharu.domain.owner.service.OwnerCommandService;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpChildCommand;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.LoginUserOAuthCommand;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.domain.user.service.UserCommandService;
import com.backend.onharu.domain.user.service.UserOAuthCommandService;
import com.backend.onharu.domain.user.service.UserOAuthQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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

    @Mock
    private UserOAuthQueryService userOAuthQueryService;

    @Mock
    private UserOAuthCommandService userOAuthCommandService;

    @Mock
    private PasswordEncoder passwordEncoder;

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
    @DisplayName("아동 회원가입중 Child 생성 실패시 예외가 발생한다.")
    void signUpChild_fail_whenChildNotFound() {
        User user = mock(User.class);

        SignUpChildCommand command = new SignUpChildCommand("test1234@test.com",
                "password123!",
                "password123!",
                "사업자테스트",
                "01011112222",
                "코끼리땃쥐",
                "/certification/file.pdf"
        );

        when(userCommandService.signUpChild(command)).thenReturn(user);
        when(childCommandService.createChild(any()))
                .thenThrow(new CoreException(ErrorType.Child.CHILD_NOT_FOUND));

        assertThatThrownBy(() -> userFacade.signUpChild(command))
                .isInstanceOf(CoreException.class);

        verify(userCommandService).signUpChild(command);
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

        when(levelQueryService.getLevel(any()))
                .thenThrow(new CoreException(ErrorType.Level.LEVEL_NOT_FOUND));

        assertThatThrownBy(() -> userFacade.signUpOwner(command))
                .isInstanceOf(CoreException.class);

        verify(ownerCommandService, never()).createOwner(any());
    }

    @Test
    @DisplayName("최초 소셜 로그인 시도할 경우 User 와 UserOAuth 객체가 생성된다")
    void loginUserOAuth_success_whenFirstLogin() {
        LoginUserOAuthCommand command = new LoginUserOAuthCommand(
                "test@kakao.com",
                "테스트",
                "01011110000",
                ProviderType.KAKAO,
                "kakao"
        );

        User testUser = User.builder()
                .loginId(command.loginId())
                .password("password")
                .name(command.name())
                .phone(command.phoneNumber())
                .userType(UserType.NONE)
                .statusType(StatusType.PENDING)
                .build();

        when(passwordEncoder.encode(any())).thenReturn("test1234@");

        when(userOAuthQueryService.getUserByUserOAuthQuery(any()))
                .thenReturn(Optional.empty());

        when(userCommandService.createUser(any()))
                .thenReturn(testUser);

        userFacade.loginUserOAuth(command);

        verify(userCommandService).createUser(any());
        verify(userOAuthCommandService).createUserOAuth(any());
    }
}
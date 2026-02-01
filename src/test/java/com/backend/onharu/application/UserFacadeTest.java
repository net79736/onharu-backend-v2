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
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpChildCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpOwnerCommand;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@SpringBootTest
@DisplayName("UserFacade 단위 테스트")
class UserFacadeTest {

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Autowired
    private StoreScheduleJpaRepository storeScheduleJpaRepository;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @BeforeEach
    public void setUp() {
        // 외래 키 제약 조건을 고려한 삭제 순서 (자식 → 부모)
        reservationJpaRepository.deleteAll();
        storeScheduleJpaRepository.deleteAll();
        storeJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        childJpaRepository.deleteAll();
        ownerJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Nested
    @DisplayName("아동 회원가입 테스트")
    class SignUpChildTest {
        private String loginId = "test_child" + "_" + UUID.randomUUID().toString();

        @Test
        @DisplayName("아동 회원가입 성공")
        @Rollback(value = false)
        public void shouldSignUpChild() {
            
            // given
            SignUpChildCommand command = new SignUpChildCommand(
                loginId,
                "password123",
                "password123",
                "테스트 아동",
                "01012345678",
                "/certificates/test.pdf"
            );

            // when
            User user = userFacade.signUpChild(command);

            // then
            Assertions.assertNotNull(user);
            Assertions.assertNotNull(user.getId());
            Assertions.assertEquals(user.getLoginId(), loginId);
            Assertions.assertEquals(user.getName(), "테스트 아동");
            Assertions.assertEquals(user.getPhone(), "01012345678");
            Assertions.assertEquals(user.getUserType(), UserType.CHILD);
            Assertions.assertEquals(user.getProviderType(), ProviderType.LOCAL);
            Assertions.assertEquals(user.getStatusType(), StatusType.PENDING);
            
            // Child 엔티티도 생성되었는지 확인
            Child child = childJpaRepository.findByUser_LoginId(user.getLoginId()).orElse(null);
            Assertions.assertNotNull(child);
            Assertions.assertEquals(child.getUser().getId(), user.getId());
            Assertions.assertEquals(child.getCertificate(), "/certificates/test.pdf");
            
            System.out.println("✅ 아동 회원가입 성공 - User ID: " + user.getId());
            System.out.println("   - 로그인 ID: " + user.getLoginId());
            System.out.println("   - 이름: " + user.getName());
            System.out.println("   - 사용자 유형: " + user.getUserType());
            System.out.println("   - 상태: " + user.getStatusType());
            System.out.println("   - Child ID: " + child.getId());
        }

        @Test
        @DisplayName("아동 회원가입 실패 - 로그인 ID 중복")
        public void shouldThrowExceptionWhenLoginIdAlreadyExists() {
            // given
            userJpaRepository.save(
                User.builder()
                    .loginId(loginId)
                    .password("password123")
                    .name("기존 사용자")
                    .phone("01011111111")
                    .userType(UserType.CHILD)
                    .providerType(ProviderType.LOCAL)
                    .statusType(StatusType.ACTIVE)
                    .build()
            );
            
            SignUpChildCommand command = new SignUpChildCommand(
                loginId,
                "password123",
                "password123",
                "새로운 아동",
                "01022222222",
                "/certificates/new.pdf"
            );

            // when & then
            CoreException exception = Assertions.assertThrows(
                CoreException.class,
                () -> userFacade.signUpChild(command)
            );
            
            Assertions.assertEquals(exception.getErrorType(), ErrorType.User.USER_ID_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("사업자 회원가입 테스트")
    class SignUpOwnerTest {
        
        private String loginId = "test_owner" + "_" + UUID.randomUUID().toString();
        
        @Test
        @DisplayName("사업자 회원가입 성공")
        @Rollback(value = false)
        public void shouldSignUpOwner() {
            // given
            SignUpOwnerCommand command = new SignUpOwnerCommand(
                loginId,
                "password123",
                "password123",
                "테스트 사업자",
                "01087654321",
                "테스트 가게",
                "1234567890",
                "1"
            );

            // when
            User user = userFacade.signUpOwner(command);

            // then
            Assertions.assertNotNull(user);
            Assertions.assertNotNull(user.getId());
            Assertions.assertEquals(user.getLoginId(), loginId);
            Assertions.assertEquals(user.getName(), "테스트 사업자");
            Assertions.assertEquals(user.getPhone(), "01087654321");
            Assertions.assertEquals(user.getUserType(), UserType.OWNER);
            Assertions.assertEquals(user.getProviderType(), ProviderType.LOCAL);
            Assertions.assertEquals(user.getStatusType(), StatusType.PENDING);
            
            // Owner 엔티티도 생성되었는지 확인
            Owner owner = ownerJpaRepository.findByUser_LoginId(user.getLoginId()).orElse(null);
            Assertions.assertNotNull(owner);
            Assertions.assertEquals(owner.getUser().getId(), user.getId());
            Assertions.assertEquals(owner.getLevelId(), 1L);
            Assertions.assertEquals(owner.getBusinessNumber(), "1234567890");
            
            System.out.println("✅ 사업자 회원가입 성공 - User ID: " + user.getId());
            System.out.println("   - 로그인 ID: " + user.getLoginId());
            System.out.println("   - 이름: " + user.getName());
            System.out.println("   - 사용자 유형: " + user.getUserType());
            System.out.println("   - 상태: " + user.getStatusType());
            System.out.println("   - Owner ID: " + owner.getId());
            System.out.println("   - 사업자 번호: " + owner.getBusinessNumber());
        }

        @Test
        @DisplayName("사업자 회원가입 실패 - 로그인 ID 중복")
        public void shouldThrowExceptionWhenLoginIdAlreadyExists() {
            // given
            userJpaRepository.save(
                User.builder()
                    .loginId(loginId)
                    .password("password123")
                    .name("기존 사업자")
                    .phone("01011111111")
                    .userType(UserType.OWNER)
                    .providerType(ProviderType.LOCAL)
                    .statusType(StatusType.ACTIVE)
                    .build()
            );
            
            SignUpOwnerCommand command = new SignUpOwnerCommand(
                loginId,
                "password123",
                "password123",
                "새로운 사업자",
                "01022222222",
                "새로운 가게",
                "2234567890",
                "2"
            );

            // when & then
            CoreException exception = Assertions.assertThrows(
                CoreException.class,
                () -> userFacade.signUpOwner(command)
            );
            
            Assertions.assertEquals(exception.getErrorType(), ErrorType.User.USER_ID_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("사업자 회원가입 성공 - levelId가 문자열로 전달됨")
        @Rollback(value = false)
        public void shouldSignUpOwnerWithStringLevelId() {
            // given
            SignUpOwnerCommand command = new SignUpOwnerCommand(
                loginId,
                "password123",
                "password123",
                "테스트 사업자2",
                "01011112222",
                "테스트 가게2",
                "3334567890",
                "3"  // 문자열로 전달
            );

            // when
            User user = userFacade.signUpOwner(command);

            // then
            Assertions.assertNotNull(user);
            Assertions.assertNotNull(user.getId());
            
            // Owner 엔티티 확인
            Owner owner = ownerJpaRepository.findByUser_LoginId(user.getLoginId()).orElse(null);
            Assertions.assertNotNull(owner);
            Assertions.assertEquals(owner.getLevelId(), 3L);  // 문자열 "3"이 Long 3L로 변환되었는지 확인
            
            System.out.println("✅ 사업자 회원가입 성공 (문자열 levelId) - User ID: " + user.getId());
            System.out.println("   - Level ID: " + owner.getLevelId());
        }
    }
}

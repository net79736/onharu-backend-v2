package com.backend.onharu.domain.user.service;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.dto.UserCommand.CreateUserCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpChildCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpOwnerCommand;
import com.backend.onharu.domain.user.dto.UserQuery.GetUserByIdQuery;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import com.backend.onharu.domain.common.TestDataHelper;

import static com.backend.onharu.domain.support.error.ErrorType.User.USER_ID_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("UserCommandService 단위 테스트")
class UserCommandServiceTest {

    @Autowired
    private UserCommandService userCommandService;

    @Autowired
    private UserQueryService userQueryService;

    @Autowired


    private TestDataHelper testDataHelper;


    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private StoreScheduleJpaRepository storeScheduleJpaRepository;

    @BeforeEach
    void setUp() {

        testDataHelper.cleanAll();

    }

    @Nested
    @DisplayName("아동 회원가입 테스트")
    class SignUpChildTest {

        @Test
        @DisplayName("아동 회원가입 성공")
        void shouldSignUpChild() {
            // given
            SignUpChildCommand command = new SignUpChildCommand(
                    "test_child",
                    "password123",
                    "password123",
                    "테스트 아동",
                    "01012345678",
                    "닉네임테스트",
                    List.of() // 이미지 없음
            );

            // when
            User user = userCommandService.signUpChild(command);

            // then
            assertThat(user).isNotNull();
            assertThat(user.getId()).isNotNull();
            assertThat(user.getLoginId()).isEqualTo("test_child");
            assertThat(user.getName()).isEqualTo("테스트 아동");
            assertThat(user.getUserType()).isEqualTo(UserType.CHILD);
            assertThat(user.getProviderType()).isEqualTo(ProviderType.LOCAL);
            assertThat(user.getStatusType()).isEqualTo(StatusType.PENDING);

            // DB에 저장되었는지 확인
            User savedUser = userQueryService.getUser(
                    new GetUserByIdQuery(user.getId())
            );
            assertThat(savedUser).isNotNull();

            System.out.println("✅ 아동 회원가입 성공 - User ID: " + user.getId());
            System.out.println("   - 로그인 ID: " + user.getLoginId());
            System.out.println("   - 이름: " + user.getName());
            System.out.println("   - 사용자 유형: " + user.getUserType());
            System.out.println("   - 상태: " + user.getStatusType());
        }

        @Test
        @DisplayName("아동 회원가입 실패 - 로그인 ID 중복")
        void shouldThrowExceptionWhenLoginIdAlreadyExists() {
            // given
            userJpaRepository.save(
                    User.builder()
                            .loginId("duplicate_id")
                            .password("password123")
                            .name("기존 사용자")
                            .phone("01011111111")
                            .userType(UserType.CHILD)
                            .providerType(ProviderType.LOCAL)
                            .statusType(StatusType.ACTIVE)
                            .build()
            );

            SignUpChildCommand command = new SignUpChildCommand(
                    "duplicate_id",
                    "password123",
                    "password123",
                    "새로운 아동",
                    "01022222222",
                    "닉네임테스트",
                    List.of() // 이미지 없음
            );

            // when
            CoreException coreException = org.junit.jupiter.api.Assertions.assertThrows(
                    CoreException.class,
                    () -> userCommandService.signUpChild(command)
            );

            // then
            assertThat(coreException.getErrorType()).isEqualTo(USER_ID_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("사업자 회원가입 테스트")
    class SignUpOwnerTest {

        @Test
        @DisplayName("사업자 회원가입 성공")
        void shouldSignUpOwner() {
            // given
            SignUpOwnerCommand command = new SignUpOwnerCommand(
                    "test_owner",
                    "password123",
                    "password123",
                    "테스트 사업자",
                    "01087654321",
                    "1234567890"
            );

            // when
            User user = userCommandService.signUpOwner(command);

            // then
            assertThat(user).isNotNull();
            assertThat(user.getId()).isNotNull();
            assertThat(user.getLoginId()).isEqualTo("test_owner");
            assertThat(user.getName()).isEqualTo("테스트 사업자");
            assertThat(user.getUserType()).isEqualTo(UserType.OWNER);
            assertThat(user.getProviderType()).isEqualTo(ProviderType.LOCAL);
            assertThat(user.getStatusType()).isEqualTo(StatusType.PENDING);

            System.out.println("✅ 사업자 회원가입 성공 - User ID: " + user.getId());
            System.out.println("   - 로그인 ID: " + user.getLoginId());
            System.out.println("   - 이름: " + user.getName());
            System.out.println("   - 사용자 유형: " + user.getUserType());
            System.out.println("   - 상태: " + user.getStatusType());
        }
    }

    @Nested
    @DisplayName("사용자 생성 테스트")
    class CreateUserTest {

        @Test
        @DisplayName("사용자 생성 성공")
        void shouldCreateUser() {
            // given
            CreateUserCommand command = new CreateUserCommand(
                    "test_user",
                    "password123",
                    "테스트 사용자",
                    "01055556666",
                    UserType.CHILD,
                    StatusType.ACTIVE,
                    ProviderType.LOCAL
            );

            // when
            User user = userCommandService.createUser(command);

            // then
            assertThat(user).isNotNull();
            assertThat(user.getId()).isNotNull();
            assertThat(user.getLoginId()).isEqualTo("test_user");
            assertThat(user.getName()).isEqualTo("테스트 사용자");
            assertThat(user.getUserType()).isEqualTo(UserType.CHILD);
            assertThat(user.getStatusType()).isEqualTo(StatusType.ACTIVE);

            System.out.println("✅ 사용자 생성 성공 - User ID: " + user.getId());
            System.out.println("   - 로그인 ID: " + user.getLoginId());
            System.out.println("   - 이름: " + user.getName());
            System.out.println("   - 사용자 유형: " + user.getUserType());
            System.out.println("   - 제공자 유형: " + user.getProviderType());
            System.out.println("   - 상태: " + user.getStatusType());
        }
    }
}

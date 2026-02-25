package com.backend.onharu.application;

import java.util.List;
import java.util.UUID;

import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import org.junit.jupiter.api.*;
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
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
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

    @Autowired
    private LevelJpaRepository levelJpaRepository;

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
        levelJpaRepository.deleteAll();
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
                "테스트 닉네임",
                    List.of() // 이미지 없음
            );

            // when
            User user = userFacade.signUpChild(command);

            // then
            assertNotNull(user);
            assertNotNull(user.getId());
            assertEquals(user.getLoginId(), loginId);
            assertEquals(user.getName(), "테스트 아동");
            assertEquals(user.getPhone(), "01012345678");
            assertEquals(user.getUserType(), UserType.CHILD);
            assertEquals(user.getProviderType(), ProviderType.LOCAL);
            assertEquals(user.getStatusType(), StatusType.PENDING);

            // Child 엔티티도 생성되었는지 확인
            Child child = childJpaRepository.findByUser_LoginId(user.getLoginId()).orElse(null);
            assertNotNull(child);
            assertEquals(child.getUser().getId(), user.getId());

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
                    "테스트 닉네임",
                    List.of() // 이미지 없음
            );

            // when & then
            CoreException exception = assertThrows(
                CoreException.class,
                () -> userFacade.signUpChild(command)
            );

            assertEquals(exception.getErrorType(), ErrorType.User.USER_ID_ALREADY_EXISTS);
        }
    }

    @Nested
    @DisplayName("사업자 회원가입 테스트")
    class SignUpOwnerTest {

        @Test
        @DisplayName("사업자 회원가입 성공")
        public void shouldSignUpOwner() {
            // given
            // 기본 등급 저장
            Level level = levelJpaRepository.save(
                    Level.builder()
                            .name("비기너")
                            .build()
            );

            SignUpOwnerCommand command = new SignUpOwnerCommand(
                    "testOwner123@test.com",
                    "password123!",
                    "password123!",
                    "테스트 사업자",
                    "01087654321",
                    "1234567890"
            );

            // when
            User user = userFacade.signUpOwner(command);

            // then
            assertNotNull(user);
            assertNotNull(user.getId());
            assertEquals("testOwner123@test.com", user.getLoginId());
            assertEquals("테스트 사업자", user.getName());
            assertEquals("01087654321", user.getPhone());
            assertEquals(UserType.OWNER, user.getUserType());
            assertEquals(ProviderType.LOCAL, user.getProviderType());
            assertEquals(StatusType.PENDING, user.getStatusType());

            // Owner 엔티티도 생성되었는지 확인
            Owner owner = ownerJpaRepository.findByUser_LoginId(user.getLoginId()).orElse(null);
            assertNotNull(owner);
            assertEquals(user.getId(), owner.getUser().getId());
            assertEquals("비기너", owner.getLevel().getName()); // 생성한 등급명과 일치
            assertEquals("1234567890", owner.getBusinessNumber());

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
            // 기본 등급 저장
            Level level = levelJpaRepository.save(
                    Level.builder()
                            .name("비기너")
                            .build()
            );

            userJpaRepository.save(
                    User.builder()
                            .loginId("testOwner1234@test.com")
                            .password("password123")
                            .name("기존 사업자")
                            .phone("01011111111")
                            .userType(UserType.OWNER)
                            .providerType(ProviderType.LOCAL)
                            .statusType(StatusType.ACTIVE)
                            .build()
            );

            SignUpOwnerCommand command = new SignUpOwnerCommand(
                    "testOwner1234@test.com",
                    "password123",
                    "password123",
                    "새로운 사업자",
                    "01022222222",
                    "2234567890"
            );

            // when & then
            CoreException exception = assertThrows(
                    CoreException.class,
                    () -> userFacade.signUpOwner(command)
            );

            assertEquals(ErrorType.User.USER_ID_ALREADY_EXISTS, exception.getErrorType());
        }
    }
}

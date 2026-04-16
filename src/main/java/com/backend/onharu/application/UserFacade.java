package com.backend.onharu.application;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.application.dto.UserLogin;
import com.backend.onharu.domain.child.dto.ChildCommand.CreateChildCommand;
import com.backend.onharu.domain.child.dto.ChildCommand.UpdateChildCommand;
import com.backend.onharu.domain.child.dto.ChildQuery.GetChildByIdQuery;
import com.backend.onharu.domain.child.dto.ChildQuery.GetChildByUserIdQuery;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.service.ChildCommandService;
import com.backend.onharu.domain.child.service.ChildQueryService;
import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.dto.LevelQuery.FindFirstByConditionNumberQuery;
import com.backend.onharu.domain.level.dto.LevelQuery.GetLevelByNameQuery;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.level.service.LevelCommandService;
import com.backend.onharu.domain.level.service.LevelQueryService;
import com.backend.onharu.domain.owner.dto.OwnerCommand.CreateOwnerCommand;
import com.backend.onharu.domain.owner.dto.OwnerCommand.UpdateOwnerCommand;
import com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByIdQuery;
import com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByUserIdQuery;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.service.OwnerCommandService;
import com.backend.onharu.domain.owner.service.OwnerQueryService;
import com.backend.onharu.domain.store.dto.StoreQuery;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.service.StoreQueryService;
import com.backend.onharu.domain.user.dto.UserCommand.CreateUserCommand;
import com.backend.onharu.domain.user.dto.UserCommand.LoginUserCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpChildCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpOwnerCommand;
import com.backend.onharu.domain.user.dto.UserCommand.UpdateChildProfileCommand;
import com.backend.onharu.domain.user.dto.UserCommand.UpdateDeletedUserCommand;
import com.backend.onharu.domain.user.dto.UserCommand.UpdateOwnerProfileCommand;
import com.backend.onharu.domain.user.dto.UserCommand.UpdateUserCommand;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.CreateUserOAuth;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.LoginUserOAuthCommand;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.SignUpChildUserOAuthCommand;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.SignUpOwnerUserOAuthCommand;
import com.backend.onharu.domain.user.dto.UserOAuthQuery.GetUserByUserOAuthQuery;
import com.backend.onharu.domain.user.dto.UserProfile.NextLevelInfo;
import com.backend.onharu.domain.user.dto.UserProfile.UserChildProfile;
import com.backend.onharu.domain.user.dto.UserProfile.UserOwnerProfile;
import com.backend.onharu.domain.user.dto.UserQuery.GetChildProfileQuery;
import com.backend.onharu.domain.user.dto.UserQuery.GetOwnerProfileQuery;
import com.backend.onharu.domain.user.dto.UserQuery.GetUserByIdQuery;
import com.backend.onharu.domain.user.dto.UserQuery.GetUserByLoginIdQuery;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.domain.user.model.UserOAuth;
import com.backend.onharu.domain.user.service.UserCommandService;
import com.backend.onharu.domain.user.service.UserOAuthCommandService;
import com.backend.onharu.domain.user.service.UserOAuthQueryService;
import com.backend.onharu.domain.user.service.UserQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자 Facade
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserCommandService userCommandService;
    private final OwnerCommandService ownerCommandService;
    private final OwnerQueryService ownerQueryService;
    private final ChildCommandService childCommandService;
    private final ChildQueryService childQueryService;
    private final LevelQueryService levelQueryService;
    private final UserQueryService userQueryService;
    private final UserOAuthQueryService userOAuthQueryService;
    private final UserOAuthCommandService userOAuthCommandService;
    private final NotificationFacade notificationFacade;
    private final PasswordEncoder passwordEncoder;
    private final LevelCommandService levelCommandService;
    private final StoreQueryService storeQueryService;
    private final FileFacade fileFacade;

    /**
     * 로그인 아이디로 사용자를 조회합니다.
     *
     * @param query 로그인 아이디가 포함된 Query
     * @return 조호된 사용자 엔티티
     */
    public User getUser(GetUserByLoginIdQuery query) {
        return userQueryService.getUserByLoginId(
                new GetUserByLoginIdQuery(query.loginId())
        );
    }

    /**
     * 아동 회원가입을 처리합니다.
     * <p>
     * User와 Child를 함께 생성합니다.
     *
     * @param command 아동 회원가입 Command
     * @return 생성된 사용자 엔티티
     */
    @Transactional
    public User signUpChild(SignUpChildCommand command) {
        // 사용자 회원가입 처리
        User user = userCommandService.signUpChild(command);

        // 아동 회원가입 Command 생성
        CreateChildCommand createChildCommand = new CreateChildCommand(
                user,
                command.nickname()
        );

        // 아동 회원가입 처리
        Child child = childCommandService.createChild(createChildCommand);

        // 메타데이터 등록(파일 저장)
        fileFacade.registerFiles(AttachmentType.CHILD, child.getId(), command.images());

        return user;
    }

    /**
     * 사업자 회원가입을 처리합니다.
     * <p>
     * User와 Owner를 함께 생성합니다.
     *
     * @param command 사업자 회원가입 Command
     * @return 생성된 사용자 엔티티
     */
    @Transactional
    public User signUpOwner(SignUpOwnerCommand command) {
        // 기본 등급(비기너)을 이름으로 조회
        Level level = levelQueryService.getLevelByName(
                new GetLevelByNameQuery(
                        "비기너"
                )
        );

        // 사용자 회원가입 처리
        User user = userCommandService.signUpOwner(command);

        // 사업자 회원가입 Command 생성
        CreateOwnerCommand createOwnerCommand = new CreateOwnerCommand(
                user,
                level,
                command.businessNumber()
        );

        // 사업자 회원가입 처리
        ownerCommandService.createOwner(createOwnerCommand);

        return user;
    }

    /**
     * 사용자 타입별로 로그인 정보를 분리합니다.
     *
     * @param user 사용자 엔티티
     * @return 사용자, 아동/사업자 타입별 ID (다른 타입은 null)
     */
    public UserLogin divideUserType(User user) {
        return switch (user.getUserType()) {
            case CHILD -> {
                Child child = childQueryService.getChildByUserId(
                        new GetChildByUserIdQuery(user.getId())
                );
                yield new UserLogin(user, child.getId());
            }
            case OWNER -> {
                Owner owner = ownerQueryService.getOwnerByUserId(
                        new GetOwnerByUserIdQuery(user.getId())
                );
                yield new UserLogin(user, owner.getId());
            }
            case ADMIN, NONE -> new UserLogin(user, null);
        };
    }

    /**
     * 사용자 로그인을 처리합니다.
     * <p>
     * User 를 생성하고 비밀번호와 계정 상태를 검증합니다.
     *
     * @param command 로그인 요청 Command
     * @return 로그인 사용자 엔티티
     */
    @Transactional
    public UserLogin loginUser(LoginUserCommand command) {
        // 로그인 아이디로 사용자 조회
        User user = userQueryService.getUserByLoginId(
                new GetUserByLoginIdQuery(command.loginId())
        );

        // 비밀번호 검증
        user.verifyPassword(command.password(), passwordEncoder);

        // 사용자 계정 상태 검증
        user.verifyStatus();

        // 사용자 타입별 조회
        UserLogin userLogin = divideUserType(user);

        // 알림 설정 생성
        notificationFacade.ensureNotificationExists(user.getId());

        return userLogin;
    }

    /**
     * 소셜 로그인을 처리합니다.
     * <p>
     * 계정이 없는 경우 소셜 회원가입을 진행하고, 계정이 있는 경우 소셜 계정과 연동한 뒤, 사용자 계정 상태를 검증합니다.
     *
     * @param command 소셜 로그인 요청
     * @return 사용자 엔티티
     */
    @Transactional
    public User loginUserOAuth(LoginUserOAuthCommand command) {
        // 소셜 로그인 사용자 조회(UserOAuth 테이블에 없는 경우, 임시 사용자 생성)
        User user = userOAuthQueryService.getUserByUserOAuthQuery(
                        new GetUserByUserOAuthQuery(
                                command.providerId()
                        )
                )
                .map(UserOAuth::getUser) // 소셜 사용자 계정이 존재하는 경우, 해당 사용자 반환
                .orElseGet(() -> {
                    // 소셜 사용자 계정이 없는 경우, 임시 회원 생성
                    User tempUser = userCommandService.createUser(
                            new CreateUserCommand(
                                    command.loginId(),
                                    passwordEncoder.encode(UUID.randomUUID().toString()),
                                    command.name(),
                                    command.phoneNumber(),
                                    UserType.NONE,
                                    StatusType.PENDING,
                                    command.providerType()
                            ));
                    // 소셜 사용자(임시 회원) 회원가입 처리
                    userOAuthCommandService.createUserOAuth(
                            new CreateUserOAuth(
                                    tempUser,
                                    command.providerType(),
                                    command.providerId()
                            )
                    );

                    return tempUser; // 임시 회원 반환
                });

        user.verifyStatus(); // 기존 사용자 계정 상태 검증

        // 알림 설정 생성
        notificationFacade.ensureNotificationExists(user.getId());

        return user; // 기존 회원 반환
    }

    /**
     * 소셜 사용자 아동 회원가입을 처리합니다.
     * <p>
     * 추가 정보를 받아 소셜 사용자 아동의 회원가입을 마무리 합니다.
     *
     * @param command 아동 회원가입에 필요한 추가 정보
     * @return 사용자 와 childId 가 포함된 DTO
     */
    @Transactional
    public UserLogin completeSignUpChildUserOAuth(SignUpChildUserOAuthCommand command) {
        // 사용자 조회
        User user = userQueryService.getUser(
                new GetUserByIdQuery(
                        Long.valueOf(command.userId())
                )
        );
        // 사용자 타입을 아동으로 변경
        user.changeUserTypeToChild();
        // 아동 회원가입 Command
        CreateChildCommand createChildCommand = new CreateChildCommand(
                user,
                command.nickname()
        );

        // 아동 회원가입 처리
        Child child = childCommandService.createChild(createChildCommand);

        // 메타데이터 등록(파일 저장)
        fileFacade.registerFiles(AttachmentType.CHILD, child.getId(), command.images());

        return new UserLogin(user, child.getId());
    }

    /**
     * 소셜 사용자 사업자 회원가입을 처리합니다.
     * <p>
     * 추가 정보를 받아 소셜 사용자 사업자의 회원가입을 마무리 합니다.
     *
     * @param command 사업자 회원가입에 필요한 추가 정보
     * @return 사용자 엔티티
     */
    @Transactional
    public UserLogin completeSignUpOwnerUserOAuth(SignUpOwnerUserOAuthCommand command) {
        // 기본 등급(비기너)을 이름으로 조회
        Level level = levelQueryService.getLevelByName(
                new GetLevelByNameQuery(
                        "비기너"
                )
        );

        // 추가 정보를 받을 사용자 조회
        User user = userQueryService.getUser(
                new GetUserByIdQuery(
                        Long.valueOf(command.userId())
                )
        );

        // 사용자 타입을 사업자로 변경
        user.changeUserTypeToOwner();

        // 사업자 생성 Command
        CreateOwnerCommand createOwnerCommand = new CreateOwnerCommand(
                user,
                level,
                command.businessNumber()
        );

        // 사업자 생성
        Owner owner = ownerCommandService.createOwner(createOwnerCommand);

        return new UserLogin(user, owner.getId());
    }

    /**
     * 현재 사용자가 로그인 되었는지 확인합니다.
     *
     * @param query 사용자 ID 가 포함된 query
     * @return 조회된 사용자 정보
     */
    public User getMe(GetUserByIdQuery query) {

        User user = userQueryService.getUser(query);// 사용자 정보 조회

        user.verifyStatus(); // 사용자 계정 상태 검증(ACTIVE 또는 PENDING 가 아닐경우 예외 발생)

        return user;
    }

    /**
     * 로그인 ID 부분 일치 검색 (채팅 상대 선택용). 본인은 결과에서 제외됩니다.
     */
    @Transactional(readOnly = true)
    public List<User> searchUsersByLoginIdForChat(String keyword, Long excludeUserId) {
        return userQueryService.searchUsersByLoginIdLike(keyword, excludeUserId);
    }

    /**
     * 사용자(아동) 프로필 조회
     *
     * @param query 사용자 ID 와 아동 ID 가 포함된 query
     * @return 조회된 사용자 및 아동 엔티티
     */
    @Transactional
    public UserChildProfile getChildProfile(GetChildProfileQuery query) {
        // 사용자 조회
        User user = userQueryService.getUser(
                new GetUserByIdQuery(query.userId())
        );

        user.verifyStatus(); // 계정 상태 확인

        // 아동 조회
        Child child = childQueryService.getChildById(
                new GetChildByIdQuery(query.childId())
        );

        return new UserChildProfile(user, child);
    }

    /**
     * 사용자(사업자) 프로필 조회
     *
     * @param query 사용자 ID, 등급 ID, 사업자 ID 가 포함된 query
     * @return 조회된 사용자, 등급, 사업자 엔티티
     */
    @Transactional
    public UserOwnerProfile getOwnerProfile(GetOwnerProfileQuery query) {
        // 사용자 조회
        User user = userQueryService.getUser(
                new GetUserByIdQuery(query.userId())
        );

        user.verifyStatus(); // 계정 상태 확인

        // 사업자 조회
        Owner owner = ownerQueryService.getOwnerById(
                new GetOwnerByIdQuery(query.ownerId())
        );

        Level currentLevel = owner.getLevel(); // 사업자에 연결된 등급 도메인 추출
        int currentLevelConditionNumber = currentLevel.getConditionNumber();// 현재 등급의 나눔 횟수 추출
        int distributionCount = owner.getDistributionCount(); // 사업자의 나눔 횟수

        // 다음 등급 조회
        NextLevelInfo nextLevelInfo = levelQueryService.findFirstByConditionNumber(
                new FindFirstByConditionNumberQuery(currentLevelConditionNumber)
        ).map(level ->
                new NextLevelInfo(level, Math.max(level.getConditionNumber() - distributionCount, 0))
        ).orElse(new NextLevelInfo(currentLevel, 0)); // 다음 등급 없는 경우, 현재 등급과 남은 횟수를 0 반환

        // 가게 정보 조회
        List<Store> stores = storeQueryService.findByOwnerId(new StoreQuery.FindByOwnerIdQuery(query.ownerId()));

        return new UserOwnerProfile(user, currentLevel, owner, stores, nextLevelInfo);
    }

    /**
     * 사용자(아동) 프로필 수정 (User 와 Child 는 Dirty Checking 업데이트)
     *
     * @param command 사용자 ID, 아동 ID, 이름, 전화번호, 닉네임이 포함된 Command
     */
    @Transactional
    public void updateChildProfile(UpdateChildProfileCommand command) {
        // 사용자 조회
        User user = userQueryService.getUser(
                new GetUserByIdQuery(command.userId())
        );

        // 업데이트할 사용자 및 이름, 전화번호 검증
        user.verifyUpdate(command.name(), command.phone());

        // 사용자 변경사항 DB 반영
        userCommandService.updateUser(new UpdateUserCommand(user));

        // 아동 조회
        Child child = childQueryService.getChildById(
                new GetChildByIdQuery(command.childId())
        );

        // 아동 닉네임 검증 및 변경
        child.verifyAndUpdate(command.nickname());

        // 아동 변경사항 DB 반영
        childCommandService.updateChild(new UpdateChildCommand(child));
    }

    /**
     * 사용자(사업자) 프로필 수정 (User 와 Owner 는 Dirty Checking 업데이트)
     *
     * @param command 사용자 ID, 사업자 ID, 이름, 전화번호, 사업자 번호가 포함된 Command
     */
    @Transactional
    public void updateOwnerProfile(UpdateOwnerProfileCommand command) {
        // 사용자 조회
        User user = userQueryService.getUser(
                new GetUserByIdQuery(command.userId())
        );

        // 업데이트할 사용자 및 이름, 전화번호 검증
        user.verifyUpdate(command.name(), command.phone());

        // 사용자 변경사항 DB 반영
        userCommandService.updateUser(new UpdateUserCommand(user));

        // 사업자 조회
        Owner owner = ownerQueryService.getOwnerById(
                new GetOwnerByIdQuery(command.ownerId())
        );

        // 사업자 번호 검증 및 변경
        owner.verifyAndUpdate(command.businessNumber());

        // 사업자 변경사항 DB 반영
        ownerCommandService.updateOwner(new UpdateOwnerCommand(owner));
    }

    /**
     * 사용자 계정을 비활성화 시킵니다.
     *
     * @param command 사용자 제거 Command (사용자 ID, 사용자 계정 상태가 포함된 Command)
     */
    public void updateDeletedUser(UpdateDeletedUserCommand command) {
        // 사용자 조회
        User user = userQueryService.getUser(
                new GetUserByIdQuery(command.userId())
        );

        user.verifyStatus(); // 계정 상태 확인

        user.changeStatus(StatusType.DELETED); // 계정 상태 변경(상태만 삭제됨 으로 변경하고 도메인 제거 x)

        userCommandService.updateDeletedUser(command); // 변경된 사용자 갱신
    }
}

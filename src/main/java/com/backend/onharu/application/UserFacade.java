package com.backend.onharu.application;

import com.backend.onharu.domain.child.dto.ChildCommand.CreateChildCommand;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.service.ChildCommandService;
import com.backend.onharu.domain.child.service.ChildQueryService;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.dto.LevelQuery.GetLevelByIdQuery;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.level.service.LevelCommandService;
import com.backend.onharu.domain.level.service.LevelQueryService;
import com.backend.onharu.domain.owner.dto.OwnerCommand.CreateOwnerCommand;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.service.OwnerCommandService;
import com.backend.onharu.domain.owner.service.OwnerQueryService;
import com.backend.onharu.domain.user.dto.UserCommand.*;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.CreateUserOAuth;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.LoginUserOAuthCommand;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.SignUpChildUserOAuthCommand;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.SignUpOwnerUserOAuthCommand;
import com.backend.onharu.domain.user.dto.UserOAuthQuery.GetUserByUserOAuthQuery;
import com.backend.onharu.domain.user.dto.UserProfile.UserChildProfile;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.backend.onharu.domain.child.dto.ChildCommand.UpdateChildCommand;
import static com.backend.onharu.domain.child.dto.ChildQuery.GetChildByIdQuery;
import static com.backend.onharu.domain.level.dto.LevelCommand.UpdateNameByIdCommand;
import static com.backend.onharu.domain.owner.dto.OwnerCommand.updateOwnerBusinessNumberByIdCommand;
import static com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByIdQuery;
import static com.backend.onharu.domain.user.dto.UserProfile.UserOwnerProfile;
import static com.backend.onharu.domain.user.dto.UserQuery.GetChildProfileQuery;
import static com.backend.onharu.domain.user.dto.UserQuery.GetOwnerProfileQuery;

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

    private final PasswordEncoder passwordEncoder;
    private final LevelCommandService levelCommandService;

    /**
     * 아동 회원가입을 처리합니다.
     * <p>
     * User와 Child를 함께 생성합니다.
     *
     * @param command 아동 회원가입 Command
     * @return 생성된 사용자 엔티티
     */
    public User signUpChild(SignUpChildCommand command) {

        User user = userCommandService.signUpChild(command);

        CreateChildCommand createChildCommand = new CreateChildCommand(
                user,
                command.nickname(),
                command.certificateFilePath()
        );
        childCommandService.createChild(createChildCommand);

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
    public User signUpOwner(SignUpOwnerCommand command) {

        Level level = levelQueryService.getLevel(
                new GetLevelByIdQuery(
                        Long.valueOf(command.levelId())
                )
        );

        User user = userCommandService.signUpOwner(command);

        CreateOwnerCommand createOwnerCommand = new CreateOwnerCommand(
                user,
                level,
                command.businessNumber()
        );

        ownerCommandService.createOwner(createOwnerCommand);

        return user;
    }

    /**
     * 사용자 로그인을 처리합니다.
     * <p>
     * User 를 생성하고 비밀번호와 계정 상태를 검증합니다.
     *
     * @param command 로그인 요청 Command
     * @return 로그인 사용자 엔티티
     */
    public User loginUser(LoginUserCommand command) {

        User user = userQueryService.getUserByLoginId(
                new GetUserByLoginIdQuery(command.loginId())
        );

        user.verifyPassword(command.password(), passwordEncoder);
        user.verifyStatus();

        return user;
    }

    /**
     * 소셜 로그인을 처리합니다.
     * <p>
     * 계정이 없는 경우 소셜 회원가입을 진행하고, 계정이 있는 경우 소셜 계정과 연동한 뒤, 사용자 계정 상태를 검증합니다.
     *
     * @param command 소셜 로그인 요청
     * @return 사용자 엔티티
     */
    public User loginUserOAuth(LoginUserOAuthCommand command) {

        User user = userOAuthQueryService.getUserByUserOAuthQuery(
                        new GetUserByUserOAuthQuery(
                                command.providerId()
                        )
                )
                .map(UserOAuth::getUser)
                .orElseGet(() -> {
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

                    userOAuthCommandService.createUserOAuth(
                            new CreateUserOAuth(
                                    tempUser,
                                    command.providerType(),
                                    command.providerId()
                            )
                    );

                    return tempUser;
                });

        user.verifyStatus();

        return user;
    }

    /**
     * 소셜 사용자 아동 회원가입을 처리합니다.
     * <p>
     * 추가 정보를 받아 소셜 사용자 아동의 회원가입을 마무리 합니다.
     *
     * @param command 아동 회원가입에 필요한 추가 정보
     * @return 사용자 엔티티
     */
    public User completeSignUpChildUserOAuth(SignUpChildUserOAuthCommand command) {

        User user = userQueryService.getUser(
                new GetUserByIdQuery(
                        Long.valueOf(command.userId())
                )
        );

        user.changeUserTypeToChild();

        CreateChildCommand createChildCommand = new CreateChildCommand(
                user,
                command.nickname(),
                command.certificate()
        );

        childCommandService.createChild(createChildCommand);

        return user;
    }

    /**
     * 소셜 사용자 사업자 회원가입을 처리합니다.
     * <p>
     * 추가 정보를 받아 소셜 사용자 사업자의 회원가입을 마무리 합니다.
     *
     * @param command 사업자 회원가입에 필요한 추가 정보
     * @return 사용자 엔티티
     */
    public User completeSignUpOwnerUserOAuth(SignUpOwnerUserOAuthCommand command) {

        Level level = levelQueryService.getLevel(
                new GetLevelByIdQuery(
                        Long.valueOf(command.levelId())
                )
        );

        User user = userQueryService.getUser(
                new GetUserByIdQuery(
                        Long.valueOf(command.userId())
                )
        );

        user.changeUserTypeToOwner();

        CreateOwnerCommand createOwnerCommand = new CreateOwnerCommand(
                user,
                level,
                command.businessNumber()
        );

        ownerCommandService.createOwner(createOwnerCommand);

        return user;
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
     * 사용자(아동) 프로필 조회
     *
     * @param query 사용자 ID 와 아동 ID 가 포함된 query
     * @return 조회된 사용자 및 아동 엔티티
     */
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

        Level level = owner.getLevel(); // 사업자에 연결된 등급 도메인 추출

        return new UserOwnerProfile(user, level, owner);
    }

    /**
     * 사용자(아동) 프로필 수정
     *
     * @param command 사용자 ID, 아동 ID, 이름, 전화번호, 닉네임이 포함된 command
     */
    public void updateChildProfile(UpdateChildProfileCommand command) {
        // 사용자 조회
        User user = userQueryService.getUser(
                new GetUserByIdQuery(command.userId())
        );
        user.verifyStatus(); // 계정 상태 확인

        // 사용자 정보 업데이트
        userCommandService.updateUserByIdAndNameAndPhone(
                new UpdateUserCommand(
                        command.userId(),
                        user.getName(),
                        user.getPhone()
                )
        );

        // 아동 정보 업데이트
        childCommandService.updateChildByNickname(
                new UpdateChildCommand(
                        command.childId(),
                        command.nickname()
                )
        );
    }

    // 사용자(사업자) 프로필 수정
    public void updateOwnerProfile(UpdateOwnerProfileCommand command) {
        // 사용자 조회
        User user = userQueryService.getUser(
                new GetUserByIdQuery(command.userId())
        );
        user.verifyStatus(); // 계정 상태 확인

        // 사용자 정보 업데이트
        userCommandService.updateUserByIdAndNameAndPhone(
                new UpdateUserCommand(
                        command.userId(),
                        user.getName(),
                        user.getPhone()
                )
        );

        // 등급 정보 업데이트
        levelCommandService.updateNameById(
                new UpdateNameByIdCommand(
                        command.name(),
                        command.levelId()
                )
        );

        // 사용자(사업자) 정보 업데이트
        ownerCommandService.updateOwnerBusinessNumberById(
                new updateOwnerBusinessNumberByIdCommand(
                        command.ownerId(),
                        command.businessNumber()
                )
        );
    }

    /**
     * 사용자 계정을 비활성화 시킵니다.
     *
     * @param command 사용자 제거 Command (사용자 ID, 사용자 계정 상태가 포함된 Command)
     */
    public void updateDeletedUser(UpdateDeletedUser command) {
        // 사용자 조회
        User user = userQueryService.getUser(
                new GetUserByIdQuery(command.userId())
        );

        user.verifyStatus(); // 계정 상태 확인

        user.changeStatus(StatusType.DELETED); // 계정 상태 변경(상태만 삭제됨 으로 변경하고 도메인 제거 x)

        userCommandService.updateDeletedUser(command); // 변경된 사용자 갱신
    }
}

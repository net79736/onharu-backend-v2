package com.backend.onharu.application;

import com.backend.onharu.domain.child.dto.ChildCommand.CreateChildCommand;
import com.backend.onharu.domain.child.service.ChildCommandService;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.dto.LevelQuery.GetLevelByIdQuery;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.level.service.LevelQueryService;
import com.backend.onharu.domain.owner.dto.OwnerCommand.CreateOwnerCommand;
import com.backend.onharu.domain.owner.service.OwnerCommandService;
import com.backend.onharu.domain.user.dto.UserCommand.CreateUserCommand;
import com.backend.onharu.domain.user.dto.UserCommand.LoginUserCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpChildCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpOwnerCommand;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.CreateUserOAuth;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.LoginUserOAuthCommand;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.SignUpChildUserOAuthCommand;
import com.backend.onharu.domain.user.dto.UserOAuthCommand.SignUpOwnerUserOAuthCommand;
import com.backend.onharu.domain.user.dto.UserOAuthQuery.GetUserByUserOAuthQuery;
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

/**
 * 사용자 Facade
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserCommandService userCommandService;
    private final OwnerCommandService ownerCommandService;
    private final ChildCommandService childCommandService;
    private final LevelQueryService levelQueryService;
    private final UserQueryService userQueryService;
    private final UserOAuthQueryService userOAuthQueryService;
    private final UserOAuthCommandService userOAuthCommandService;

    private final PasswordEncoder passwordEncoder;

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
}

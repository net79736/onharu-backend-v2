package com.backend.onharu.application;

import com.backend.onharu.domain.child.dto.ChildCommand.CreateChildCommand;
import com.backend.onharu.domain.child.service.ChildCommandService;
import com.backend.onharu.domain.level.dto.LevelQuery.GetLevelByIdQuery;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.level.service.LevelQueryService;
import com.backend.onharu.domain.owner.dto.OwnerCommand.CreateOwnerCommand;
import com.backend.onharu.domain.owner.service.OwnerCommandService;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpChildCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpOwnerCommand;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.domain.user.service.UserCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 사용자 Facade
 */
@Component
@RequiredArgsConstructor
public class UserFacade {

    private final UserCommandService userCommandService;
    private final OwnerCommandService ownerCommandService;
    private final ChildCommandService childCommandService;
    private final LevelQueryService levelQueryService;

    /**
     * 아동 회원가입을 처리합니다.
     * <p>
     * User와 Child를 함께 생성합니다.
     *
     * @param command 아동 회원가입 Command
     * @return 생성된 사용자 엔티티
     */
    public User signUpChild(SignUpChildCommand command) {
        // 1. User 생성
        User user = userCommandService.signUpChild(command);

        // 2. Child 생성 (User와 연결)
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
        // 1. User 생성
        User user = userCommandService.signUpOwner(command);

        // 2. Level 조회
        Level level = levelQueryService.getLevel(
                new GetLevelByIdQuery(
                        Long.valueOf(command.levelId())
                )
        );

        // 3. Owner 생성 (User, Level 과 연결)
        CreateOwnerCommand createOwnerCommand = new CreateOwnerCommand(
                user,
                level,
                command.businessNumber()
        );
        ownerCommandService.createOwner(createOwnerCommand);

        return user;
    }
}

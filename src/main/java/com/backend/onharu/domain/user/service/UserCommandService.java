package com.backend.onharu.domain.user.service;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.dto.UserCommand.CreateUserCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpChildCommand;
import com.backend.onharu.domain.user.dto.UserCommand.SignUpOwnerCommand;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.GetUserByLoginIdParam;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.backend.onharu.domain.support.error.ErrorType.User.USER_ID_ALREADY_EXISTS;
import static com.backend.onharu.domain.user.dto.UserCommand.UpdatePasswordCommand;
import static com.backend.onharu.domain.user.dto.UserCommand.UpdateUserCommand;
import static com.backend.onharu.domain.user.dto.UserRepositoryParam.UpdateUserByIdAndNameAndPhoneParam;
import static com.backend.onharu.domain.user.dto.UserRepositoryParam.UpdateUserByIdAndPasswordParam;

/**
 * 사용자 Command Service
 * <p>
 * 사용자 도메인의 상태를 변경하는 비즈니스 로직을 처리하는 서비스입니다.
 * Command 패턴을 사용하여 도메인 모델의 변경 작업을 캡슐화합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 아동 회원가입을 처리합니다.
     *
     * @param command 아동 회원가입 Command
     * @return 생성된 사용자 엔티티
     */
    public User signUpChild(SignUpChildCommand command) {

        if (userRepository.existsByLoginId(new GetUserByLoginIdParam(command.loginId()))) {
            throw new CoreException(USER_ID_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(command.password());

        CreateUserCommand createCommand = new CreateUserCommand(
                command.loginId(),
                encodedPassword,
                command.name(),
                command.phone(),
                UserType.CHILD,
                StatusType.PENDING,
                ProviderType.LOCAL
        );

        return createUser(createCommand);
    }

    /**
     * 사업자 회원가입을 처리합니다.
     *
     * @param command 사업자 회원가입 Command
     * @return 생성된 사용자 엔티티
     */
    public User signUpOwner(SignUpOwnerCommand command) {

        if (userRepository.existsByLoginId(new GetUserByLoginIdParam(command.loginId()))) {
            throw new CoreException(USER_ID_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(command.password());

        CreateUserCommand createCommand = new CreateUserCommand(
                command.loginId(),
                encodedPassword,
                command.name(), // 사업자는 매장명을 name에 저장
                command.phone(),
                UserType.OWNER,
                StatusType.PENDING, // 사업자도 관리자 승인 후 활성화
                ProviderType.LOCAL
        );

        return createUser(createCommand);
    }

    /**
     * 사용자를 생성합니다.
     *
     * @param command 사용자 생성 Command
     * @return 생성된 사용자 엔티티
     */
    public User createUser(CreateUserCommand command) {
        User user = User.builder()
                .loginId(command.loginId())
                .password(command.password())
                .name(command.name())
                .phone(command.phone())
                .userType(command.userType())
                .statusType(command.statusType())
                .providerType(command.providerType())
                .build();

        return userRepository.save(user);
    }

    /**
     * 사용자의 비밀번호를 임시 비밀번호로 초기화 합니다.
     *
     * @param command 비밀번호 초기화 Command
     */
    public void updateUserByIdAndPassword(UpdatePasswordCommand command) {
        userRepository.updateUserByIdAndPassword(
                new UpdateUserByIdAndPasswordParam(
                        command.id(),
                        command.password()
                )
        );
    }

    /**
     * 사용자 정보를 수정합니다.
     *
     * @param command 사용자 수정 Command (사용자 ID, 이름, 전화번호)
     */
    public void updateUserByIdAndNameAndPhone(UpdateUserCommand command) {
        userRepository.updateUserByIdAndNameAndPhone(
                new UpdateUserByIdAndNameAndPhoneParam(
                        command.userId(),
                        command.name(),
                        command.phone()
                )
        );
    }
}

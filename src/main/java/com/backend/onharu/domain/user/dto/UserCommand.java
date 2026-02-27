package com.backend.onharu.domain.user.dto;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;

import java.util.List;

import static com.backend.onharu.domain.file.dto.FileCommand.ImageMetadata;
import static com.backend.onharu.domain.support.error.ErrorType.Child.CHILD_ID_MUST_NOT_BE_NULL;
import static com.backend.onharu.domain.support.error.ErrorType.Child.NICKNAME_MUST_NOT_BE_BLANK;
import static com.backend.onharu.domain.support.error.ErrorType.Owner.BUSINESS_NUMBER_MUST_NOT_BE_BLANK;
import static com.backend.onharu.domain.support.error.ErrorType.Owner.OWNER_ID_MUST_NOT_BE_NULL;
import static com.backend.onharu.domain.support.error.ErrorType.User.*;

/**
 * 사용자 관련 Command DTO
 * <p>
 * 사용자 도메인에서 사용되는 Command 패턴의 DTO 를 정의합니다.
 * Command 는 도메인 모델의 상태를 변경하는 작업을 나타냅니다.
 */
public class UserCommand {

    /**
     * 아동 회원가입 Command
     * <p>
     * 아동 사용자 회원가입에 필요한 정보를 담는 Command 입니다.
     */
    public record SignUpChildCommand(
            String loginId,
            String password,
            String passwordConfirm,
            String name,
            String phone,
            String nickname,
            List<ImageMetadata> images
    ) {
        public SignUpChildCommand {
            if (loginId == null || loginId.isBlank()) {
                throw new CoreException(LOGIN_ID_MUST_NOT_BE_BLANK);
            }
            if (password == null || password.isBlank()) {
                throw new CoreException(PASSWORD_MUST_NOT_BE_BLANK);
            }
            if (passwordConfirm == null || passwordConfirm.isBlank()) {
                throw new CoreException(PASSWORD_CONFIRM_MUST_NOT_BE_BLANK);
            }
            if (!password.equals(passwordConfirm)) {
                throw new CoreException(PASSWORD_CONFIRM_MISMATCH);
            }
            if (name == null || name.isBlank()) {
                throw new CoreException(USER_NAME_MUST_NOT_BE_BLANK);
            }
            if (phone == null || phone.isBlank()) {
                throw new CoreException(PHONE_MUST_NOT_BE_BLANK);
            }
            if (nickname == null || nickname.isBlank()) {
                throw new CoreException(NICKNAME_MUST_NOT_BE_BLANK);
            }
        }
    }

    /**
     * 사업자 회원가입 Command
     * <p>
     * 사업자 사용자 회원가입에 필요한 정보를 담는 Command 입니다.
     */
    public record SignUpOwnerCommand(
            String loginId,
            String password,
            String passwordConfirm,
            String name,
            String phone,
            String businessNumber
    ) {
        public SignUpOwnerCommand {
            if (loginId == null || loginId.isBlank()) {
                throw new CoreException(LOGIN_ID_MUST_NOT_BE_BLANK);
            }
            if (password == null || password.isBlank()) {
                throw new CoreException(PASSWORD_MUST_NOT_BE_BLANK);
            }
            if (passwordConfirm == null || passwordConfirm.isBlank()) {
                throw new CoreException(PASSWORD_CONFIRM_MUST_NOT_BE_BLANK);
            }
            if (!password.equals(passwordConfirm)) {
                throw new CoreException(PASSWORD_CONFIRM_MISMATCH);
            }
            if (name == null || name.isBlank()) {
                throw new CoreException(USER_NAME_MUST_NOT_BE_BLANK);
            }
            if (phone == null || phone.isBlank()) {
                throw new CoreException(PHONE_MUST_NOT_BE_BLANK);
            }
            if (businessNumber == null || businessNumber.isBlank()) {
                throw new CoreException(BUSINESS_NUMBER_MUST_NOT_BE_BLANK);
            }
        }
    }

    /**
     * 사용자 생성 Command
     * <p>
     * 사용자 엔티티를 생성하기 위한 Command 입니다.
     */
    public record CreateUserCommand(
            String loginId,
            String password,
            String name,
            String phone,
            UserType userType,
            StatusType statusType,
            ProviderType providerType
    ) {
        public CreateUserCommand {
            if (loginId == null || loginId.isBlank()) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (password == null || password.isBlank()) {
                throw new CoreException(PASSWORD_MUST_NOT_BE_BLANK);
            }
            if (name == null || name.isBlank()) {
                throw new CoreException(USER_NAME_MUST_NOT_BE_BLANK);
            }
            if (phone == null || phone.isBlank()) {
                throw new CoreException(PHONE_MUST_NOT_BE_BLANK);
            }
            if (userType == null) {
                throw new CoreException(USER_TYPE_MUST_NOT_BE_NULL);
            }
            if (statusType == null) {
                statusType = StatusType.PENDING; // 기본값은 대기상태
            }
            if (providerType == null) {
                providerType = ProviderType.LOCAL; // 기본값은 로컬
            }
        }
    }

    /**
     * 로그인 요청 Command
     * <p>
     * 로그인 요청 Command 입니다.
     */
    public record LoginUserCommand(
            String loginId,
            String password
    ) {
        public LoginUserCommand {
            if (loginId == null || loginId.isBlank()) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (password == null || password.isBlank()) {
                throw new CoreException(PASSWORD_MUST_NOT_BE_BLANK);
            }
        }
    }

    /**
     * 비밀번호 찾기(초기화) Command
     */
    public record ResetPasswordUserCommand(
            String loginId,
            String name,
            String phone
    ) {
        public ResetPasswordUserCommand {
            if (loginId == null || loginId.isBlank()) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (name == null || name.isBlank()) {
                throw new CoreException(USER_NAME_MUST_NOT_BE_BLANK);
            }
            if (phone == null || phone.isBlank()) {
                throw new CoreException(PHONE_MUST_NOT_BE_BLANK);
            }
        }
    }

    /**
     * 임시 비밀번호 업데이트 Command
     */
    public record UpdatePasswordCommand(
            Long id,
            String password
    ) {
        public UpdatePasswordCommand {
            if (id == null) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (password == null || password.isBlank()) {
                throw new CoreException(PASSWORD_MUST_NOT_BE_BLANK);
            }
        }
    }

    /**
     * 사용자(아동) 프로필 수정 Command
     */
    public record UpdateChildProfileCommand(
            Long userId,
            Long childId,
            String name,
            String phone,
            String nickname
    ) {
        public UpdateChildProfileCommand {
            if (userId == null) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (childId == null) {
                throw new CoreException(CHILD_ID_MUST_NOT_BE_NULL);
            }
            if (name == null || name.isBlank()) {
                throw new CoreException(USER_NAME_MUST_NOT_BE_BLANK);
            }
            if (phone == null || phone.isBlank()) {
                throw new CoreException(PHONE_MUST_NOT_BE_BLANK);
            }
            if (nickname == null || nickname.isBlank()) {
                throw new CoreException(NICKNAME_MUST_NOT_BE_BLANK);
            }
        }
    }

    /**
     * 사용자(사업자) 프로필 수정 Command
     */
    public record UpdateOwnerProfileCommand(
            Long userId,
            Long ownerId,
            String name,
            String phone,
            String businessNumber
    ) {
        public UpdateOwnerProfileCommand {
            if (userId == null) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (ownerId == null) {
                throw new CoreException(OWNER_ID_MUST_NOT_BE_NULL);
            }
            if (name == null || name.isBlank()) {
                throw new CoreException(USER_NAME_MUST_NOT_BE_BLANK);
            }
            if (phone == null || phone.isBlank()) {
                throw new CoreException(PHONE_MUST_NOT_BE_BLANK);
            }
            if (businessNumber == null || businessNumber.isBlank()) {
                throw new CoreException(BUSINESS_NUMBER_MUST_NOT_BE_BLANK);
            }
        }
    }

    /**
     * 사용자 제거 Command
     */
    public record UpdateDeletedUserCommand(
            Long userId,
            StatusType statusType
    ) {
        public UpdateDeletedUserCommand {
            if (userId == null) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (statusType == null) {
                statusType = StatusType.PENDING;
            }
        }
    }

    /**
     * 비밀번호 변경 Command
     */
    public record ChangePasswordCommand(
            Long userId,
            String currentPassword,
            String newPassword,
            String newPasswordConfirm
    ) {
        public ChangePasswordCommand {
            if (userId == null) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (currentPassword == null || currentPassword.isBlank()) {
                throw new CoreException(PASSWORD_MUST_NOT_BE_BLANK);
            }
            if (!newPassword.equals(newPasswordConfirm)) {
                throw new CoreException(PASSWORD_CONFIRM_MISMATCH);
            }
        }
    }

    /**
     * 사용자 변경 Command
     *
     * @param user 사용자 도메인
     */
    public record UpdateUserCommand(
            User user
    ) {
        public UpdateUserCommand {
            if (user == null) {
                throw new CoreException(USER_MUST_NOT_BE_NULL);
            }
        }
    }
}

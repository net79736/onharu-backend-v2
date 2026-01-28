package com.backend.onharu.domain.user.dto;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.support.error.CoreException;

import static com.backend.onharu.domain.support.error.ErrorType.Child.CERTIFICATE_MUST_NOT_BE_BLANK;
import static com.backend.onharu.domain.support.error.ErrorType.Child.NICKNAME_MUST_NOT_BE_BLANK;
import static com.backend.onharu.domain.support.error.ErrorType.Owner.*;
import static com.backend.onharu.domain.support.error.ErrorType.User.*;

/**
 * 사용자 관련 Command DTO
 * <p>
 * 사용자 도메인에서 사용되는 Command 패턴의 DTO를 정의합니다.
 * Command는 도메인 모델의 상태를 변경하는 작업을 나타냅니다.
 */
public class UserCommand {

    /**
     * 아동 회원가입 Command
     * <p>
     * 아동 사용자 회원가입에 필요한 정보를 담는 Command입니다.
     */
    public record SignUpChildCommand(
            String loginId,
            String password,
            String passwordConfirm,
            String name,
            String phone,
            String nickname,
            String certificateFilePath
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
                throw new CoreException(NAME_MUST_NOT_BE_BLANK);
            }
            if (phone == null || phone.isBlank()) {
                throw new CoreException(PHONE_MUST_NOT_BE_BLANK);
            }
            if (nickname == null || nickname.isBlank()) {
                throw new CoreException(NICKNAME_MUST_NOT_BE_BLANK);
            }
            if (certificateFilePath == null || certificateFilePath.isBlank()) {
                throw new CoreException(CERTIFICATE_MUST_NOT_BE_BLANK);
            }
        }
    }

    /**
     * 사업자 회원가입 Command
     * <p>
     * 사업자 사용자 회원가입에 필요한 정보를 담는 Command입니다.
     */
    public record SignUpOwnerCommand(
            String loginId,
            String password,
            String passwordConfirm,
            String name,
            String phone,
            String storeName,
            String businessNumber,
            String levelId
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
                throw new CoreException(NAME_MUST_NOT_BE_BLANK);
            }
            if (phone == null || phone.isBlank()) {
                throw new CoreException(PHONE_MUST_NOT_BE_BLANK);
            }
            if (storeName == null || storeName.isBlank()) {
                throw new CoreException(STORE_NAME_MUST_NOT_BE_BLANK);
            }
            if (businessNumber == null || businessNumber.isBlank()) {
                throw new CoreException(BUSINESS_NUMBER_MUST_NOT_BE_BLANK);
            }
            if (levelId == null || levelId.isBlank()) {
                throw new CoreException(LEVEL_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 사용자 생성 Command
     * <p>
     * 사용자 엔티티를 생성하기 위한 Command입니다.
     */
    public record CreateUserCommand(
            String loginId,
            String password,
            String name,
            String phone,
            UserType userType,
            ProviderType providerType,
            StatusType statusType
    ) {
        public CreateUserCommand {
            if (loginId == null || loginId.isBlank()) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (password == null || password.isBlank()) {
                throw new CoreException(PASSWORD_MUST_NOT_BE_BLANK);
            }
            if (name == null || name.isBlank()) {
                throw new CoreException(NAME_MUST_NOT_BE_BLANK);
            }
            if (phone == null || phone.isBlank()) {
                throw new CoreException(PHONE_MUST_NOT_BE_BLANK);
            }
            if (userType == null) {
                throw new CoreException(USER_TYPE_MUST_NOT_BE_NULL);
            }
            if (providerType == null) {
                throw new CoreException(PROVIDER_TYPE_MUST_NOT_BE_NULL);
            }
            if (statusType == null) {
                statusType = StatusType.PENDING;
            }
        }
    }
}

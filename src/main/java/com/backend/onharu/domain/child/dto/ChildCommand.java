package com.backend.onharu.domain.child.dto;

import static com.backend.onharu.domain.support.error.ErrorType.Child.CERTIFICATE_MUST_NOT_BE_BLANK;
import static com.backend.onharu.domain.support.error.ErrorType.Child.NICKNAME_MUST_NOT_BE_BLANK;
import static com.backend.onharu.domain.support.error.ErrorType.User.USER_ID_MUST_NOT_BE_NULL;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;

/**
 * 아동 관련 Command DTO
 */
public class ChildCommand {

    /**
     * 아동 생성 Command
     */
    public record CreateChildCommand(
            User user,
            String nickname,
            String certificate
    ) {
        public CreateChildCommand {
            if (user == null || user.getId() == null) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (nickname == null || nickname.isBlank()) {
                throw new CoreException(NICKNAME_MUST_NOT_BE_BLANK);
            }
            if (certificate == null || certificate.isBlank()) {
                throw new CoreException(CERTIFICATE_MUST_NOT_BE_BLANK);
            }
        }
    }
}

package com.backend.onharu.domain.child.dto;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;

import static com.backend.onharu.domain.support.error.ErrorType.Child.CHILD_MUST_NOT_BE_NULL;
import static com.backend.onharu.domain.support.error.ErrorType.Child.NICKNAME_MUST_NOT_BE_BLANK;
import static com.backend.onharu.domain.support.error.ErrorType.User.USER_ID_MUST_NOT_BE_NULL;

/**
 * 아동 관련 Command DTO
 */
public class ChildCommand {

    /**
     * 아동 생성 Command
     *
     * @param user     사용자 엔티티
     * @param nickname 닉네임
     */
    public record CreateChildCommand(
            User user,
            String nickname
    ) {
        public CreateChildCommand {
            if (user == null) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (nickname == null || nickname.isBlank()) {
                throw new CoreException(NICKNAME_MUST_NOT_BE_BLANK);
            }
        }
    }

    /**
     * 아동 도메인 수정 Command
     *
     * @param child 아동
     */
    public record UpdateChildCommand(
            Child child
    ) {
        public UpdateChildCommand {
            if (child == null) {
                throw new CoreException(CHILD_MUST_NOT_BE_NULL);
            }
        }
    }
}

package com.backend.onharu.domain.owner.dto;

import static com.backend.onharu.domain.support.error.ErrorType.Owner.BUSINESS_NUMBER_MUST_NOT_BE_BLANK;
import static com.backend.onharu.domain.support.error.ErrorType.Owner.LEVEL_ID_MUST_NOT_BE_NULL;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.user.model.User;

/**
 * 사업자 관련 Command DTO
 */
public class OwnerCommand {

    /**
     * 사업자 생성 Command
     */
    public record CreateOwnerCommand(
            User user,
            Long levelId,
            String businessNumber
    ) {
        public CreateOwnerCommand {
            if (user == null || user.getId() == null) {
                throw new CoreException(ErrorType.User.USER_ID_MUST_NOT_BE_NULL);
            }
            if (levelId == null) {
                throw new CoreException(LEVEL_ID_MUST_NOT_BE_NULL);
            }
            if (businessNumber == null || businessNumber.isBlank()) {
                throw new CoreException(BUSINESS_NUMBER_MUST_NOT_BE_BLANK);
            }
        }
    }
}

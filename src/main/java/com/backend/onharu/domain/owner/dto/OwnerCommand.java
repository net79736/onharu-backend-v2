package com.backend.onharu.domain.owner.dto;

import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;

import static com.backend.onharu.domain.support.error.ErrorType.Owner.BUSINESS_NUMBER_MUST_NOT_BE_BLANK;
import static com.backend.onharu.domain.support.error.ErrorType.Owner.LEVEL_ID_MUST_NOT_BE_NULL;
import static com.backend.onharu.domain.support.error.ErrorType.User.USER_ID_MUST_NOT_BE_NULL;

/**
 * 사업자 도메인의 CommandService 에 사용될 DTO
 */
public class OwnerCommand {

    /**
     * 사업자 생성 Command
     *
     * @param user 사용자
     * @param level 등급
     * @param businessNumber 사업자 등록번호
     */
    public record CreateOwnerCommand(
            User user,
            Level level,
            String businessNumber
    ) {
        public CreateOwnerCommand {
            if (user == null) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (level == null) {
                throw new CoreException(LEVEL_ID_MUST_NOT_BE_NULL);
            }
            if (businessNumber == null || businessNumber.isBlank()) {
                throw new CoreException(BUSINESS_NUMBER_MUST_NOT_BE_BLANK);
            }
        }
    }

    /**
     * 사업자 등록번호 확인 Command
     *
     * @param businessNumber 사업자 등록번호
     */
    public record checkBusinessNumberCommand(
            String businessNumber
    ) {
    }
}

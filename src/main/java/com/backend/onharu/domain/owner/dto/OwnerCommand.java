package com.backend.onharu.domain.owner.dto;

import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.user.model.User;

import static com.backend.onharu.domain.support.error.ErrorType.Owner.BUSINESS_NUMBER_MUST_NOT_BE_BLANK;
import static com.backend.onharu.domain.support.error.ErrorType.Owner.LEVEL_ID_MUST_NOT_BE_NULL;

/**
 * 사업자 관련 Command DTO
 */
public class OwnerCommand {

    /**
     * 사업자 생성 Command
     */
    public record CreateOwnerCommand(
            User user,
            Level level,
            String businessNumber
    ) {
        public CreateOwnerCommand {
            if (user == null || user.getId() == null) {
                throw new CoreException(ErrorType.User.USER_ID_MUST_NOT_BE_NULL);
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
     * 사업자 수정 Command
     *
     * @param ownerId        사업자 ID
     * @param businessNumber 사업자 등록번호
     */
    public record updateOwnerBusinessNumberByIdCommand(
            Long ownerId,
            String businessNumber
    ) {
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

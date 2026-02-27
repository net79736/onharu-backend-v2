package com.backend.onharu.domain.owner.dto;

import com.backend.onharu.domain.support.error.CoreException;

import static com.backend.onharu.domain.support.error.ErrorType.Owner.OWNER_ID_MUST_NOT_BE_NULL;
import static com.backend.onharu.domain.support.error.ErrorType.Owner.OWNER_USER_ID_MUST_NOT_BE_NULL;

/**
 * 사업자 도메인의 QueryService 사용될 DTO
 */
public class OwnerQuery {

    /**
     * 사업자 ID로 사업자 조회 Query
     *
     * @param id 사업자 ID
     */
    public record GetOwnerByIdQuery(
            Long id
    ) {
        public GetOwnerByIdQuery {
            if (id == null) {
                throw new CoreException(OWNER_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 사용자 ID로 사업자 조회 Query
     *
     * @param userId 사용자 ID
     */
    public record GetOwnerByUserIdQuery(
            Long userId
    ) {
        public GetOwnerByUserIdQuery {
            if (userId == null) {
                throw new CoreException(OWNER_USER_ID_MUST_NOT_BE_NULL);
            }
        }
    }
}

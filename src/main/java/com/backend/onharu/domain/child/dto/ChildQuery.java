package com.backend.onharu.domain.child.dto;

import com.backend.onharu.domain.support.error.CoreException;

import static com.backend.onharu.domain.support.error.ErrorType.Child.*;

/**
 * 아동 도메인의 QueryService 에 사용되는 DTO
 */
public class ChildQuery {

    /**
     * 아동 ID로 아동 엔티티 조회 Query
     *
     * @param childId 아동 ID
     */
    public record GetChildByIdQuery(
            Long childId
    ) {
        public GetChildByIdQuery {
            if (childId == null) {
                throw new CoreException(CHILD_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 사용자 ID로 아동 조회 Query
     *
     * @param userId 사용자 ID
     */
    public record GetChildByUserIdQuery(
            Long userId
    ) {
        public GetChildByUserIdQuery {
            if (userId == null) {
                throw new CoreException(CHILD_USER_ID_MUST_NOT_BE_NULL);
            }
        }
    }
}

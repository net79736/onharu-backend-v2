package com.backend.onharu.domain.child.dto;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;

/**
 * 아동 Repository 파라미터
 */
public class ChildRepositoryParam {

    /**
     * 아동 ID로 조회하는 파라미터
     *
     * @param childId 아동 ID
     */
    public record GetChildByIdParam(
            Long childId
    ) {
        public GetChildByIdParam {
            if (childId == null) {
                throw new CoreException(ErrorType.Child.CHILD_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 사용자 ID 로 아동을 조회하는 파라미터
     *
     * @param userId 사용자 ID
     */
    public record GetChildByUserIdParam(
            Long userId
    ) {
        public GetChildByUserIdParam {
            if (userId == null) {
                throw new CoreException(ErrorType.User.USER_ID_MUST_NOT_BE_NULL);
            }
        }
    }
}

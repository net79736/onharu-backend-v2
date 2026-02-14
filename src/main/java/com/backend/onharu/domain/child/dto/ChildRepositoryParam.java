package com.backend.onharu.domain.child.dto;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;

/**
 * 아동 Repository 파라미터
 */
public class ChildRepositoryParam {
    /**
     * 아동 ID로 조회하는 파라미터
     */
    public record GetChildByIdParam(
            Long id
    ) {
        public GetChildByIdParam {
            if (id == null) {
                throw new CoreException(ErrorType.Child.CHILD_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 로그인 ID로 조회하는 파라미터
     */
    public record GetChildByLoginIdParam(
            String loginId
    ) {
        public GetChildByLoginIdParam {
            if (loginId == null || loginId.isBlank()) {
                throw new CoreException(ErrorType.Child.LOGIN_ID_MUST_NOT_BE_NULL);
            }
        }
    }
}

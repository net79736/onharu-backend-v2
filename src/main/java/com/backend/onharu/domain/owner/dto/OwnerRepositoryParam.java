package com.backend.onharu.domain.owner.dto;

import static com.backend.onharu.domain.support.error.ErrorType.Owner.LOGIN_ID_MUST_NOT_BE_NULL;
import static com.backend.onharu.domain.support.error.ErrorType.Owner.OWNER_ID_MUST_NOT_BE_NULL;

import com.backend.onharu.domain.support.error.CoreException;

/**
 * 사업자 Repository 파라미터
 * 
 * Repository 인터페이스에서 사용되는 파라미터를 정의합니다.
 */
public class OwnerRepositoryParam {

    /**
     * 사업자 ID로 조회하는 파라미터
     */
    public record GetOwnerByIdParam(
        Long id
    ) {
        public GetOwnerByIdParam {
            if (id == null) {
                throw new CoreException(OWNER_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 로그인 ID로 조회하는 파라미터
     */
    public record GetOwnerByLoginIdParam(
            String loginId
    ) {
        public GetOwnerByLoginIdParam {
            if (loginId == null || loginId.isBlank()) {
                throw new CoreException(LOGIN_ID_MUST_NOT_BE_NULL);
            }
        }
    }
}

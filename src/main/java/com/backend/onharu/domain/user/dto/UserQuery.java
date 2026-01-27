package com.backend.onharu.domain.user.dto;

import static com.backend.onharu.domain.support.error.ErrorType.User.USER_ID_MUST_NOT_BE_NULL;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;

/**
 * 사용자 관련 Query DTO
 * 
 * 사용자 도메인에서 사용되는 Query 패턴의 DTO를 정의합니다.
 * Query는 도메인 모델의 상태를 조회하는 작업을 나타냅니다.
 */
public class UserQuery {

    /**
     * 사용자 ID로 조회하는 Query
     */
    public record GetUserByIdQuery(
            Long id
    ) {
        public GetUserByIdQuery {
            if (id == null) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 로그인 ID로 조회하는 Query
     */
    public record GetUserByLoginIdQuery(
            String loginId
    ) {
        public GetUserByLoginIdQuery {
            if (loginId == null || loginId.isBlank()) {
                throw new CoreException(ErrorType.User.LOGIN_ID_MUST_NOT_BE_BLANK);
            }
        }
    }
}

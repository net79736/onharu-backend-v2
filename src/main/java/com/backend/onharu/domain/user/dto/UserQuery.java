package com.backend.onharu.domain.user.dto;

import com.backend.onharu.domain.support.error.CoreException;

import static com.backend.onharu.domain.support.error.ErrorType.Child.CHILD_ID_MUST_NOT_BE_NULL;
import static com.backend.onharu.domain.support.error.ErrorType.Owner.OWNER_ID_MUST_NOT_BE_NULL;
import static com.backend.onharu.domain.support.error.ErrorType.User.*;

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
                throw new CoreException(LOGIN_ID_MUST_NOT_BE_BLANK);
            }
        }
    }

    /**
     * 이름과 전화번호로 조회하는 Query
     */
    public record GetUserByNameAndPhoneQuery(
            String name,
            String phone
    ) {
        public GetUserByNameAndPhoneQuery {
            if (name == null || name.isBlank()) {
                throw new CoreException(USER_NAME_MUST_NOT_BE_BLANK);
            }

            if (phone == null || phone.isBlank()) {
                throw new CoreException(PHONE_MUST_NOT_BE_BLANK);
            }
        }
    }

    /**
     * 사용자(아동) 프로필 조회 Query
     * @param userId 사용자 ID
     * @param childId 아동 ID
     */
    public record GetChildProfileQuery(
            Long userId,
            Long childId
    ) {
        public GetChildProfileQuery {
            if (userId == null) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (childId == null) {
                throw new CoreException(CHILD_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 사용자(가게) 프로필 조회 Query
     * @param userId 사용자 ID
     * @param ownerId 가게 ID
     */
    public record GetOwnerProfileQuery(
            Long userId,
            Long ownerId
    ) {
        public GetOwnerProfileQuery {
            if (userId == null) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (ownerId == null) {
                throw new CoreException(OWNER_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 비밀번호 조회 Query 입니다.
     * @param userId 사용자 ID,
     * @param password 비밀번호
     */
    public record ValidatePasswordQuery(
            Long userId,
            String password
    ) {
        public ValidatePasswordQuery {
            if (userId == null) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
            if (password == null || password.isBlank()) {
                throw new CoreException(PASSWORD_MUST_NOT_BE_BLANK);
            }
        }
    }
}

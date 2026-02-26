package com.backend.onharu.domain.owner.dto;

import com.backend.onharu.domain.support.error.CoreException;

import static com.backend.onharu.domain.support.error.ErrorType.Owner.USER_ID_MUST_NOT_BE_NULL;

/**
 * 사업자 관련 Query DTO
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
    }

    /**
     * 로그인 아이디로 사업자 조회 Query
     *
     * @param loginId 로그인 아이디
     */
    public record GetOwnerByLoginIdQuery(
            String loginId
    ) {
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
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
        }
    }
}

package com.backend.onharu.domain.owner.dto;

import com.backend.onharu.domain.support.error.CoreException;

import static com.backend.onharu.domain.support.error.ErrorType.Owner.*;

/**
 * 사업자 Repository 파라미터
 * <p>
 * Repository 인터페이스에서 사용되는 파라미터를 정의합니다.
 */
public class OwnerRepositoryParam {

    /**
     * 사업자 ID로 조회하는 파라미터
     *
     * @param id 사업자 ID
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
     *
     * @param loginId 로그인 아이디
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

    /**
     * 사용자 ID 로 사업자를 조회하는 파라미터
     *
     * @param userId 사용자 ID
     */
    public record GetOwnerByUserIdParam(
            Long userId
    ) {
        public GetOwnerByUserIdParam {
            if (userId == null) {
                throw new CoreException(USER_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 사업자 정보 수정 파라미터
     *
     * @param ownerId        사업자 ID
     * @param businessNumber 사업자 등록번호
     */
    public record UpdateOwnerBusinessNumberByIdParam(
            Long ownerId,
            String businessNumber
    ) {
    }
}

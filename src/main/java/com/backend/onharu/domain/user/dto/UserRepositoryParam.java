package com.backend.onharu.domain.user.dto;

import com.backend.onharu.domain.common.enums.StatusType;

/**
 * 사용자 Repository 파라미터
 * <p>
 * Repository 인터페이스에서 사용되는 파라미터를 정의합니다.
 * Repository 구현체에서 JPA 쿼리 메서드에 전달되는 파라미터입니다.
 */
public class UserRepositoryParam {

    /**
     * 사용자 ID로 조회하는 파라미터
     */
    public record GetUserByIdParam(
            Long userId
    ) {
    }

    /**
     * 로그인 ID로 조회하는 파라미터
     */
    public record GetUserByLoginIdParam(
            String loginId
    ) {
    }

    /**
     * 이름, 전화번호로 조회하는 파라미터
     */
    public record GetUserByNameAndPhoneParam(
            String name,
            String phone
    ) {
    }

    /**
     * 사용자 ID 와 임시 비밀번호로 사용자 비밀번호를 초기화합니다.
     */
    public record UpdateUserByIdAndPasswordParam(
            Long id,
            String password
    ) {
    }

    /**
     * 제거된 사용자를 업데이트 합니다. (소프트 삭제)
     * @param userId 사용자 ID
     * @param statusType 계정 상태(DELETED)
     */
    public record UpdateDeletedUserParam(
            Long userId,
            StatusType statusType
    ) {
    }
}

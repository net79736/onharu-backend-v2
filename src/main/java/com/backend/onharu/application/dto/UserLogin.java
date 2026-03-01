package com.backend.onharu.application.dto;

import com.backend.onharu.domain.user.model.User;

/**
 * UserFacade 에서 사용자와 아동/사업자 ID 를 반환하기 위한 DTO
 *
 * @param user     사용자
 * @param domainId 사용자가 아동일 경우 childId, 사업자인 경우 ownerId 반환
 */
public record UserLogin(
        User user,
        Long domainId
) {
}

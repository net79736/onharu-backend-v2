package com.backend.onharu.domain.user.dto;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.user.model.User;

/**
 * 사용자 프로필 관련 DTO 입니다.
 */
public class UserProfile {

    /**
     * 사용자(아동) 프로필 조회 결과 DTO
     */
    public record UserChildProfile(
            User user,
            Child child
    ) {
    }

    /**
     * 사용자(가게) 프로필 조회 결과 DTO
     */
    public record UserOwnerProfile(
            User user,
            Level level,
            Owner owner
    ) {
    }
}

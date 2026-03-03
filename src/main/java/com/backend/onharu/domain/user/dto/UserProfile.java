package com.backend.onharu.domain.user.dto;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.user.model.User;

import java.util.List;

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
     * 사용자(사업자) 프로필 조회 결과 DTO
     */
    public record UserOwnerProfile(
            User user,
            Level level,
            Owner owner,
            List<Store> stores,
            NextLevelInfo nextLevelInfo
    ) {
    }

    /**
     * 사업자의 프로필에 보낼 다음 등급 DTO
     */
    public record NextLevelInfo(
            Level nextLevel,
            int nextToConditionNumber
    ) {
    }
}

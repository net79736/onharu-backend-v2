package com.backend.onharu.domain.store.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.backend.onharu.domain.store.model.Store;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Redis 캐시 전용 DTO.
 * - 엔티티/프록시를 담지 않고 순수 값만 담음
 * - 필요한 필드만 평탄화(flatten)해서 직렬화 문제 원천 차단
 *
 * <p>주의: 이 DTO는 "캐시"가 목적이므로, 엔티티를 절대 들고 있지 않아야 합니다.
 * (Hibernate 프록시/LAZY 컬렉션을 그대로 담으면 직렬화/역직렬화에서 문제가 납니다.)</p>
 */
@Getter
@ToString
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true) // Jackson 기본 생성자: final 필드는 기본값으로 강제 초기화
public final class StoreCacheDto implements Serializable {

    // Store 기본 필드
    private final Long id;
    private final String name;
    private final String address;
    private final String phone;
    private final String lat;
    private final String lng;
    private final String introduction;
    private final String intro;
    private final Boolean isOpen;
    private final Boolean isSharing;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    // Category (flatten)
    private final Long categoryId;
    private final String categoryName;

    // Owner (flatten) - 캐시에 필요한 것만
    private final Long ownerId;
    private final String ownerName; // owner.user.name

    // 집계
    private final Long favoriteCount;
    private final Double distance;

    // 컬렉션 (flatten)
    private final List<BusinessHoursDto> businessHours;
    private final List<TagDto> tags;

    /**
     * 영업시간 캐시 DTO (BusinessHours 엔티티를 평탄화).
     * - businessDay: {@link com.backend.onharu.domain.common.enums.WeekType} name()
     * - openTime/closeTime: LocalTime#toString() (예: "10:00")
     */
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true) // Jackson 기본 생성자: final 필드는 기본값으로 강제 초기화
    public static final class BusinessHoursDto implements Serializable {
        private final Long id;
        private final String businessDay;
        private final String openTime;
        private final String closeTime;
    }

    /**
     * 태그 캐시 DTO (Tag 엔티티를 평탄화).
     */
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED, force = true) // Jackson 기본 생성자: final 필드는 기본값으로 강제 초기화
    public static final class TagDto implements Serializable {
        private final Long id;
        private final String name;
    }

    /**
     * {@link StoreWithFavoriteCount} → {@link StoreCacheDto} 변환.
     *
     * <p>권장 호출 위치: 트랜잭션 범위 안.
     * (연관 컬렉션/연관 엔티티가 LAZY라면, 이 변환 단계에서 필요한 값만 뽑아서 캐시에 넣습니다.)</p>
     */
    public static StoreCacheDto from(StoreWithFavoriteCount swfc) {
        Store store = swfc.store();

        List<BusinessHoursDto> bhList = (store.getBusinessHours() == null ? Collections.<com.backend.onharu.domain.store.model.BusinessHours>emptyList() : store.getBusinessHours())
                .stream()
                .map(bh -> new BusinessHoursDto(
                        bh.getId(),
                        bh.getBusinessDay() != null ? bh.getBusinessDay().name() : null,
                        bh.getOpenTime() != null ? bh.getOpenTime().toString() : null,
                        bh.getCloseTime() != null ? bh.getCloseTime().toString() : null
                ))
                .toList();

        List<TagDto> tagList = (store.getStoreTags() == null ? Collections.<com.backend.onharu.domain.store.model.StoreTag>emptyList() : store.getStoreTags())
                .stream()
                .map(st -> new TagDto(
                        st.getTag() != null ? st.getTag().getId() : null,
                        st.getTag() != null ? st.getTag().getName() : null
                ))
                .toList();

        return new StoreCacheDto(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getPhone(),
                store.getLat(),
                store.getLng(),
                store.getIntroduction(),
                store.getIntro(),
                store.getIsOpen(),
                store.getIsSharing(),
                store.getCreatedAt(),
                store.getUpdatedAt(),

                // Category
                store.getCategory() != null ? store.getCategory().getId() : null,
                store.getCategory() != null ? store.getCategory().getName() : null,

                // Owner → User
                store.getOwner() != null ? store.getOwner().getId() : null,
                (store.getOwner() != null && store.getOwner().getUser() != null) ? store.getOwner().getUser().getName() : null,

                // 집계
                swfc.favoriteCount(),
                swfc.distance(),

                // 컬렉션은 불변으로 저장(캐시 DTO 불변성 유지)
                List.copyOf(bhList),
                List.copyOf(tagList)
        );
    }
}
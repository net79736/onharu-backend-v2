package com.backend.onharu.domain.store.dto;

/**
 * 위치 기반 검색 Native Query 결과 매핑용 프로젝션.
 * SELECT s.id, favoriteCount, distance 컬럼과 매핑할 때 사용하는 인터페이스
 */
public interface StoreWithFavoriteCountByLocationProjection {

    Long getId();

    Long getFavoriteCount();

    Double getDistance();
}

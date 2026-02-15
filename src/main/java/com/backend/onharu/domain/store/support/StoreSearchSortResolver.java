package com.backend.onharu.domain.store.support;

/**
 * Store 검색 시 정렬 필드를 JPQL/Native Query 표현식으로 변환합니다.
 * 
 * 위치 정보(lat, lng) 유무에 따라 쿼리 타입이 달라지며,
 * - 위치 O: Native Query → (distance), (favoriteCount) 등 alias 사용
 * - 위치 X: JPQL → COUNT(f) 등 집계 함수 사용
 * 
 * 이 변환 로직은 Store 검색 도메인 규칙에 속하므로 domain 레이어에 위치
 */
public final class StoreSearchSortResolver {

    private StoreSearchSortResolver() {
    }

    /**
     * API 요청의 정렬 필드를 실제 쿼리에서 사용할 표현식으로 변환합니다.
     *
     * @param requestedSortField 요청된 정렬 필드 (distance, favoriteCount, id 등)
     * @param hasLocation        위치 정보 유무 (true: Native Query, false: JPQL)
     * @return 변환된 정렬 필드 표현식
     */
    public static String resolve(String requestedSortField, boolean hasLocation) {
        if (requestedSortField == null || requestedSortField.isEmpty()) {
            return "id";
        }

        // distance는 위치 정보가 있을 때만 유효
        if (requestedSortField.contains("distance")) {
            if (!hasLocation) {
                return "id";
            }
            return "(" + requestedSortField + ")";
        }

        // favoriteCount 정렬
        if (requestedSortField.contains("favoriteCount")) {
            if (hasLocation) {
                return "(favoriteCount)";
            }
            return "COUNT(f)";
        }

        return requestedSortField;
    }
}

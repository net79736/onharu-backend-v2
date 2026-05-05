package com.backend.onharu.infra.elasticsearch.store;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch `stores` 인덱스에 저장되는 문서 모델.
 *
 * <p>MySQL Store 엔티티의 모든 필드를 그대로 복제하지 않고, 검색/필터/정렬에 필요한 최소 필드만 담습니다.</p>
 */
public record StoreSearchDocument(
        long id,
        long ownerId,
        long categoryId,
        String name,
        String address,
        String phone,
        String introduction,
        String intro,
        boolean isOpen,
        boolean isSharing,
        LocalDateTime createdAt,
        String createdBy,
        GeoPoint location,
        List<String> tags
) {
    public record GeoPoint(double lat, double lon) {
    }
}


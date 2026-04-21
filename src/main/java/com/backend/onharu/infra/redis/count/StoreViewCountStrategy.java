package com.backend.onharu.infra.redis.count;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.model.StoreCount;
import com.backend.onharu.infra.db.store.StoreCountJpaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Store 조회수 동기화 전략.
 *
 * <p>Redis Hash field "view" 값을 DB의 store_counts.view_count에 반영합니다.</p>
 */
@Component
@RequiredArgsConstructor
public class StoreViewCountStrategy implements CountStrategy {

    static final String KEY_PREFIX = "onharu:count:store:";
    static final String PATTERN = KEY_PREFIX + "*";
    static final String FIELD_VIEW = "view"; // 조회수
    static final String FIELD_FAVORITE = "favorite"; // 좋아요 수

    private final StoreCountJpaRepository storeCountJpaRepository;
    private final EntityManager entityManager;

    @Override
    public DomainType getSupportedDomain() {
        return DomainType.STORE; // STORE 도메인 지원
    }

    @Override
    public String getRedisPattern() {
        return PATTERN; // "onharu:count:store:*"
    }

    @Override
    public String getRedisKey(long id) {
        return KEY_PREFIX + id; // "onharu:count:store:123"
    }

    @Override
    public Long extractIdFromKey(String key) {
        if (key == null || !key.startsWith(KEY_PREFIX)) return null;
        String raw = key.substring(KEY_PREFIX.length());
        try {
            return Long.parseLong(raw); // 123
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CommonCount loadFromDatabase(long id) {
        return storeCountJpaRepository.findById(id)
                .map(sc -> new CommonCount(
                        sc.getViewCount() != null ? sc.getViewCount() : 0L, // 조회수
                        sc.getFavoriteCount() != null ? sc.getFavoriteCount() : 0L // 좋아요 수
                ))
                .orElseGet(() -> new CommonCount(0L, 0L));
    }

    @Override
    @Transactional
    public void updateToDatabase(long id, CommonCount count) {
        // viewCount는 감소 방지(더 큰 값만), favoriteCount는 절대값 그대로 반영(+/- 토글 반영)
        int viewUpdated = storeCountJpaRepository.setViewCountIfGreater(id, count.viewCount()); // 조회수
        int favoriteUpdated = storeCountJpaRepository.setFavoriteCount(id, Math.max(0L, count.favoriteCount())); // 좋아요 수
        if (viewUpdated > 0 || favoriteUpdated > 0) return; // 조회수 또는 좋아요 수 업데이트 성공 시 종료

        // 토이 프로젝트 기준: StoreCount가 없으면 0으로 시작(혹은 생성 시도)만 하고 넘어갑니다.
        // (Redis에만 있다가 유실될 수 있어도 무방)
        try {
            Store storeRef = entityManager.getReference(Store.class, id); // 프록시 조회
            StoreCount sc = StoreCount.create(storeRef); // 생성
            storeCountJpaRepository.save(sc); // 저장
            storeCountJpaRepository.setViewCountIfGreater(id, count.viewCount()); // 조회수 Redis 값 반영
            storeCountJpaRepository.setFavoriteCount(id, Math.max(0L, count.favoriteCount())); // 좋아요 수 Redis 값 반영
        } catch (DataIntegrityViolationException | EntityNotFoundException ignored) {
            // 동시 생성/이미 존재, 또는 store 자체가 없는 경우: 무시
        }
    }
}
package com.backend.onharu.infra.redis;

/**
 * 가게 검색 최근 검색어 Redis 키 (coupon-issue-v3 패턴: List + Set 분리).
 * <ul>
 *   <li>List: 최근 순서(MRU, 왼쪽이 최신)</li>
 *   <li>Set: 중복 여부 빠른 판별 및 제거 시 동기화</li>
 * </ul>
 */
public final class StoreRecentSearchRedisKeyUtil {

    private static final String LIST_PREFIX = "onharu:store:recent:list:";
    private static final String SET_PREFIX = "onharu:store:recent:set:";

    private StoreRecentSearchRedisKeyUtil() {
    }

    /**
     * 최근 검색어 목록 키를 생성합니다.
     * @param ownerKey u:[:user_id]
     * @return onharu:store:recent:list:u:[:user_id]
     */
    public static String listKey(String ownerKey) {
        return LIST_PREFIX + ownerKey;
    }

    /**
     * 최근 검색어 중복 여부 빠른 판별 및 제거 시 동기화 키를 생성합니다.
     * @param ownerKey u:[:user_id]
     * @return onharu:store:recent:set:u:[:user_id]
     */
    public static String setKey(String ownerKey) {
        return SET_PREFIX + ownerKey;
    }
}

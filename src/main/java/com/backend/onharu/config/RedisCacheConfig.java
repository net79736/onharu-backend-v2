package com.backend.onharu.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

/**
 * Redis 캐시 설정 클래스
 *
 * <p>Spring의 캐시 추상화를 Redis로 구현합니다.
 * {@code @Cacheable}, {@code @CacheEvict}, {@code @CachePut} 등의 어노테이션을 사용할 수 있습니다.
 *
 * <p>캐시 전략(이 모듈, coupon-issue-v3 {@code RedissCacheConfiguration}과 동일 패턴):
 * <ul>
 *   <li><strong>storeDetail</strong> — 가게 상세(기본, 위치 미포함) 조회,
 *   {@link com.backend.onharu.domain.store.service.StoreQueryService#getStoreDetailByIdCached(Long)} 등</li>
 *   <li>TTL: 기본 30분 (아래 {@code entryTtl}과 동일)</li>
 * </ul>
 *
 * <p>참고: 다른 프로젝트에서는 {@code @Profile("!test")} 로 테스트에서 캐시 빈을 끄기도 합니다.
 * onharu는 테스트에서 Redis/캐시 구성이 필요할 수 있어, 기본적으로 프로필에 따라 분리하지 않습니다.
 * 필요 시 {@code @Profile} 로 조정하세요.
 */
@EnableCaching
@RequiredArgsConstructor
@Configuration
public class RedisCacheConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * Redis 캐시에 넣는 값(JSON)을 직렬화할 때 쓰는 ObjectMapper입니다.
     *
     * <ul>
     *   <li>Java 8 날짜/시간 API: {@link JavaTimeModule}</li>
     *   <li>날짜를 타임스탬프·숫자 배열이 아닌 ISO-8601 문자열로 직렬화: {@code WRITE_DATES_AS_TIMESTAMPS = false}</li>
     *   <li>엔티티 필드가 바뀐 예전 캐시 JSON과의 호환: {@code FAIL_ON_UNKNOWN_PROPERTIES = false}</li>
     *   <li>역직렬화 시 구체 타입 복원: default typing ({@code EVERYTHING}, {@code @JsonTypeInfo.As.PROPERTY}) —
     *   {@code record}/final 타입도 {@code @class} 포함 (Jackson에서 해당 enum은 deprecated 예정이나 Redis 캐시와 호환을 위해 유지)</li>
     *   <li>JPA 엔티티/프록시: {@link Hibernate6Module} — Redis에 {@code Store} 등 지연 연관이 포함된 값을 넣을 때
     *   {@code HibernateProxy}·{@code hibernateLazyInitializer} 직렬화 오류 방지</li>
     * </ul>
     */
    @SuppressWarnings("deprecation")
    private static ObjectMapper redisCacheValueObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Java 8 날짜/시간 API 지원
        objectMapper.registerModule(new JavaTimeModule());
        // JPA(Hibernate 6) 프록시·컬렉션 — @Cacheable 값에 엔티티 그래프가 있을 때 필수
        objectMapper.registerModule(new Hibernate6Module());
        // 날짜를 타임스탬프가 아닌 ISO 8601 문자열로 직렬화
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // 알 수 없는 속성 무시 (엔티티 변경 시 이전 캐시 데이터와 호환성 유지)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 다형성 타입 정보 포함 (역직렬화 시 정확한 타입 복원)
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );
        return objectMapper;
    }

    /**
     * Redis 기본 캐시 설정
     *
     * <ul>
     *   <li>TTL: 30분 (기존 동작 유지)</li>
     *   <li>Key: String 직렬화</li>
     *   <li>Value: JSON 직렬화 (위 ObjectMapper)</li>
     * </ul>
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 30분 (TTL)
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(redisCacheValueObjectMapper())
                        )
                );
    }

    /**
     * Redis를 캐시 저장소로 사용하는 CacheManager 빈을 생성합니다.
     * 이 CacheManager는 @Cacheable, @CacheEvict 등에서 사용됩니다.
     */
    @Primary // 기본 캐시 매니저로 설정
    @Bean
    public CacheManager redisCacheManager(RedisCacheConfiguration redisCacheConfiguration) {
        return RedisCacheManager.builder(redisConnectionFactory) // Redis 연결 팩토리 주입
                .cacheDefaults(redisCacheConfiguration)          // 기본 캐시 설정 적용
                .build();                                        // CacheManager 인스턴스 생성
    }
}

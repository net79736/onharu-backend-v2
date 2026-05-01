package com.backend.onharu.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.backend.onharu.infra.redis.lock.DistributeLockExecutor;

/**
 * coupon 의 {@code RedisCouponIssueServiceTest} / movie 의 {@code WaitingQueueRedisRepositoryTest} 처럼
 * 임베디드 Redis 위에서 Spring Data Redis + Redisson 동작을 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("임베디드 Redis 통합")
class EmbeddedRedisIntegrationTest {

    private static final String KEY_NS = "embedded-redis-test:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private DistributeLockExecutor distributeLockExecutor;

    @BeforeEach
    void cleanKeys() {
        Collection<String> keys = redisTemplate.keys(KEY_NS + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Nested
    @DisplayName("RedisTemplate")
    class RedisTemplateTests {

        @Test
        @DisplayName("문자열 set/get 이 동일 브로커에서 동작한다")
        void stringSetGet() {
            String key = KEY_NS + "kv";
            redisTemplate.opsForValue().set(key, "hello-embedded");
            assertThat(redisTemplate.opsForValue().get(key)).isEqualTo("hello-embedded");
        }

        @Test
        @DisplayName("Hash HSET/HGET 이 동작한다")
        void hashOps() {
            String key = KEY_NS + "hash";
            redisTemplate.opsForHash().put(key, "f1", "v1");
            assertThat(redisTemplate.opsForHash().get(key, "f1")).isEqualTo("v1");
        }
    }

    @Nested
    @DisplayName("Redisson")
    class RedissonTests {

        @Test
        @DisplayName("tryLock 후 현재 스레드가 보유하고 unlock 한다")
        void tryLockUnlock() throws Exception {
            RLock lock = redissonClient.getLock(KEY_NS + "lock-1");
            assertThat(lock.tryLock(2, 5, TimeUnit.SECONDS)).isTrue();
            try {
                assertThat(lock.isHeldByCurrentThread()).isTrue();
            } finally {
                lock.unlock();
            }
        }
    }

    @Nested
    @DisplayName("DistributeLockExecutor")
    class DistributeLockTests {

        @Test
        @DisplayName("동일 lock 으로 병렬 실행 시 Redis 카운터가 경쟁 없이 증가한다")
        void lockSerializesIncrement() throws Exception {
            String lockName = KEY_NS + "lock-counter";
            String counterKey = KEY_NS + "counter";
            int threads = 8;
            ExecutorService pool = Executors.newFixedThreadPool(threads);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);
            AtomicInteger errors = new AtomicInteger();

            for (int i = 0; i < threads; i++) {
                pool.submit(() -> {
                    try {
                        start.await();
                        distributeLockExecutor.execute(() -> {
                            String v = redisTemplate.opsForValue().get(counterKey);
                            int c = v == null ? 0 : Integer.parseInt(v);
                            redisTemplate.opsForValue().set(counterKey, String.valueOf(c + 1));
                        }, lockName, 10_000, 30_000);
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            assertThat(done.await(45, TimeUnit.SECONDS)).isTrue();
            pool.shutdown();
            assertThat(errors.get()).isZero();
            assertThat(redisTemplate.opsForValue().get(counterKey)).isEqualTo(String.valueOf(threads));
        }
    }
}

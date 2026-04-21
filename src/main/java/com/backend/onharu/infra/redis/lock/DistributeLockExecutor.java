package com.backend.onharu.infra.redis.lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DistributeLockExecutor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private final RedissonClient redissonClient;

    public void execute(Runnable runnable, String lockName, long waitMs, long leaseMs) {
        execute(() -> {
            runnable.run();
            return null;
        }, lockName, waitMs, leaseMs);
    }

    public <T> T execute(Supplier<T> supplier, String lockName, long waitMs, long leaseMs) {
        RLock rLock = redissonClient.getLock(lockName);
        logger.info("🐧 락 획득 시도 lockName={}", lockName);

        try {
            printLockStatus(rLock, lockName);
            boolean available = rLock.tryLock(waitMs, leaseMs, TimeUnit.MILLISECONDS);
            if (!available) {
                throw new IllegalStateException(
                        ("[" + lockName + "] lock 획득 실패🔐🔐 (waitMs: %d, leaseMs: %d)")
                                .formatted(waitMs, leaseMs));
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("[" + lockName + "] 락 획득 중 인터럽트가 발생했습니다.", e);
        } finally {
            if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

    public void printLockStatus(RLock rLock, String lockName) {
        if (rLock.isLocked()) {
            logger.info("🔒 이미 다른 프로세스가 락을 잡고 있음: {}", lockName);
        } else {
            logger.info("✅ 락이 현재 사용되지 않음: {}", lockName);
        }
    }
}


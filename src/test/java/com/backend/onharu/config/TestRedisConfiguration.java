package com.backend.onharu.config;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

/**
 * test 프로필 전용: 임베디드 Redis + RedissonClient를 제공합니다.
 * <p>
 * 기존 테스트 설정에서 Redis auto-config가 exclude 되어 있어도,
 * 분산락 테스트가 동작하도록 별도 빈으로 구성합니다.
 */
@Profile("test")
@Configuration
public class TestRedisConfiguration {

    public static final class EmbeddedRedisServerHandle {
        private final Object server;

        private EmbeddedRedisServerHandle(Object server) {
            this.server = server;
        }

        Object server() {
            return server;
        }

        public void stop() {
            try {
                server.getClass().getMethod("stop").invoke(server);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("임베디드 Redis 서버 종료에 실패했습니다.", e);
            }
        }
    }

    /**
     * embedded-redis 라이브러리를 "정적 타입"으로 참조하면,
     * IDE/러너 환경에 따라 테스트 classpath 구성 차이로 {@code NoClassDefFoundError}가 발생할 수 있습니다.
     * 이를 방지하기 위해 리플렉션으로 RedisServer를 생성합니다.
     */
    @Bean(destroyMethod = "stop")
    public EmbeddedRedisServerHandle embeddedRedisServer() throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }

        try {
            Class<?> redisServerClass = Class.forName("redis.embedded.RedisServer");
            Object builder = redisServerClass.getMethod("newRedisServer").invoke(null);
            builder.getClass().getMethod("port", int.class).invoke(builder, port);
            builder.getClass().getMethod("setting", String.class).invoke(builder, "maxmemory 128M");
            Object server = builder.getClass().getMethod("build").invoke(builder);
            server.getClass().getMethod("start").invoke(server);
            return new EmbeddedRedisServerHandle(server);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "embedded-redis 라이브러리가 test classpath에 없습니다. " +
                    "build.gradle에 `testImplementation 'com.github.codemonstur:embedded-redis:1.4.3'`가 필요합니다.", e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("임베디드 Redis 서버 초기화에 실패했습니다.", e);
        }
    }

    @Bean(destroyMethod = "shutdown")
    @DependsOn("embeddedRedisServer")
    public RedissonClient redissonClient(
            EmbeddedRedisServerHandle embeddedRedisServer,
            @Value("${spring.data.redis.host:localhost}") String host) {
        try {
            Object server = embeddedRedisServer.server();
            @SuppressWarnings("unchecked")
            List<Integer> ports = (List<Integer>) server.getClass().getMethod("ports").invoke(server);
            int port = ports.get(0);
            Config config = new Config();
            config.useSingleServer().setAddress("redis://" + host + ":" + port);
            return Redisson.create(config);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("임베디드 Redis 포트 조회에 실패했습니다.", e);
        }
    }
}
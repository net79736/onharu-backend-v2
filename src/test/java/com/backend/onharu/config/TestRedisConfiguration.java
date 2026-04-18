package com.backend.onharu.config;

import java.io.IOException;
import java.net.ServerSocket;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import redis.embedded.RedisServer;

/**
 * test 프로필 전용 임베디드 Redis. main 에 두지 않고 test 소스에만 둡니다.
 * <p>
 * 등록: {@code src/test/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * (테스트 실행 시에만 클래스패스에 올라와 자동 구성으로 로드됩니다.)
 * </p>
 */
@AutoConfiguration
@Profile("test")
public class TestRedisConfiguration {

    @Bean(destroyMethod = "stop")
    public RedisServer embeddedRedisServer() throws IOException {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        RedisServer server = RedisServer.newRedisServer()
                .port(port)
                .setting("maxmemory 128M")
                .build();
        server.start();
        return server;
    }

    @Bean
    @Primary
    @DependsOn("embeddedRedisServer")
    public RedisConnectionFactory redisConnectionFactory(RedisServer embeddedRedisServer) {
        int port = embeddedRedisServer.ports().get(0);
        RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration();
        cfg.setHostName("localhost");
        cfg.setPort(port);
        return new LettuceConnectionFactory(cfg);
    }

    @Bean(destroyMethod = "shutdown")
    @DependsOn("embeddedRedisServer")
    public RedissonClient redissonClient(
            RedisServer embeddedRedisServer,
            @Value("${spring.data.redis.host:localhost}") String host) {
        int port = embeddedRedisServer.ports().get(0);
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        return Redisson.create(config);
    }
}

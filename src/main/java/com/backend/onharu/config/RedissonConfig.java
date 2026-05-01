package com.backend.onharu.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("!test")
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    // Redis의 포트 정보를 주입받습니다.
    @Value("${spring.data.redis.port:6380}")
    private int port;

    @PostConstruct
    public void init() {
        log.info("RedissonConfiguration 이 실행되었습니다.");
        log.info("host: {}", host);
        log.info("port: {}", port);
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + this.host + ":" + this.port);
        return Redisson.create(config);
    }
}
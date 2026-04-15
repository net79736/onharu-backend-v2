package com.backend.onharu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisTemplate 설정.
 *
 * <p>Redis Hash(HSET/HGET) 기반 캐싱을 위해 key/value 및 hashKey/hashValue를 문자열로 직렬화합니다.</p>
 */
@Configuration
public class RedisTemplateConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer string = new StringRedisSerializer();
        template.setKeySerializer(string); // key를 문자열로 직렬화 (기본으로 필요함)
        template.setValueSerializer(string); // value를 문자열로 직렬화 (기본으로 필요함)
        template.setHashKeySerializer(string); // hashKey를 문자열로 직렬화
        template.setHashValueSerializer(string); // hashValue를 문자열로 직렬화

        template.afterPropertiesSet();
        return template;
    }
}
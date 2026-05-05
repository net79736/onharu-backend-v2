package com.backend.onharu;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ReactiveElasticsearchClientAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

import lombok.extern.slf4j.Slf4j;

/**
 * Kafka / RabbitMQ / Elasticsearch 는 수동 설정만 사용합니다.
 * 자동 구성을 제외시켜 브로커·ES 엔진 없이 기동할 수 있도록 위함.
 */
@Slf4j
@SpringBootApplication(exclude = {
        KafkaAutoConfiguration.class,
        RabbitAutoConfiguration.class,
        ElasticsearchClientAutoConfiguration.class, // Elasticsearch 8.13.4
        ReactiveElasticsearchClientAutoConfiguration.class, // Elasticsearch 8.13.4
        ElasticsearchDataAutoConfiguration.class, // Elasticsearch 8.13.4
        ElasticsearchRepositoriesAutoConfiguration.class, // Elasticsearch 8.13.4
})
public class OnharuApplication {

	public static void main(String[] args) {
        SpringApplication.run(OnharuApplication.class, args);

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("Time (Option Fixed - Seoul): {}", now);
        LocalDateTime now2 = LocalDateTime.now();
        log.info("Time (Default): {}", now2);
	}
}

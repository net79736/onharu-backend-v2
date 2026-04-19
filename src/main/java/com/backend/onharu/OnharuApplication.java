package com.backend.onharu;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

/**
 * Kafka / RabbitMQ 는 각각 {@code infra.kafka}, {@code infra.rabbitmq} 수동 설정만 사용합니다.
 * 자동 구성을 제외시켜 브로커 엔진 없이 기동할 수 있도록 위함.
 */
@SpringBootApplication(exclude = {KafkaAutoConfiguration.class, RabbitAutoConfiguration.class})
public class OnharuApplication {

	public static void main(String[] args) {
        SpringApplication.run(OnharuApplication.class, args);
        
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        System.out.println("Time (Option Fixed - Seoul): " + now);
        LocalDateTime now2 = LocalDateTime.now();
        System.out.println("Time (Default): " + now2);
	}
}

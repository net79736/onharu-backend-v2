package com.backend.onharu;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

/**
 * Kafka는 {@code infra.kafka} 수동 설정만 사용합니다(coupon/movie 패턴).
 * {@link KafkaAutoConfiguration} 제외로 onharu.kafka.enabled=false 일 때 브로커 없이 기동 가능합니다.
 */
@SpringBootApplication(exclude = {KafkaAutoConfiguration.class})
public class OnharuApplication {

	public static void main(String[] args) {
        SpringApplication.run(OnharuApplication.class, args);
        
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        System.out.println("Time (Option Fixed - Seoul): " + now);
        LocalDateTime now2 = LocalDateTime.now();
        System.out.println("Time (Default): " + now2);
	}
}

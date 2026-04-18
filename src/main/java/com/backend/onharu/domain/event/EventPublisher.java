package com.backend.onharu.domain.event;

/**
 * 도메인·인바운드 어댑터에서 외부로 이벤트를 내보낼 때 사용하는 포트.
 * 구현체는 Kafka({@code OnharuKafkaProducer}) 등 메시징 인프라에 둡니다(coupon {@code EventPublisher} 와 동일 역할).
 */
public interface EventPublisher {

    /**
     * {@code spring.kafka.template.default-topic} 으로 문자열 본문을 전송합니다.
     */
    void publish(String payload);
    
    /**
     * 지정한 토픽으로 문자열 본문을 전송합니다.
     */
    void publish(String topic, String payload);
}
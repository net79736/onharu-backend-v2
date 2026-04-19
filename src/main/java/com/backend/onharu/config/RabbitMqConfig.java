package com.backend.onharu.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 가 꺼져 있을 때 브로커 없이 기동할 수 있도록 {@link org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration} 은 제외하고,
 * {@code onharu.rabbitmq.enabled=true} 일 때만 빈을 등록합니다.
 */
@EnableRabbit
@ConditionalOnProperty(name = "onharu.rabbitmq.enabled", havingValue = "true")
@Configuration
public class RabbitMqConfig {

    /**
     * RabbitMQ 연결 팩토리 설정
     * 여러 프로젝트에서 동일한 RabbitMQ 서버를 사용할수 있기 때문에 가상 호스트로 방을 나눈다.
     * 
     * @param host RabbitMQ 호스트 (아파트 건물 주소)
     * @param port RabbitMQ 포트 (아파트 출입문)
     * @param username RabbitMQ 사용자명 (아파트 주인)
     * @param password RabbitMQ 비밀번호 (아파트 주인 열쇠)
     * @param virtualHost RabbitMQ 가상 호스트 (아파트 내의 몇 호(Room Number)에 들어갈 것인지)
     * @return
     */
    @Bean
    public ConnectionFactory rabbitConnectionFactory(
            @Value("${spring.rabbitmq.host:localhost}") String host,
            @Value("${spring.rabbitmq.port:5672}") int port,
            @Value("${spring.rabbitmq.username:guest}") String username,
            @Value("${spring.rabbitmq.password:guest}") String password,
            @Value("${spring.rabbitmq.virtual-host:/}") String virtualHost
    ) {
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        return factory;
    }

    /**
     * RabbitMQ 템플릿 설정
     * 
     * @param connectionFactory
     * @return
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        // 디스크 영속: 브로커 재시작·클러스터에서 메시지 손실 완화 (큐도 durable 이어야 함)
        template.setBeforePublishPostProcessors(message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT); // 메시지를 메모리가 아닌 디스크(Disk)에 저장하도록 브로커(RabbitMQ)에 지시
            message.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN); // 메시지 콘텐츠 유형을 텍스트로 설정
            return message;
        });
        return template;
    }

    /**
     * RabbitMQ 관리자 설정
     * 
     * @param connectionFactory
     * @return
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    /**
     * 채팅 이벤트 큐 설정
     * 
     * @param name 채팅 이벤트 큐 이름
     * @return
     */
    @Bean
    public Queue onharuChatEventsQueue(
            @Value("${onharu.rabbitmq.chat-events-queue:onharu.chat.events}") String name
    ) {
        // durable=true: RabbitMQ 서버가 꺼졌다가 다시 켜져도 이 큐를 삭제하지 않고 그대로 유지하겠다는 뜻
        return new Queue(name, true);
    }

    /**
     * RabbitMQ 리스너 컨테이너 팩토리 설정
     * * @RabbitListener가 메시지를 소비할 때 사용할 환경 설정을 정의합니다.
     * * [주요 설정: 수동 승인(MANUAL ACK)]
     * 1. 데이터 안전성: 기본값인 AUTO는 전달 즉시 메시지를 삭제하지만, MANUAL은 처리가 완료될 때까지 보관합니다.
     * 2. 장애 복구: 로직 수행 중 에러 발생 시 basicAck를 보내지 않으면, RabbitMQ가 메시지를 큐에 유지(Re-queue)하여 재처리를 보장합니다.
     * 3. 정교한 제어: 비즈니스 로직 성공 시에만 ack를 전송하고, 실패 시 nack/reject를 통해 재시도 여부를 결정할 수 있습니다.
     * * @param connectionFactory RabbitMQ 연결을 위한 팩토리
     * @return 설정이 완료된 리스너 컨테이너 팩토리
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        
        // 수동 ACK 모드 설정: 처리(DB 저장, 푸시 등) 완료 후 명시적으로 basicAck를 호출해야 함
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        
        return factory;
    }
}

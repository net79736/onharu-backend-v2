package com.backend.onharu.config;

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
        return new RabbitTemplate(connectionFactory);
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
        return new Queue(name, true);
    }

    /**
     * RabbitMQ 리스너 컨테이너 팩토리 설정
     * 
     * @param connectionFactory
     * @return
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }
}

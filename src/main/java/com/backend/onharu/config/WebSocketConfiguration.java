package com.backend.onharu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Spring STOMP 활성화
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    // 엔드포인트 등록
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat") // ws://서버주소//ws-chat
                .setAllowedOriginPatterns("*") // TODO: 운영 서버 배포시 * 대신 프론트엔드 도메인 적기
//                .withSockJS()
        ;
    }

    // prefix 설정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); // 내장 메시지 브로커 -> 단체 채팅 /topic, 일대일 채팅 /queue
        registry.setApplicationDestinationPrefixes("/app"); // 클라이언트 에서 서버로 메시지 보낼 때 붙이는 경로 -> 스프링이 @MessageMapping 가 붙은 컨트롤러로 전달
    }
}

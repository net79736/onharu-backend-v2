package com.backend.onharu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.backend.onharu.domain.support.ChatStompDestination;

@Configuration
@EnableWebSocketMessageBroker // Spring STOMP 활성화
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    /**
     * [단계 1] 클라이언트가 서버에 처음 연결될 '접속 지점' 설정
     * 채팅을 시작하려면 먼저 서버와 연결(Handshake)이 되어야 하는데, 그 대문을 만드는 곳입니다.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // 1. {@link ChatStompDestination#WS_ENDPOINT} 이름으로 대문을 만듭니다.
        // 클라이언트는 소켓 연결을 시도할 때 ws://서버주소 + WS_ENDPOINT 주소를 사용하게 됩니다.
        registry.addEndpoint(ChatStompDestination.WS_ENDPOINT)

                // 2. 보안 설정: 누가 이 대문에 들어올 수 있는지 정합니다.
                .setAllowedOriginPatterns("*") // TODO: 운영 시 프론트 도메인으로 제한

                // 3. SockJS — 구형 브라우저 대비 시 .withSockJS() 검토
                // .withSockJS()
                ;
    }

    /**
     * [단계 2] 메시지 브로커(우체국) 설정
     * 클라이언트가 보낸 메시지를 어디로 전달할지,
     * 그리고 클라이언트가 어떤 주소를 구독(대기)할지 정하는 곳입니다.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // 1. [도착 지점 설정] 서버가 클라이언트에게 메시지를 "전달"할 때 사용하는 경로입니다.
        // 클라이언트(프론트엔드)는 이 주소들을 '구독(Subscribe)'하고 기다립니다.
        // - /topic : 주로 1:N 전체 채팅이나 단체 채팅방용 (방송 개념)
        // - /queue : 주로 1:1 메시지나 특정 사용자 지정 전송용 (개인 우편함 개념)
        registry.enableSimpleBroker(ChatStompDestination.BROKER_PREFIX_TOPIC, ChatStompDestination.BROKER_PREFIX_QUEUE);

        // 2. [출발 지점 설정] 클라이언트가 서버로 메시지를 "보낼" 때 붙이는 규칙(Prefix)입니다.
        // 사용자가 채팅을 입력해서 전송할 때, 주소 앞에 "/app"을 붙여서 보내도록 약속하는 것입니다.
        // 예: 사용자가 /app/chat/message 로 메시지를 보내면,
        // 스프링은 "아! 이건 내가 처리할 메시지구나"라고 인식해서 @MessageMapping이 붙은 컨트롤러로 배달합니다.
        registry.setApplicationDestinationPrefixes(ChatStompDestination.APP_PREFIX);
    }
}

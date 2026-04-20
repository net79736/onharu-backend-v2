package com.backend.onharu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.backend.onharu.domain.support.ChatStompDestination;

@Configuration
@EnableWebSocketMessageBroker // Spring STOMP 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${onharu.stomp.relay.enabled:false}")
    private boolean stompRelayEnabled; // RabbitMQ STOMP 플러그인 사용 여부

    @Value("${onharu.stomp.relay.host:localhost}")
    private String stompRelayHost; // RabbitMQ STOMP 플러그인 호스트

    @Value("${onharu.stomp.relay.port:61613}")
    private int stompRelayPort; // RabbitMQ STOMP 플러그인 포트

    @Value("${onharu.stomp.relay.client-login:onharu}")
    private String stompClientLogin; // RabbitMQ STOMP 플러그인 클라이언트 로그인

    @Value("${onharu.stomp.relay.client-passcode:onharu}")
    private String stompClientPasscode; // RabbitMQ STOMP 플러그인 클라이언트 패스코드

    @Value("${onharu.stomp.relay.system-login:guest}")
    private String stompSystemLogin; // RabbitMQ STOMP 플러그인 시스템 로그인

    @Value("${onharu.stomp.relay.system-passcode:guest}")
    private String stompSystemPasscode; // RabbitMQ STOMP 플러그인 시스템 패스코드

    @Value("${onharu.stomp.relay.virtual-host:/}")
    private String stompVirtualHost; // RabbitMQ STOMP 플러그인 가상 호스트

    /**
     * [단계 1] 클라이언트가 서버에 처음 연결될 '접속 지점' 설정
     * 채팅을 시작하려면 먼저 서버와 연결(Handshake)이 되어야 하는데, 그 대문을 만드는 곳입니다.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // 1. {@link ChatStompDestination#WS_ENDPOINT} 이름으로 대문을 만듭니다.
        // 클라이언트가 소켓 연결을 시도할 때 → ws://서버주소 + WS_ENDPOINT 주소를 사용 (예: ws://localhost:8080/ws-chat)
        registry.addEndpoint(ChatStompDestination.WS_ENDPOINT)

                // 2. 보안 설정
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

        // 1. [메시지 배달 경로 설정] 
        // 서버가 사용자에게 메시지를 쏠 때, 어떤 "채널"을 사용할지 정의합니다.
        // 프론트엔드는 이 주소를 '구독'해서 실시간 데이터를 받습니다.

        // - /topic : 단체 채널. (예: 식당의 실시간 예약 현황 업데이트, 전체 공지)
        // - /queue : 개인 채널. (예: 특정 아이에게 전송되는 '예약 확정' 알림, 1:1 문의 답변)
        // registry.enableSimpleBroker(ChatStompDestination.BROKER_PREFIX_TOPIC, ChatStompDestination.BROKER_PREFIX_QUEUE);

        // 1. [도착 지점] 인메모리 SimpleBroker 또는 외부 브로커(RabbitMQ STOMP 등) 릴레이
        if (stompRelayEnabled) {
            var relay = registry
                    .enableStompBrokerRelay(
                            ChatStompDestination.BROKER_PREFIX_TOPIC,
                            ChatStompDestination.BROKER_PREFIX_QUEUE
                    ) // SimpleBroker는 해당하는 경로를 SUBSCRIBE하는 Client에게 메세지를 전달하는 간단한 작업을 수행
                    .setRelayHost(stompRelayHost) // RabbitMQ STOMP 플러그인 호스트
                    .setRelayPort(stompRelayPort) // RabbitMQ STOMP 플러그인 포트
                    .setClientLogin(stompClientLogin) // RabbitMQ STOMP 플러그인 클라이언트 로그인
                    .setClientPasscode(stompClientPasscode) // RabbitMQ STOMP 플러그인 클라이언트 패스코드
                    .setSystemLogin(stompSystemLogin) // RabbitMQ STOMP 플러그인 시스템 로그인
                    .setSystemPasscode(stompSystemPasscode); // RabbitMQ STOMP 플러그인 시스템 패스코드
            if (StringUtils.hasText(stompVirtualHost)) {
                relay.setVirtualHost(stompVirtualHost);
            }
        } else {
            registry.enableSimpleBroker(
                    ChatStompDestination.BROKER_PREFIX_TOPIC,
                    ChatStompDestination.BROKER_PREFIX_QUEUE
            );
        }

        // 2. [출발 지점 설정] 클라이언트가 서버로 메시지를 "보낼" 때 붙이는 규칙(Prefix)입니다.
        // 사용자가 채팅을 입력해서 전송할 때, 주소 앞에 "/app"을 붙여서 보내도록 약속하는 것입니다.
        // 예: 사용자가 /app/chat/message 로 메시지를 보내면,
        // 스프링은 "아! 이건 내가 처리할 메시지구나"라고 인식해서 @MessageMapping이 붙은 컨트롤러로 배달합니다.
        registry.setApplicationDestinationPrefixes(ChatStompDestination.APP_PREFIX);
    }
}

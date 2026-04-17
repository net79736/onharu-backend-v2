package com.backend.onharu.domain.support;

/**
 * STOMP/WebSocket 목적지(Destination) 경로를 한곳에서 관리합니다.
 * <p>
 * 브로커·앱 프리픽스·채팅방 토픽 규칙이 바뀌면 이 클래스만 수정하면 됩니다.
 * 향후 1:1(개인 큐) 등이 추가되면 {@link #BROKER_PREFIX_QUEUE} 기반 메서드를 확장합니다.
 */
public final class ChatStompDestination {

    private ChatStompDestination() {
    }

    /** SockJS/WebSocket 핸드셰이크 엔드포인트 (예: {@code ws://host/ws-chat}) */
    public static final String WS_ENDPOINT = "/ws-chat";

    /** Spring Security 등에서 사용하는 Ant 패턴 */
    public static final String WS_ENDPOINT_ANT_PATTERN = WS_ENDPOINT + "/**";

    /**
     * 단순 브로커가 구독 배달에 사용하는 prefix.
     * {@link com.backend.onharu.config.WebSocketConfiguration} 의 {@code enableSimpleBroker}와 동일해야 합니다.
     */
    public static final String BROKER_PREFIX_TOPIC = "/topic";
    public static final String BROKER_PREFIX_QUEUE = "/queue";

    /**
     * 애플리케이션 핸들러({@code @MessageMapping})로 보낼 때 클라이언트가 붙이는 prefix.
     */
    public static final String APP_PREFIX = "/app";

    /**
     * 채팅 전송 — {@code @MessageMapping} 에만 쓰는 상대 경로 (앞에 {@link #APP_PREFIX} 가 붙은 전체 경로로 전송).
     */
    public static final String MESSAGE_MAPPING_CHAT_SEND = "/chat/send";

    private static final String TOPIC_CHAT_BASE = BROKER_PREFIX_TOPIC + "/chat/";

    /**
     * 채팅방 단위 브로드캐스트 토픽 — 클라이언트는 이 경로를 subscribe 합니다.
     */
    public static String topicChatRoom(long chatRoomId) {
        return TOPIC_CHAT_BASE + chatRoomId; // "/topic/chat/{chatRoomId}"
    }

    /**
     * 클라이언트가 STOMP SEND 에 사용하는 전체 destination (예: {@code /app/chat/send}).
     */
    public static String appDestinationChatSend() {
        return APP_PREFIX + MESSAGE_MAPPING_CHAT_SEND; // "/app/chat/send"
    }

    /**
     * 향후 1:1 또는 사용자별 알림 등 — 개인 큐 예시 (브로커가 {@link #BROKER_PREFIX_QUEUE} 를 쓸 때).
     */
    public static String queueUserInbox(long userId) {
        return BROKER_PREFIX_QUEUE + "/users/" + userId + "/inbox"; // "/queue/users/{userId}/inbox"
    }
}

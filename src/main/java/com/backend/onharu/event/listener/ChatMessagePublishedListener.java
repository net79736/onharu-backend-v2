package com.backend.onharu.event.listener;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.backend.onharu.domain.chat.repository.ChatRoomRepository;
import com.backend.onharu.domain.event.ChatRabbitPublishPort;
import com.backend.onharu.event.model.ChatMessagePublishedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 메시지 저장 이후 단계(채팅방 마지막 메시지 ID 갱신, 외부 브로커 발행)를 메인 트랜잭션 커밋 후에 분리해 수행합니다.
 *
 * - chat_rooms.last_message_id 를 단일 원자 UPDATE 로 갱신 — chat_messages INSERT 의 FK S-lock 과 같은 트랜잭션에서 충돌하지 않도록 분리.
 * - RabbitMQ 발행 — 메인 트랜잭션 범위 밖이므로 브로커 지연이 DB 락 보유 시간에 영향을 주지 않고, 롤백 시 메시지가 외부에 누출되지 않음.
 *
 * {@code @Async("chatEventExecutor")} 로 별도 스레드 풀에서 실행하므로, 호출 지점(STOMP clientInboundChannel) 은
 * 이벤트 publish 직후 즉시 반환되며 메인 DB 커넥션도 커밋과 동시에 풀로 되돌아갑니다. 이를 통해 요청당 커넥션 수요가
 * {@code 메인 TX + 리스너 TX = 2} 에서 실질적으로 {@code 1} 로 떨어져 HikariCP 풀 포화가 발생하지 않습니다.
 *
 * AFTER_COMMIT + REQUIRES_NEW 로 묶여 있어 리스너 내부 예외는 메인 트랜잭션(이미 커밋됨) 을 되돌리지 않습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessagePublishedListener {

    private final ChatRoomRepository chatRoomRepository;

    /** RabbitMQ 활성화된 경우에만 주입됩니다. */
    private final ObjectProvider<ChatRabbitPublishPort> chatRabbitPublishPort;

    @Async("chatEventExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChatMessagePublished(ChatMessagePublishedEvent event) {
        advanceChatRoomLastMessage(event); // chat_rooms.last_message_id 를 최신 값으로 업데이트
        publishToRabbit(event); // RabbitMQ 채팅 이벤트 발행
    }

    /**
     * chat_rooms.last_message_id 를 최신 값으로 업데이트합니다.
     * [주의] 일반적인 JPA Dirty Checking을 쓰지 않는 이유:
     * 메세지 INSERT와 채팅방 UPDATE가 겹칠 때 발생하는 '데드락(Deadlock)'을 방지하기 위함입니다.
     * 
     * @param event
     */
    private void advanceChatRoomLastMessage(ChatMessagePublishedEvent event) {
        try {
            chatRoomRepository.bumpLastMessageId(event.chatRoomId(), event.chatMessageId());
        } catch (Exception e) {
            log.warn(
                    "chat_rooms.last_message_id 갱신 실패 chatRoomId={} chatMessageId={} : {}",
                    event.chatRoomId(), event.chatMessageId(), e.getMessage()
            );
        }
    }

    /**
     * RabbitMQ 채팅 이벤트 발행
     * 
     * @param event
     */
    private void publishToRabbit(ChatMessagePublishedEvent event) {
        chatRabbitPublishPort.ifAvailable(port -> {
            try {
                port.publishChatMessagePublished(
                        event.chatRoomId(),
                        event.chatMessageId(),
                        event.senderId(),
                        event.content(),
                        event.createdAt()
                );
            } catch (Exception e) {
                log.warn(
                        "RabbitMQ 채팅 이벤트 발행 실패 chatRoomId={} chatMessageId={} : {}",
                        event.chatRoomId(), event.chatMessageId(), e.getMessage()
                );
            }
        });
    }
}

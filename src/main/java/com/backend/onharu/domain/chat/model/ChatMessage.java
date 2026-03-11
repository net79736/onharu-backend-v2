package com.backend.onharu.domain.chat.model;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.user.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 채팅 메시지 엔티티
 * 주요필드:
 * - chatRoom: 채팅방
 * - sender: 발신자(메세지를 보낸 사용자 ID)
 * - content: 메시지 내용
 * IDX_CHAT_ROOM_MESSAGE: 특정 채팅방의 메시지를 조회 {chatRoomId, ChatMessageId}
 */
@Table(
        name = "chat_messages", indexes = {
        @Index(
                name = "IDX_CHAT_ROOM_MESSAGE",
                columnList = "CHAT_ROOM_ID, ID"
        )
})
@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAT_ROOM_ID", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User sender;

    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Builder
    public ChatMessage(ChatRoom chatRoom, User sender, String content) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.content = content;
    }
}

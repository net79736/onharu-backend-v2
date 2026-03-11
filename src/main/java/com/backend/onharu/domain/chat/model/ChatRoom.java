package com.backend.onharu.domain.chat.model;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.common.enums.RoomType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 채팅방 엔티티
 * 주요필드:
 * - roomType: 채팅방 종류(일대일 대화방, 그룹 대화방)
 * - lastMessageId: 채팅방의 마지막 메시지 ID
 */
@Table(
        name = "chat_rooms"
)
@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom extends BaseEntity {

    @Column(name = "NAME", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROOM_TYPE", nullable = false)
    private RoomType roomType;

    @Column(name = "LAST_MESSAGE_ID", nullable = true)
    private Long lastMessageId;

    @Builder
    public ChatRoom(String name, RoomType roomType) {
        this.name = name;
        this.roomType = roomType;
    }

    /**
     * 채팅방의 마지막으로 읽은 메시지 정보를 업데이트 합니다
     *
     * @param messageId 마지막으로 읽은 메시지 ID
     */
    public void updateLastMessage(Long messageId) {
        this.lastMessageId = messageId;
    }

    /**
     * 채팅방 수정
     * @param name 수정할 이름
     */
    public void update(String name) {
        this.name = name;
    }
}

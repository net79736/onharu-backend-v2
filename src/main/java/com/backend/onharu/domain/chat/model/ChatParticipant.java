package com.backend.onharu.domain.chat.model;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.user.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 채팅 참여자 엔티티
 * 주요필드:
 * - chatRoom: 채팅방 엔티티
 * - user: 사용자 엔티티
 * - lastReadMessageId: 마지막으로 읽은 메시지
 * UK_CHAT_ROOM_USER: 사용자의 채팅방 중복 생성 방지 {chatRoomId, userId}
 * IDX_PARTICIPANT_USER: 특정 사용자가 들어가 있는 채팅방 목록을 조회 {userId}
 */
@Table(
        name = "chat_participants", uniqueConstraints = {
        @UniqueConstraint(
                name = "UK_CHAT_ROOM_USER",
                columnNames = {
                        "CHAT_ROOM_ID",
                        "USER_ID"
                }
        )
},
        indexes = {
                @Index(
                        name = "IDX_PARTICIPANT_USER",
                        columnList = "USER_ID"
                )
        }
)
@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChatParticipant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHAT_ROOM_ID", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "LAST_READ_MESSAGE_ID", nullable = true)
    private Long lastReadMessageId;

    @Builder
    public ChatParticipant(ChatRoom chatRoom, User user) {
        this.chatRoom = chatRoom;
        this.user = user;
    }

    /**
     * 마지막으로 읽은 메시지 갱신
     * @param messageId 메시지 ID
     */
    public void updateLastReadMessageId(Long messageId) {
        this.lastReadMessageId = messageId;
    }
}

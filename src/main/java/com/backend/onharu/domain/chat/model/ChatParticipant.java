package com.backend.onharu.domain.chat.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.user.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅 참여자 엔티티
 * 주요필드:
 * - chatRoom: 채팅방 엔티티
 * - user: 사용자 엔티티
 * - lastReadMessageId: 마지막으로 읽은 메시지
 * - isActive: 채팅방 참가 여부
 *
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

    @Column(name = "IS_ACTIVE", nullable = false)
    private boolean isActive;

    @Builder
    public ChatParticipant(ChatRoom chatRoom, User user) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.isActive = true;
    }

    /**
     * 마지막으로 읽은 메시지 갱신
     * @param messageId 메시지 ID
     */
    public void updateLastReadMessageId(Long messageId) {
        this.lastReadMessageId = messageId;
    }

    /**
     * 마지막으로 읽은 메시지 ID를 "더 최신일 때만" 갱신한다.
     * - 기존 값이 null 이면(아직 한 번도 읽지 않음) 최초 갱신
     * - 기존 값보다 messageId 가 더 큰 경우에만 갱신(과거 메시지로 역행 방지)
     * 동시성/경쟁 상황에서 오래된 읽음 커서가 최신 커서를 덮어쓰는 것을 방지하기 위한 도메인 규칙.
     *
     * @param messageId 새로 읽은 메시지 ID (null 이면 no-op)
     * @return 실제로 값이 갱신되었으면 true, 아니면 false
     */
    public boolean advanceLastReadMessageId(Long messageId) {
        if (messageId == null) {
            return false;
        }
        if (this.lastReadMessageId != null && this.lastReadMessageId >= messageId) {
            return false;
        }
        updateLastReadMessageId(messageId);
        return true;
    }

    /**
     * 채팅 참여자가 채팅방을 떠난 경우
     */
    public void leaveChatRoom() {
        this.isActive = false;
    }

    /**
     * 채팅 참여자가 채팅방에 입장하는 경우
     */
    public void enterChatRoom() {
        this.isActive = true;
    }
}

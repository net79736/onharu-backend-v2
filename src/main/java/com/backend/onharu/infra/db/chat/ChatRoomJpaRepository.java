package com.backend.onharu.infra.db.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.onharu.domain.chat.model.ChatRoom;

/**
 * 채팅방 JPA 구현체
 */
public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 채팅방의 마지막 메시지 번호를 최신값으로 안전하게 갱신합니다.
     * * [왜 단일 UPDATE 쿼리를 쓰나요?]
     * 일반적인 JPA 수정 방식(조회 후 수정)은 메시지 추가 시 발생하는 잠금(S-Lock)과 충돌하여 
     * '데드락'을 유발할 수 있습니다. 이를 피하기 위해 DB에서 직접 원자적으로 갱신합니다.
     * 
     * ChatParticipantCommandService.java > updateLastReadMessage 과 하는 역할이 서로 다름.
     * updateLastReadMessage 는 특정 사용자가 마지막으로 읽은 메시지를 업데이트 하는 것임
     * 
     * 
     * @param messageId 기존 번호보다 클 때만 업데이트됨 (순서 보장)
     * @return 1: 갱신 성공, 0: 대상 없음 또는 이미 더 최신 상태
     */
    @Modifying
    @Query("""
            UPDATE ChatRoom cr
            SET cr.lastMessageId = :messageId
            WHERE cr.id = :chatRoomId
              AND (cr.lastMessageId IS NULL OR cr.lastMessageId < :messageId)
            """)
    int bumpLastMessageId(@Param("chatRoomId") Long chatRoomId, @Param("messageId") Long messageId);
}

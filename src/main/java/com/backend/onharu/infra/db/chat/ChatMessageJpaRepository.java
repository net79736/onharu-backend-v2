package com.backend.onharu.infra.db.chat;

import com.backend.onharu.domain.chat.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageJpaRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findTopByChatRoom_IdOrderByIdDesc(Long chatRoomId);

    /**
     * 채팅방의 안읽은 메시지 개수 계산하기
     *
     * @param chatRoomId        채팅방 ID
     * @param lastReadMessageId 마지막으로 읽은 메시지 ID
     * @return 안읽은 메시지 개수 계산하기
     */
    @Query("""
            SELECT COUNT(cm)
            FROM ChatMessage cm
            WHERE cm.chatRoom.id = :chatRoomId AND cm.id > :lastReadMessageId
            """)
    long countUnreadMessage(@Param("chatRoomId") Long chatRoomId, @Param("lastReadMessageId") Long lastReadMessageId);

    /**
     * 채팅 메시지 목록 조회 (커서)
     */
    @Query(value = """
            SELECT cm
            FROM ChatMessage cm
            WHERE cm.chatRoom.id = :chatRoomId
            AND (:cursorId IS NULL OR cm.id < :cursorId)
            ORDER BY cm.id DESC
            """)
    List<ChatMessage> findChatMessage(@Param("chatRoomId") Long chatRoomId, @Param("cursorId") Long cursorId, Pageable pageable);

}

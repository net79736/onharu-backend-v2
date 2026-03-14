package com.backend.onharu.infra.db.chat;

import com.backend.onharu.domain.chat.model.ChatParticipant;
import com.backend.onharu.domain.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantJpaRepository extends JpaRepository<ChatParticipant, Long> {

    /**
     * 채팅참여자 중 특정 채팅방의 사용자 조회
     *
     * @param chatRoomId 채팅방 ID
     * @param userId     사용자 ID
     * @return 채팅참여자
     */
    Optional<ChatParticipant> findByChatRoom_IdAndUser_Id(Long chatRoomId, Long userId);

    /**
     * 특정 채팅방의 정보 조회
     *
     * @param userId 사용자 ID
     * @return 채팅방 ID, 마지막 메시지 내용, 메시지 작성 시각이 포함된 ChatRoomSummary 리스트
     */
    @Query("""
            SELECT cr.id as id, cm.content as content, cm.createdAt as createdAt, cp.lastReadMessageId as lastReadMessageId
            FROM ChatParticipant cp JOIN cp.chatRoom cr LEFT JOIN ChatMessage cm ON cm.id = cr.lastMessageId
            WHERE cp.user.id = :userId
            ORDER BY cm.createdAt desc
            """)
    List<ChatRoomSummary> getChatRoomSummary(@Param("userId") Long userId);

    /**
     * 정렬된 채팅방 목록 조회
     * @param userId 사용자 ID
     * @return 특정 사용자의 채팅방 목록을 마지막 메시지 ID 를 기준으로 정렬
     */
    @Query("""
            SELECT cr
            FROM ChatParticipant cp JOIN cp.chatRoom cr LEFT JOIN ChatMessage cm ON cm.id = cr.lastMessageId
            WHERE cp.user.id = :userId
            ORDER BY cm.createdAt DESC
            """)
    List<ChatRoom> findSortedChatRooms(@Param("userId") Long userId);
}

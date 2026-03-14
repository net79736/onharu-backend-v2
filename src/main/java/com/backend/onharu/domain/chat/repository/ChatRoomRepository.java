package com.backend.onharu.domain.chat.repository;

import com.backend.onharu.domain.chat.model.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static com.backend.onharu.domain.chat.dto.ChatRoomRepositoryParam.FindChatRoomByIdParam;

/**
 * 채팅방 도메인 Repository
 */
public interface ChatRoomRepository {

    /**
     * 채팅방 저장
     *
     * @param chatRoom 채팅방 엔티티
     */
    ChatRoom save(ChatRoom chatRoom);

    /**
     * 채팅방 삭제
     *
     * @param chatRoom 채팅방 엔티티
     */
    void delete(ChatRoom chatRoom);

    /**
     * 전체 채팅방 목록 조회(페이징)
     *
     * @param pageable 페이징 정보
     * @return 페이징된 채팅방 목록
     */
    Page<ChatRoom> findAll(Pageable pageable);

    /**
     * 채팅방 ID 로 채팅방 조회
     *
     * @param param 채팅방 ID 를 포함한 파라미터
     * @return 채팅방 엔티티
     */
    ChatRoom findById(FindChatRoomByIdParam param);
}

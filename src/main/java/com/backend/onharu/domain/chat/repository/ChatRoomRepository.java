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

    /**
     * 채팅방의 마지막 메시지 ID 를 원자적으로 갱신 (단조 증가 보장).
     * 엔티티 조회 없이 단일 UPDATE 로 수행하므로 동일 트랜잭션 내 FK S-lock 경합에 의한 데드락을 피합니다.
     *
     * @param chatRoomId 채팅방 ID
     * @param messageId 새 마지막 메시지 ID
     * @return UPDATE 된 row 수
     */
    int bumpLastMessageId(Long chatRoomId, Long messageId);
}

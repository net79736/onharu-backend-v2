package com.backend.onharu.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import com.backend.onharu.domain.chat.dto.ChatParticipantRepositoryParam.FindByChatRoomIdAndUserIdParam;
import com.backend.onharu.domain.chat.dto.ChatParticipantRepositoryParam.FindChatParticipantByChatRoomIdParam;
import com.backend.onharu.domain.chat.dto.ChatParticipantRepositoryParam.FindChatParticipantByIdParam;
import com.backend.onharu.domain.chat.dto.ChatParticipantRepositoryParam.FindSortedChatRoomsParam;
import com.backend.onharu.domain.chat.dto.ChatParticipantRepositoryParam.GetChatRoomSummaryParam;
import com.backend.onharu.domain.chat.model.ChatParticipant;
import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.infra.db.chat.ChatRoomSummary;

/**
 * 채팅참여자 도메인
 */
public interface ChatParticipantRepository {

    /**
     * 채팅참여자 저장
     *
     * @param chatParticipant 채팅참여자 엔티티
     */
    ChatParticipant save(ChatParticipant chatParticipant);

    /**
     * 채팅참여자 제거
     *
     * @param chatParticipant 채팅참여자 엔티티
     */
    void delete(ChatParticipant chatParticipant);

    /**
     * 채팅참여자 단건 조회
     *
     * @param param 채팅참여자 ID 를 포함한 파라미터
     * @return 채팅참여자
     */
    ChatParticipant findById(FindChatParticipantByIdParam param);

    /**
     * 채팅참여자 중 특정 채팅방의 사용자 조회
     *
     * @param param 채팅방 ID 와 사용자 ID 를 포함한 파라미터
     * @return 특정 채팅방의 채팅참여자
     */
    ChatParticipant findByChatRoomIdAndUserId(FindByChatRoomIdAndUserIdParam param);

    /**
     * 채팅참여자 중 특정 채팅방의 사용자 조회(Optional)
     */
    Optional<ChatParticipant> getChatParticipantByChatRoomIdAndUserId(FindByChatRoomIdAndUserIdParam param);

    /**
     * 내가 참여한 채팅방 목록 조회
     *
     * @param param 사용자 ID 를 포함한 파라미터
     * @return 채팅방 ID, 채팅 메시지 내용, 채팅 메시지 작성 시각이 포함된 정보들의 목록
     */
    List<ChatRoomSummary> getChatRoomSummary(GetChatRoomSummaryParam param);

    /**
     * 정렬된 채팅방 목록 조회
     *
     * @param param 사용자 ID 를 포함한 파라미터
     * @return 채팅방 목록
     */
    List<ChatRoom> findSortedChatRooms(FindSortedChatRoomsParam param);

    /**
     * 특정 채팅방의 채팅참가자 목록 조회
     * @param param 채팅방 ID
     * @return 채팅 참가자 목록
     */
    List<ChatParticipant> findChatParticipantByChatRoomId(FindChatParticipantByChatRoomIdParam param);
}

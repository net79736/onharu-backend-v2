package com.backend.onharu.domain.chat.repository;

import com.backend.onharu.domain.chat.dto.ChatMessageRepositoryParam.FindChatMessageByIdParam;
import com.backend.onharu.domain.chat.dto.ChatMessageRepositoryParam.FindTopByChatRoom_IdOrderByIdDescParam;
import com.backend.onharu.domain.chat.model.ChatMessage;

import java.util.List;

import static com.backend.onharu.domain.chat.dto.ChatMessageRepositoryParam.CountUnreadMessageParam;
import static com.backend.onharu.domain.chat.dto.ChatMessageRepositoryParam.FindChatMessageParam;

/**
 * 채팅메시지 도메인 Repository 입니다.
 */
public interface ChatMessageRepository {

    void save(ChatMessage chatMessage);

    void delete(ChatMessage chatMessage);

    ChatMessage findChatMessageById(FindChatMessageByIdParam param);

    List<ChatMessage> findTopByChatRoom_IdOrderByIdDesc(FindTopByChatRoom_IdOrderByIdDescParam param);

    /**
     * 안읽은 메시지 수 계산
     *
     * @param param 채팅방 ID, 마지막으로 읽은 메시지 ID 를 포함한 파라미터
     * @return 안읽은 메세지 수
     */
    long countUnreadMessage(CountUnreadMessageParam param);

    /**
     * 채팅 메시지 목록 조회
     *
     * @param param 채팅방 ID, 마지막으로 읽은 메시지 ID 를 포함한 파라미터
     * @return 채팅 메시지 목록
     */
    List<ChatMessage> findChatMessage(FindChatMessageParam param);
}
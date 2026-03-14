package com.backend.onharu.domain.chat.service;

import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.backend.onharu.domain.chat.dto.ChatRoomQuery.FindChatRoomByIdQuery;
import static com.backend.onharu.domain.chat.dto.ChatRoomRepositoryParam.FindChatRoomByIdParam;

@Service
@RequiredArgsConstructor
public class ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;

    /**
     * 채팅방 조회 Query Service
     *
     * @param query 채팅방 ID 를 포함한 Query
     * @return 채팅방 엔티티
     */
    public ChatRoom findChatRoomById(FindChatRoomByIdQuery query) {
        return chatRoomRepository.findById(
                new FindChatRoomByIdParam(
                        query.chatRoomId())
        );
    }
}

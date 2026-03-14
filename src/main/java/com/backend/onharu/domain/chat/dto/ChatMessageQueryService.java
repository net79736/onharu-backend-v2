package com.backend.onharu.domain.chat.dto;

import com.backend.onharu.domain.chat.dto.ChatMessageQuery.CountUnreadMessageQuery;
import com.backend.onharu.domain.chat.model.ChatMessage;
import com.backend.onharu.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.backend.onharu.domain.chat.dto.ChatMessageQuery.FindChatMessageQuery;
import static com.backend.onharu.domain.chat.dto.ChatMessageRepositoryParam.CountUnreadMessageParam;
import static com.backend.onharu.domain.chat.dto.ChatMessageRepositoryParam.FindChatMessageParam;

@Service
@RequiredArgsConstructor
public class ChatMessageQueryService {

    private final ChatMessageRepository chatMessageRepository;

    /**
     * 안읽은 메세지 갯수 세기
     * @param query 채팅방 ID, 마지막으로 읽은 메시지 ID 를 포함한 query
     * @return 안읽은 메시지 수
     */
    public long countUnreadMessage(CountUnreadMessageQuery query) {
        // 마지막으로 읽은 메시지 ID 가 null 인 경우 0L 으로 고정
        Long lastReadMessageId = (query.lastReadMessageId() == null) ? 0L : query.lastReadMessageId();

        return chatMessageRepository.countUnreadMessage(
                new CountUnreadMessageParam(query.chatRoomId(), lastReadMessageId)
        );
    }

    /**
     * 채팅 메시지 목록 조회
     *
     * @param query 채팅방 ID, 마지막으로 읽은 메시지 ID, 페이징 정보를 포함한 query
     * @return 채팅 메시지 목록
     */
    public List<ChatMessage> findChatMessage(FindChatMessageQuery query) {
        return chatMessageRepository.findChatMessage(
                new FindChatMessageParam(
                        query.chatRoomId(),
                        query.lastReadMessageId(),
                        query.pageable()
                )
        );
    }
}

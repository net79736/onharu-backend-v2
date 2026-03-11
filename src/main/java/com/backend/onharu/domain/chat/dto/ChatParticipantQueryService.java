package com.backend.onharu.domain.chat.dto;

import com.backend.onharu.domain.chat.model.ChatParticipant;
import com.backend.onharu.domain.chat.repository.ChatParticipantRepository;
import com.backend.onharu.infra.db.chat.ChatRoomSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.backend.onharu.domain.chat.dto.ChatParticipantQuery.*;
import static com.backend.onharu.domain.chat.dto.ChatParticipantQuery.GetChatRoomSummaryQuery;
import static com.backend.onharu.domain.chat.dto.ChatParticipantRepositoryParam.*;
import static com.backend.onharu.domain.chat.dto.ChatParticipantRepositoryParam.GetChatRoomSummaryParam;

@Service
@RequiredArgsConstructor
public class ChatParticipantQueryService {

    private final ChatParticipantRepository chatParticipantRepository;

    /**
     * 내가 참여한 채팅방 목록 조회 서비스
     * @param query 사용자 ID 를 포함한 Query
     * @return 채팅방 ID, 채팅 메시지 내용, 채팅 메시지 작성 시각이 포함된 정보들의 목록
     */
    @Transactional(readOnly = true)
    public List<ChatRoomSummary> getChatRoomSummary(GetChatRoomSummaryQuery query) {
        return chatParticipantRepository.getChatRoomSummary(
                new GetChatRoomSummaryParam(query.userId())
        );
    }

    /**
     * 채팅방 참가자 조회
     * @param query 채팅방 ID 와 사용자 ID 를 포함한 Query
     * @return 채팅방 참가자 엔티티
     */
    public ChatParticipant getParticipant(GetChatParticipantQuery query) {
        return chatParticipantRepository.findByChatRoomIdAndUserId(
                new FindByChatRoomIdAndUserIdParam(query.ChatRoomId(), query.userId())
        );
    }
}

package com.backend.onharu.domain.chat.service;

import com.backend.onharu.domain.chat.dto.ChatParticipantCommand.CreateChatParticipantCommand;
import com.backend.onharu.domain.chat.dto.ChatParticipantCommand.DeleteChatParticipantCommand;
import com.backend.onharu.domain.chat.dto.ChatParticipantCommand.UpdateLastReadMessageCommand;
import com.backend.onharu.domain.chat.dto.ChatParticipantCommand.updateChatParticipantCommand;
import com.backend.onharu.domain.chat.model.ChatParticipant;
import com.backend.onharu.domain.chat.repository.ChatParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.backend.onharu.domain.chat.dto.ChatParticipantRepositoryParam.FindByChatRoomIdAndUserIdParam;

@Service
@RequiredArgsConstructor
public class ChatParticipantCommandService {

    private final ChatParticipantRepository chatParticipantRepository;

    /**
     * 채팅참여자 생성
     *
     * @param command 채팅방 엔티티와 사용자 엔티티를 포함한 Command
     * @return 생성한 채팅참여자 엔티티
     */
    @Transactional
    public ChatParticipant createChatParticipant(CreateChatParticipantCommand command) {
        // 채팅참여자 객체 생성
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(command.chatRoom())
                .user(command.user())
                .build();

        // 채팅참여자 DB 저장
        return chatParticipantRepository.save(chatParticipant);
    }

    /**
     * 채팅참가자 수정사항 DB 반영
     */
    public void updateChatParticipant(updateChatParticipantCommand command) {
        chatParticipantRepository.save(command.chatParticipant());
    }

    /**
     * 채팅참여자가 마지막으로 읽은 메시지 업데이트
     */
    @Transactional
    public void updateLastReadMessage(UpdateLastReadMessageCommand command) {
        // 특정 채팅방에 참가하는 사용자 조회
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomIdAndUserId(
                new FindByChatRoomIdAndUserIdParam(command.chatRoomId(), command.userId())
        );

        // 마지막으로 읽은 메시지가 없거나 최신 메시지인 경우에만 업데이트 실행
        if (chatParticipant.getLastReadMessageId() == null || chatParticipant.getLastReadMessageId() < command.lastMessageId()) {
            chatParticipant.updateLastReadMessageId(command.lastMessageId());
        }

        chatParticipantRepository.save(chatParticipant);
    }

    /**
     * 채팅참여자가 채팅방 탈퇴
     */
    public void deleteChatParticipant(DeleteChatParticipantCommand command) {
        chatParticipantRepository.delete(command.chatParticipant());
    }
}

package com.backend.onharu.domain.chat.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.chat.dto.ChatParticipantCommand.CreateChatParticipantCommand;
import com.backend.onharu.domain.chat.dto.ChatParticipantCommand.DeleteChatParticipantCommand;
import com.backend.onharu.domain.chat.dto.ChatParticipantCommand.UpdateLastReadMessageCommand;
import com.backend.onharu.domain.chat.dto.ChatParticipantCommand.updateChatParticipantCommand;
import com.backend.onharu.domain.chat.dto.ChatParticipantRepositoryParam.FindByChatRoomIdAndUserIdParam;
import com.backend.onharu.domain.chat.model.ChatParticipant;
import com.backend.onharu.domain.chat.repository.ChatParticipantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatParticipantCommandService {

    private final ChatParticipantRepository chatParticipantRepository;

    /**
     * 채팅참여자 생성
     *
     * @param command 채팅방 엔티티와 사용자 엔티티를 포함한 Command
     */
    @Transactional
    public void createChatParticipant(CreateChatParticipantCommand command) {
        // 기존 채팅방에 참여했는지 조회
        Optional<ChatParticipant> existChatParticipant = chatParticipantRepository.getChatParticipantByChatRoomIdAndUserId(
                new FindByChatRoomIdAndUserIdParam(
                        command.chatRoom().getId(),
                        command.user().getId()
                )
        );

        // 만약 기존에 참여하던 채팅방에 재입장인 경우
        if (existChatParticipant.isPresent()) {
            ChatParticipant exist = existChatParticipant.get();
            exist.enterChatRoom();
            return;
        }

        // 채팅참여자 객체 생성
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(command.chatRoom())
                .user(command.user())
                .build();

        // 채팅참여자 DB 저장
        chatParticipantRepository.save(chatParticipant);
    }

    /**
     * 채팅참가자 수정사항 DB 반영
     */
    public void updateChatParticipant(updateChatParticipantCommand command) {
        chatParticipantRepository.save(command.chatParticipant());
    }

    /**
     * 채팅 참여자가 마지막으로 읽은 메시지 업데이트
     */
    @Transactional
    public void updateLastReadMessage(UpdateLastReadMessageCommand command) {
        // 특정 채팅방에 참가하는 사용자 조회
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomIdAndUserId(
                new FindByChatRoomIdAndUserIdParam(command.chatRoomId(), command.userId())
        );

        // 마지막으로 읽은 메시지 갱신 규칙(null 체크 + 역행 방지)은 엔티티가 소유
        chatParticipant.advanceLastReadMessageId(command.lastMessageId());
    }

    /**
     * 채팅참여자가 채팅방 탈퇴
     */
    public void deleteChatParticipant(DeleteChatParticipantCommand command) {
        chatParticipantRepository.delete(command.chatParticipant());
    }
}

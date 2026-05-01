package com.backend.onharu.domain.chat.service;

import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.chat.repository.ChatRoomRepository;
import com.backend.onharu.domain.common.enums.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.backend.onharu.domain.chat.dto.ChatRoomCommand.*;
import static com.backend.onharu.domain.chat.dto.ChatRoomRepositoryParam.FindChatRoomByIdParam;

/**
 * 채팅방 도메인의 Command Service 입니다.
 */
@Service
@RequiredArgsConstructor
public class ChatRoomCommandService {

    private final ChatRoomRepository chatRoomRepository;

    /**
     * 채팅방 생성 (일대일)
     */
    @Transactional
    public ChatRoom createChatRoom(CreateChatRoomCommand command) {
        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .name(command.name())
                .roomType(RoomType.ONE_TO_ONE)
                .build();

        // 생성한 채팅방 엔티티를 DB 에 저장
        return chatRoomRepository.save(chatRoom);
    }

    /**
     * 채팅방 수정
     *
     * @param command 채팅방 엔티티를 포함한 Command
     */
    @Transactional
    public void updateChatRoomByName(UpdateChatRoomCommand command) {

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(
                new FindChatRoomByIdParam(command.chatRoomId())
        );

        // 채팅방 수정
        chatRoom.update(command.name());

        // DB 에 수정사항 반영
        chatRoomRepository.save(chatRoom);
    }

    /**
     * 참가한 채팅방 탈퇴
     */
    @Transactional
    public void leaveChatRoom(LeaveChatRoomCommand command) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(
                new FindChatRoomByIdParam(command.chatRoomId())
        );

        // 채팅방 삭제
        chatRoomRepository.delete(chatRoom);
    }
}

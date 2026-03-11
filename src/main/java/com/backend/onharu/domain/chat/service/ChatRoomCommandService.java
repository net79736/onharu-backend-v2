package com.backend.onharu.domain.chat.service;

import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.chat.repository.ChatRoomRepository;
import com.backend.onharu.domain.common.enums.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.backend.onharu.domain.chat.dto.ChatRoomCommand.*;
import static com.backend.onharu.domain.chat.dto.ChatRoomRepositoryParam.FindChatRoomByIdParam;

@Service
@RequiredArgsConstructor
public class ChatRoomCommandService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoom createChatRoom(String name) {
        // 채팅방 엔티티 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .roomType(RoomType.ONE_TO_ONE) // 기본적으로 일대일 채팅방 생성
                .name(name)
                .build();

        // 생성한 채팅방 엔티티를 DB 에 저장
        return chatRoomRepository.save(chatRoom);
    }

    /**
     * 채팅방 초대
     */
    public void inviteChatRoom(InviteChatRoomCommand command) {

    }


    /**
     * 채팅방 수정
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
     * 채팅방 탈퇴
     */
    @Transactional
    public void leaveChatRoom(LeaveChatRoomCommand command) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(
                new FindChatRoomByIdParam(command.chatRoomId())
        );

        // 채팅방 탈퇴
        chatRoomRepository.delete(chatRoom);
    }
}

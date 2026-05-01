package com.backend.onharu.domain.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.chat.dto.ChatRoomCommand.CreateChatRoomCommand;
import com.backend.onharu.domain.chat.dto.ChatRoomCommand.LeaveChatRoomCommand;
import com.backend.onharu.domain.chat.dto.ChatRoomCommand.UpdateChatRoomCommand;
import com.backend.onharu.domain.chat.dto.ChatRoomRepositoryParam.FindChatRoomByIdParam;
import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.chat.repository.ChatRoomRepository;
import com.backend.onharu.domain.common.enums.RoomType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.chat.ChatMessageJpaRepository;
import com.backend.onharu.infra.db.chat.ChatParticipantJpaRepository;
import com.backend.onharu.infra.db.chat.ChatRoomJpaRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ChatRoomCommandService 통합 테스트")
class ChatRoomCommandServiceTest {

    @Autowired
    private ChatRoomCommandService chatRoomCommandService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRoomJpaRepository chatRoomJpaRepository;

    @Autowired
    private ChatParticipantJpaRepository chatParticipantJpaRepository;

    @Autowired
    private ChatMessageJpaRepository chatMessageJpaRepository;

    // @Transactional 클래스 레벨 적용으로 각 테스트가 자동 롤백됨.

    private CreateChatRoomCommand sampleCreateCommand(String suffix) {
        return new CreateChatRoomCommand(
                "room-" + suffix + "-" + UUID.randomUUID().toString().substring(0, 6),
                RoomType.ONE_TO_ONE,
                2L,
                1L
        );
    }

    @Nested
    @DisplayName("createChatRoom")
    class CreateChatRoom {

        @Test
        @DisplayName("새 채팅방을 저장하고 ONE_TO_ONE 타입으로 반환한다")
        void createChatRoom_returnsPersistedRoom() {
            CreateChatRoomCommand command = sampleCreateCommand("create");

            ChatRoom saved = chatRoomCommandService.createChatRoom(command);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo(command.name());
            assertThat(saved.getRoomType()).isEqualTo(RoomType.ONE_TO_ONE);
            assertThat(saved.getLastMessageId()).isNull();
            assertThat(chatRoomJpaRepository.existsById(saved.getId())).isTrue();
        }
    }

    @Nested
    @DisplayName("updateChatRoomByName")
    class UpdateChatRoomByName {

        @Test
        @DisplayName("채팅방 이름을 수정한다")
        void updateChatRoomByName_updatesName() {
            ChatRoom saved = chatRoomCommandService.createChatRoom(sampleCreateCommand("update"));

            chatRoomCommandService.updateChatRoomByName(
                    new UpdateChatRoomCommand(saved.getId(), "renamed-room"));

            ChatRoom reloaded = chatRoomRepository.findById(new FindChatRoomByIdParam(saved.getId()));
            assertThat(reloaded.getName()).isEqualTo("renamed-room");
        }

        @Test
        @DisplayName("존재하지 않는 채팅방 ID 로 수정 시 예외가 발생한다")
        void updateChatRoomByName_notFound_throws() {
            assertThatThrownBy(() ->
                    chatRoomCommandService.updateChatRoomByName(
                            new UpdateChatRoomCommand(999_999L, "new-name")))
                    .isInstanceOf(CoreException.class);
        }
    }

    @Nested
    @DisplayName("leaveChatRoom")
    class LeaveChatRoom {

        @Test
        @DisplayName("leaveChatRoom 은 채팅방 엔티티를 삭제한다")
        void leaveChatRoom_deletesRoom() {
            ChatRoom saved = chatRoomCommandService.createChatRoom(sampleCreateCommand("leave"));
            Long roomId = saved.getId();

            chatRoomCommandService.leaveChatRoom(new LeaveChatRoomCommand(roomId, 1L));

            assertThat(chatRoomJpaRepository.existsById(roomId)).isFalse();
        }

        @Test
        @DisplayName("없는 채팅방에 대한 탈퇴 요청은 예외가 발생한다")
        void leaveChatRoom_notFound_throws() {
            assertThatThrownBy(() ->
                    chatRoomCommandService.leaveChatRoom(
                            new LeaveChatRoomCommand(999_999L, 1L)))
                    .isInstanceOf(CoreException.class);
        }
    }
}

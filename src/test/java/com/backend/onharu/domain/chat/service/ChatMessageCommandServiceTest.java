package com.backend.onharu.domain.chat.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.backend.onharu.domain.chat.dto.ChatMessageCommand.CreateMessageCommand;
import com.backend.onharu.domain.chat.dto.ChatRoomCommand.CreateChatRoomCommand;
import com.backend.onharu.domain.chat.model.ChatMessage;
import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.RoomType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.chat.ChatMessageJpaRepository;
import com.backend.onharu.infra.db.chat.ChatParticipantJpaRepository;
import com.backend.onharu.infra.db.chat.ChatRoomJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ChatMessageCommandService 통합 테스트")
class ChatMessageCommandServiceTest {

    @Autowired
    private ChatMessageCommandService chatMessageCommandService;

    @Autowired
    private ChatRoomCommandService chatRoomCommandService;

    @Autowired
    private ChatMessageJpaRepository chatMessageJpaRepository;

    @Autowired
    private ChatParticipantJpaRepository chatParticipantJpaRepository;

    @Autowired
    private ChatRoomJpaRepository chatRoomJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @BeforeEach
    void setUp() {
        chatMessageJpaRepository.deleteAll();
        chatParticipantJpaRepository.deleteAll();
        chatRoomJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    private User createUser(String suffix) {
        return userJpaRepository.save(
                User.builder()
                        .loginId("chat-test-" + suffix + "-" + UUID.randomUUID().toString().substring(0, 8))
                        .password("password123")
                        .name("테스트유저-" + suffix)
                        .phone("01012345678")
                        .userType(UserType.CHILD)
                        .statusType(StatusType.ACTIVE)
                        .providerType(ProviderType.LOCAL)
                        .build());
    }

    private ChatRoom createRoom(String suffix) {
        return chatRoomCommandService.createChatRoom(
                new CreateChatRoomCommand(
                        "msg-room-" + suffix + "-" + UUID.randomUUID().toString().substring(0, 6),
                        RoomType.ONE_TO_ONE, 2L, 1L));
    }

    @Test
    @DisplayName("채팅메시지를 저장하고 저장된 엔티티를 반환한다")
    void createChatMessage_persistsAndReturns() {
        ChatRoom room = createRoom("ok");
        User sender = createUser("sender");

        ChatMessage saved = chatMessageCommandService.createChatMessage(
                new CreateMessageCommand(room, sender, "안녕하세요"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getContent()).isEqualTo("안녕하세요");
        assertThat(saved.getChatRoom().getId()).isEqualTo(room.getId());
        assertThat(saved.getSender().getId()).isEqualTo(sender.getId());
        assertThat(chatMessageJpaRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    @DisplayName("동일 채팅방에 여러 메시지를 저장해도 각각 고유 ID 를 갖는다")
    void createChatMessage_multiple_independentIds() {
        ChatRoom room = createRoom("multi");
        User sender = createUser("multi");

        ChatMessage first = chatMessageCommandService.createChatMessage(
                new CreateMessageCommand(room, sender, "첫번째"));
        ChatMessage second = chatMessageCommandService.createChatMessage(
                new CreateMessageCommand(room, sender, "두번째"));

        assertThat(first.getId()).isNotEqualTo(second.getId());
        assertThat(chatMessageJpaRepository.count()).isEqualTo(2);
    }
}

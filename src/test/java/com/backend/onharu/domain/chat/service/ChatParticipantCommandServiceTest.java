package com.backend.onharu.domain.chat.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.backend.onharu.domain.chat.dto.ChatParticipantCommand.CreateChatParticipantCommand;
import com.backend.onharu.domain.chat.dto.ChatParticipantCommand.DeleteChatParticipantCommand;
import com.backend.onharu.domain.chat.dto.ChatParticipantCommand.UpdateLastReadMessageCommand;
import com.backend.onharu.domain.chat.dto.ChatParticipantRepositoryParam.FindByChatRoomIdAndUserIdParam;
import com.backend.onharu.domain.chat.dto.ChatRoomCommand.CreateChatRoomCommand;
import com.backend.onharu.domain.chat.model.ChatMessage;
import com.backend.onharu.domain.chat.model.ChatParticipant;
import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.chat.repository.ChatParticipantRepository;
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
@DisplayName("ChatParticipantCommandService 통합 테스트")
class ChatParticipantCommandServiceTest {

    @Autowired
    private ChatParticipantCommandService chatParticipantCommandService;

    @Autowired
    private ChatMessageCommandService chatMessageCommandService;

    @Autowired
    private ChatRoomCommandService chatRoomCommandService;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private ChatParticipantJpaRepository chatParticipantJpaRepository;

    @Autowired
    private ChatMessageJpaRepository chatMessageJpaRepository;

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
                        .loginId("part-" + suffix + "-" + UUID.randomUUID().toString().substring(0, 8))
                        .password("password123")
                        .name("참가자-" + suffix)
                        .phone("01012345678")
                        .userType(UserType.CHILD)
                        .statusType(StatusType.ACTIVE)
                        .providerType(ProviderType.LOCAL)
                        .build());
    }

    private ChatRoom createRoom(String suffix) {
        return chatRoomCommandService.createChatRoom(
                new CreateChatRoomCommand(
                        "part-room-" + suffix + "-" + UUID.randomUUID().toString().substring(0, 6),
                        RoomType.ONE_TO_ONE, 2L, 1L));
    }

    @Test
    @DisplayName("신규 참여자 생성 시 isActive=true 로 저장된다")
    void createChatParticipant_newParticipant() {
        ChatRoom room = createRoom("new");
        User user = createUser("new");

        chatParticipantCommandService.createChatParticipant(
                new CreateChatParticipantCommand(room, user));

        Optional<ChatParticipant> participant = chatParticipantRepository.getChatParticipantByChatRoomIdAndUserId(
                new FindByChatRoomIdAndUserIdParam(room.getId(), user.getId()));
        assertThat(participant).isPresent();
        assertThat(participant.get().isActive()).isTrue();
        assertThat(chatParticipantJpaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("기존 참여자가 재입장 시 UK 중복 없이 isActive=true 로 복구된다")
    void createChatParticipant_reEntry_reactivatesInstead() {
        ChatRoom room = createRoom("reentry");
        User user = createUser("reentry");

        // 최초 입장 후 탈퇴(leaveChatRoom 호출로 isActive=false)
        chatParticipantCommandService.createChatParticipant(
                new CreateChatParticipantCommand(room, user));
        ChatParticipant p = chatParticipantRepository.findByChatRoomIdAndUserId(
                new FindByChatRoomIdAndUserIdParam(room.getId(), user.getId()));
        p.leaveChatRoom();
        chatParticipantJpaRepository.saveAndFlush(p);

        // 재입장
        chatParticipantCommandService.createChatParticipant(
                new CreateChatParticipantCommand(room, user));

        // 같은 row 가 재활성화 됐고 row 개수는 1 유지
        ChatParticipant reactivated = chatParticipantRepository.findByChatRoomIdAndUserId(
                new FindByChatRoomIdAndUserIdParam(room.getId(), user.getId()));
        assertThat(reactivated.isActive()).isTrue();
        assertThat(chatParticipantJpaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("updateLastReadMessage 는 기존 값보다 큰 messageId 에서만 반영된다 (단조 증가)")
    void updateLastReadMessage_onlyIncreases() {
        ChatRoom room = createRoom("lastread");
        User user = createUser("lastread");
        chatParticipantCommandService.createChatParticipant(
                new CreateChatParticipantCommand(room, user));

        ChatMessage m1 = chatMessageCommandService.createChatMessage(
                new com.backend.onharu.domain.chat.dto.ChatMessageCommand.CreateMessageCommand(room, user, "첫번째"));
        ChatMessage m2 = chatMessageCommandService.createChatMessage(
                new com.backend.onharu.domain.chat.dto.ChatMessageCommand.CreateMessageCommand(room, user, "두번째"));

        chatParticipantCommandService.updateLastReadMessage(
                new UpdateLastReadMessageCommand(room.getId(), user.getId(), m2.getId()));
        chatParticipantCommandService.updateLastReadMessage(
                new UpdateLastReadMessageCommand(room.getId(), user.getId(), m1.getId())); // 더 작은 값 — 무시되어야

        ChatParticipant reloaded = chatParticipantRepository.findByChatRoomIdAndUserId(
                new FindByChatRoomIdAndUserIdParam(room.getId(), user.getId()));
        assertThat(reloaded.getLastReadMessageId()).isEqualTo(m2.getId());
    }

    @Test
    @DisplayName("deleteChatParticipant 는 row 를 제거한다")
    void deleteChatParticipant_removesRow() {
        ChatRoom room = createRoom("delete");
        User user = createUser("delete");
        chatParticipantCommandService.createChatParticipant(
                new CreateChatParticipantCommand(room, user));
        ChatParticipant p = chatParticipantRepository.findByChatRoomIdAndUserId(
                new FindByChatRoomIdAndUserIdParam(room.getId(), user.getId()));

        chatParticipantCommandService.deleteChatParticipant(
                new DeleteChatParticipantCommand(p));

        assertThat(chatParticipantJpaRepository.existsById(p.getId())).isFalse();
    }
}

package com.backend.onharu.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.backend.onharu.domain.chat.dto.ChatMessageCommand.CreateChatMessageCommand;
import com.backend.onharu.domain.chat.dto.ChatMessageCommand.ReadMessageCommand;
import com.backend.onharu.domain.chat.dto.ChatParticipantRepositoryParam.FindByChatRoomIdAndUserIdParam;
import com.backend.onharu.domain.chat.dto.ChatRoomCommand.CreateChatRoomCommand;
import com.backend.onharu.domain.chat.dto.ChatRoomCommand.LeaveChatRoomCommand;
import com.backend.onharu.domain.chat.model.ChatParticipant;
import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.chat.repository.ChatParticipantRepository;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.RoomType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.chat.ChatMessageJpaRepository;
import com.backend.onharu.infra.db.chat.ChatParticipantJpaRepository;
import com.backend.onharu.infra.db.chat.ChatRoomJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import com.backend.onharu.infra.websocket.ChatMessageResponse;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ChatFacade 통합 테스트")
class ChatFacadeTest {

    @Autowired
    private ChatFacade chatFacade;

    @Autowired
    private ChatParticipantRepository chatParticipantRepository;

    @Autowired
    private ChatRoomJpaRepository chatRoomJpaRepository;

    @Autowired
    private ChatParticipantJpaRepository chatParticipantJpaRepository;

    @Autowired
    private ChatMessageJpaRepository chatMessageJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @BeforeEach
    void setUp() {
        chatMessageJpaRepository.deleteAll();
        chatParticipantJpaRepository.deleteAll();
        chatRoomJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    private User saveUser(String suffix, StatusType statusType) {
        return userJpaRepository.save(
                User.builder()
                        .loginId("facade-" + suffix + "-" + UUID.randomUUID().toString().substring(0, 8))
                        .password("password123")
                        .name("유저-" + suffix)
                        .phone("01012345678")
                        .userType(UserType.CHILD)
                        .statusType(statusType)
                        .providerType(ProviderType.LOCAL)
                        .build());
    }

    private User saveActiveUser(String suffix) {
        return saveUser(suffix, StatusType.ACTIVE);
    }

    @Nested
    @DisplayName("createChatRoom")
    class CreateChatRoom {

        @Test
        @DisplayName("두 사용자에 대해 일대일 채팅방 + 참여자 2명을 생성한다")
        void createChatRoom_createsRoomWithTwoParticipants() {
            User requester = saveActiveUser("req");
            User target = saveActiveUser("tgt");

            ChatRoom room = chatFacade.createChatRoom(
                    new CreateChatRoomCommand("room", RoomType.ONE_TO_ONE, target.getId(), requester.getId()));

            assertThat(room.getId()).isNotNull();
            assertThat(chatRoomJpaRepository.existsById(room.getId())).isTrue();

            ChatParticipant me = chatParticipantRepository.findByChatRoomIdAndUserId(
                    new FindByChatRoomIdAndUserIdParam(room.getId(), requester.getId()));
            ChatParticipant other = chatParticipantRepository.findByChatRoomIdAndUserId(
                    new FindByChatRoomIdAndUserIdParam(room.getId(), target.getId()));
            assertThat(me.isActive()).isTrue();
            assertThat(other.isActive()).isTrue();
        }

        @Test
        @DisplayName("자기 자신과의 채팅방 생성 시 CAN_NOT_CHAT_WITH_ONESELF 예외")
        void createChatRoom_selfTarget_throws() {
            User user = saveActiveUser("self");

            assertThatThrownBy(() ->
                    chatFacade.createChatRoom(
                            new CreateChatRoomCommand("self-room", RoomType.ONE_TO_ONE, user.getId(), user.getId())))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.Chat.CAN_NOT_CHAT_WITH_ONESELF);
        }

        @Test
        @DisplayName("상대 계정이 DELETED 상태면 verifyStatus 예외 발생")
        void createChatRoom_deletedTarget_throws() {
            User requester = saveActiveUser("req2");
            User deleted = saveUser("del", StatusType.DELETED);

            assertThatThrownBy(() ->
                    chatFacade.createChatRoom(
                            new CreateChatRoomCommand("room2", RoomType.ONE_TO_ONE, deleted.getId(), requester.getId())))
                    .isInstanceOf(CoreException.class);
        }
    }

    @Nested
    @DisplayName("createChatMessage")
    class CreateChatMessage {

        @Test
        @DisplayName("메시지 저장 후 응답에 senderId, content, createdAt 이 포함된다")
        void createChatMessage_returnsResponse() {
            User requester = saveActiveUser("msg1");
            User target = saveActiveUser("msg2");
            ChatRoom room = chatFacade.createChatRoom(
                    new CreateChatRoomCommand("msg-room", RoomType.ONE_TO_ONE, target.getId(), requester.getId()));

            ChatMessageResponse response = chatFacade.createChatMessage(
                    new CreateChatMessageCommand(room.getId(), requester.getId(), "안녕하세요"));

            assertThat(response.chatMessageId()).isNotNull();
            assertThat(response.sender()).isEqualTo(requester.getId());
            assertThat(response.content()).isEqualTo("안녕하세요");
            assertThat(response.createdAt()).isNotNull();
            assertThat(chatMessageJpaRepository.existsById(response.chatMessageId())).isTrue();
        }

        @Test
        @DisplayName("메시지 생성 시 발신자의 lastReadMessageId 가 해당 메시지로 갱신된다")
        void createChatMessage_updatesSenderLastReadId() {
            User requester = saveActiveUser("lr1");
            User target = saveActiveUser("lr2");
            ChatRoom room = chatFacade.createChatRoom(
                    new CreateChatRoomCommand("lr-room", RoomType.ONE_TO_ONE, target.getId(), requester.getId()));

            ChatMessageResponse response = chatFacade.createChatMessage(
                    new CreateChatMessageCommand(room.getId(), requester.getId(), "자동 읽음"));

            ChatParticipant senderParticipant = chatParticipantRepository.findByChatRoomIdAndUserId(
                    new FindByChatRoomIdAndUserIdParam(room.getId(), requester.getId()));
            assertThat(senderParticipant.getLastReadMessageId()).isEqualTo(response.chatMessageId());
        }
    }

    @Nested
    @DisplayName("readMessage / leaveChatRoom")
    class ReadAndLeave {

        @Test
        @DisplayName("readMessage 는 lastReadMessageId 를 갱신한다")
        void readMessage_updatesLastReadId() {
            User u1 = saveActiveUser("r1");
            User u2 = saveActiveUser("r2");
            ChatRoom room = chatFacade.createChatRoom(
                    new CreateChatRoomCommand("read-room", RoomType.ONE_TO_ONE, u2.getId(), u1.getId()));
            ChatMessageResponse msg = chatFacade.createChatMessage(
                    new CreateChatMessageCommand(room.getId(), u1.getId(), "hello"));

            chatFacade.readMessage(new ReadMessageCommand(room.getId(), u2.getId(), msg.chatMessageId()));

            ChatParticipant p = chatParticipantRepository.findByChatRoomIdAndUserId(
                    new FindByChatRoomIdAndUserIdParam(room.getId(), u2.getId()));
            assertThat(p.getLastReadMessageId()).isEqualTo(msg.chatMessageId());
        }

        @Test
        @DisplayName("leaveChatRoom 은 해당 참여자의 isActive 를 false 로 전환한다")
        void leaveChatRoom_deactivatesParticipant() {
            User u1 = saveActiveUser("l1");
            User u2 = saveActiveUser("l2");
            ChatRoom room = chatFacade.createChatRoom(
                    new CreateChatRoomCommand("leave-room", RoomType.ONE_TO_ONE, u2.getId(), u1.getId()));

            chatFacade.leaveChatRoom(new LeaveChatRoomCommand(room.getId(), u1.getId()));

            ChatParticipant p = chatParticipantRepository.findByChatRoomIdAndUserId(
                    new FindByChatRoomIdAndUserIdParam(room.getId(), u1.getId()));
            assertThat(p.isActive()).isFalse();
        }
    }
}

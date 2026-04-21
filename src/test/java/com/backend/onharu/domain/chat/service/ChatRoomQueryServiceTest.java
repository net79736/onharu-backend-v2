package com.backend.onharu.domain.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.backend.onharu.domain.chat.dto.ChatRoomCommand.CreateChatRoomCommand;
import com.backend.onharu.domain.chat.dto.ChatRoomQuery.FindChatRoomByIdQuery;
import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.common.enums.RoomType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.chat.ChatMessageJpaRepository;
import com.backend.onharu.infra.db.chat.ChatParticipantJpaRepository;
import com.backend.onharu.infra.db.chat.ChatRoomJpaRepository;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ChatRoomQueryService 통합 테스트")
class ChatRoomQueryServiceTest {

    @Autowired
    private ChatRoomQueryService chatRoomQueryService;

    @Autowired
    private ChatRoomCommandService chatRoomCommandService;

    @Autowired
    private ChatRoomJpaRepository chatRoomJpaRepository;

    @Autowired
    private ChatParticipantJpaRepository chatParticipantJpaRepository;

    @Autowired
    private ChatMessageJpaRepository chatMessageJpaRepository;

    @BeforeEach
    void setUp() {
        chatMessageJpaRepository.deleteAll();
        chatParticipantJpaRepository.deleteAll();
        chatRoomJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("존재하는 채팅방 ID 로 조회하면 엔티티를 반환한다")
    void findChatRoomById_returnsEntity() {
        ChatRoom saved = chatRoomCommandService.createChatRoom(
                new CreateChatRoomCommand(
                        "query-room-" + UUID.randomUUID().toString().substring(0, 6),
                        RoomType.ONE_TO_ONE, 2L, 1L));

        ChatRoom found = chatRoomQueryService.findChatRoomById(new FindChatRoomByIdQuery(saved.getId()));

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo(saved.getName());
        assertThat(found.getRoomType()).isEqualTo(RoomType.ONE_TO_ONE);
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 ID 조회 시 CoreException 이 발생한다")
    void findChatRoomById_notFound_throws() {
        assertThatThrownBy(() ->
                chatRoomQueryService.findChatRoomById(new FindChatRoomByIdQuery(999_999L)))
                .isInstanceOf(CoreException.class);
    }
}

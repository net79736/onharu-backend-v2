package com.backend.onharu.interfaces.api.controller.impl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.application.ChatFacade;
import com.backend.onharu.domain.chat.dto.ChatRoomCommand.CreateChatRoomCommand;
import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.RoomType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import com.backend.onharu.infra.security.LocalUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ChatController MockMvc 테스트")
class ChatControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatFacade chatFacade;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    private User createUser(String suffix) {
        return userJpaRepository.save(
                User.builder()
                        .loginId("chat-c-" + suffix + "-" + UUID.randomUUID().toString().substring(0, 8))
                        .password("encoded")
                        .name("유저" + suffix)
                        .phone("01011112222")
                        .userType(UserType.CHILD)
                        .statusType(StatusType.ACTIVE)
                        .providerType(ProviderType.LOCAL)
                        .build());
    }

    private void authenticateAs(User user) {
        LocalUser principal = new LocalUser(user, 1L);
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Test
    @DisplayName("POST /api/chats — 일대일 채팅방 생성 200")
    void createChatRoom_success() throws Exception {
        User me = createUser("me");
        User other = createUser("other");
        authenticateAs(me);

        String body = """
                {
                  "name": "새 채팅방",
                  "targetId": %d
                }
                """.formatted(other.getId());

        mockMvc.perform(post("/api/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.chatRoomId").exists());
    }

    @Test
    @DisplayName("GET /api/chats — 내가 참여한 채팅방 목록 조회 200")
    void getChatRoomSummary_empty() throws Exception {
        User me = createUser("list");
        authenticateAs(me);

        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.chatRoomResponses").isArray());
    }

    @Test
    @DisplayName("GET /api/chats/{id}/messages — 빈 방 메시지 목록 조회 200")
    void findChatMessage_empty() throws Exception {
        User me = createUser("msgs");
        User other = createUser("msgsOther");
        authenticateAs(me);
        ChatRoom room = chatFacade.createChatRoom(
                new CreateChatRoomCommand("room", RoomType.ONE_TO_ONE, other.getId(), me.getId()));

        mockMvc.perform(get("/api/chats/{id}/messages", room.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.chatRoomMessageResponses").isArray());
    }

    @Test
    @DisplayName("PATCH /api/chats/{id} — 채팅방 이름 수정 200")
    void updateChatRoom_success() throws Exception {
        User me = createUser("patch");
        User other = createUser("patchOther");
        authenticateAs(me);
        ChatRoom room = chatFacade.createChatRoom(
                new CreateChatRoomCommand("old-name", RoomType.ONE_TO_ONE, other.getId(), me.getId()));

        String body = """
                {
                  "name": "new-name"
                }
                """;

        mockMvc.perform(patch("/api/chats/{id}", room.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("채팅방 수정 완료"));
    }

    @Test
    @DisplayName("POST /api/chats/{id} — 채팅방 입장 (메시지 없으면 조용히 성공)")
    void enterChatRoom_noMessages_success() throws Exception {
        User me = createUser("enter");
        User other = createUser("enterOther");
        authenticateAs(me);
        ChatRoom room = chatFacade.createChatRoom(
                new CreateChatRoomCommand("room", RoomType.ONE_TO_ONE, other.getId(), me.getId()));

        mockMvc.perform(post("/api/chats/{id}", room.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("채팅방 입장 성공"));
    }

    @Test
    @DisplayName("DELETE /api/chats/{id} — 채팅방 탈퇴 200")
    void leaveChatRoom_success() throws Exception {
        User me = createUser("leave");
        User other = createUser("leaveOther");
        authenticateAs(me);
        ChatRoom room = chatFacade.createChatRoom(
                new CreateChatRoomCommand("room", RoomType.ONE_TO_ONE, other.getId(), me.getId()));

        mockMvc.perform(delete("/api/chats/{id}", room.getId()))
                .andExpect(status().isOk());
    }
}

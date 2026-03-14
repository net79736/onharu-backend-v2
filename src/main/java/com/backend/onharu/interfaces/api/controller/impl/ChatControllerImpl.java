package com.backend.onharu.interfaces.api.controller.impl;

import com.backend.onharu.application.ChatFacade;
import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IChatController;
import com.backend.onharu.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.backend.onharu.domain.chat.dto.ChatMessageCommand.ReadMessageCommand;
import static com.backend.onharu.domain.chat.dto.ChatMessageQuery.FindChatMessageQuery;
import static com.backend.onharu.domain.chat.dto.ChatParticipantQuery.GetChatRoomSummaryQuery;
import static com.backend.onharu.domain.chat.dto.ChatRoomCommand.*;
import static com.backend.onharu.interfaces.api.dto.ChatControllerDto.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatControllerImpl implements IChatController {

    private final ChatFacade chatFacade;

    /**
     * 채팅방 생성
     * POST /api/chats
     */
    @PostMapping
    public ResponseEntity<ResponseDTO<CreateChatRoomResponse>> createChatRoom(
            @RequestBody CreateChatRoomRequest request
    ) {
        log.info("채팅방 생성");

        // 채팅방 주인(사용자 ID) 획득
        Long userId = SecurityUtils.getUserId();

        // 채팅방 생성
        ChatRoom chatRoom = chatFacade.createChatRoom(
                new CreateChatRoomCommand(
                        request.name(),
                        userId,
                        request.chatParticipantIds()
                )
        );

        // 응답 생성
        CreateChatRoomResponse response = new CreateChatRoomResponse(chatRoom.getId());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 채팅방 초대
     * POST /api/chats/{chatRoomId}/participants
     */
    @PostMapping("/{chatRoomId}/participants")
    public ResponseEntity<ResponseDTO<String>> inviteChatRoom(
            @PathVariable Long chatRoomId,
            @RequestBody InviteChatRoomRequest request
    ) {
        log.info("채팅방 초대");

        // 채팅방 초대
        chatFacade.inviteChatRoom(
                new InviteChatRoomCommand(chatRoomId, request.userIds())
        );

        String response = "채팅방 초대 성공";

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 채팅방 수정
     * Patch /api/chats/{chatRoomId}
     * @param chatRoomId 채팅방 ID
     * @param request 변경할 채팅방 이름이 포함된 요청
     */
    @PatchMapping("/{chatRoomId}")
    public ResponseEntity<ResponseDTO<String>> updateChatRoom(
            @PathVariable Long chatRoomId,
            @RequestBody UpdateChatRoomRequest request
    ) {
        log.info("채팅방 수정");

        // 채팅방 수정 호출
        chatFacade.updateChatRoom(
                new UpdateChatRoomCommand(chatRoomId, request.name())
        );

        String response = "채팅방 수정 완료";

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 채팅 읽음 처리
     * POST /api/chats/{chatRoomId}/read
     */
    @PostMapping("/{chatRoomId}/read")
    public ResponseEntity<ResponseDTO<String>> readMessage(
            @PathVariable Long chatRoomId,
            @RequestBody ReadMessageRequest request
    ) {
        log.info("채팅방 메시지 읽음 처리");

        // 사용자 ID 추출
        Long userId = SecurityUtils.getUserId();

        chatFacade.readMessage(
                new ReadMessageCommand(
                        chatRoomId,
                        userId,
                        request.messageId()
                )
        );

        String response = "채팅방 메시지 읽음 처리 완료";

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 내가 참여한 채팅방 목록 조회
     * <p>
     * GET /api/chats
     */
    @GetMapping
    public ResponseEntity<ResponseDTO<ChatRoomsResponse>> getChatRoomSummary() {
        log.info("참여 채팅방 목록 조회");

        // 세션에서 사용자 ID 추출
        Long userId = SecurityUtils.getUserId();

        // 내가 참여한 채팅방 목록 조회
        List<ChatRoomResponse> chatRoomSummary = chatFacade.getChatRoomSummary(
                new GetChatRoomSummaryQuery(userId)
        );

        // 응답 생성
        ChatRoomsResponse response = new ChatRoomsResponse(chatRoomSummary);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 특정 채팅방의 채팅 메시지 조회
     * <p>
     * GET /api/chats/{chatRoomId}/message?cursorId={cursorId}
     */
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ResponseDTO<ChatRoomMessagesResponse>> findChatMessage(@PathVariable Long chatRoomId, @RequestParam(required = false) Long cursorId) {
        log.info("채팅방 채팅메시지 조회");

        // Limit 역할을 하는 Pageable 생성
        Pageable pageable = Pageable.ofSize(20);

        // 채팅방의 채팅 메시지 조회
        List<ChatRoomMessageResponse> chatRoomMessageResponses = chatFacade.findChatMessage(
                new FindChatMessageQuery(chatRoomId, cursorId, pageable)
        );

        // 응답 생성
        ChatRoomMessagesResponse response = new ChatRoomMessagesResponse(chatRoomMessageResponses);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 채팅방 탈퇴
     * DELETE /api/chats/{chatRoomId}
     */
    @DeleteMapping("/{chatRoomId}")
    public ResponseEntity<ResponseDTO<String>> leaveChatRoom(
            @PathVariable Long chatRoomId
    ) {
        log.info("채팅방 탈퇴");
        // 세션에 저장된 userId 추출
        Long userId = SecurityUtils.getUserId();

        // 채팅방 탈퇴
        chatFacade.leaveChatRoom(
                new LeaveChatRoomCommand(chatRoomId, userId)
        );

        // 응답 생성
        String response = "채팅방 탈퇴 성공";

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ResponseDTO.success(response));
    }


}

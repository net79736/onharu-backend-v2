package com.backend.onharu.interfaces.api.controller;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.backend.onharu.interfaces.api.dto.ChatControllerDto.*;

@Tag(name = "Chat", description = "채팅 관련 API")
public interface IChatController {

    /**
     * 채팅방 생성
     * POST /api/chats
     */
    @Operation(summary = "채팅방 생성", description = "새로운 일대일 채팅방을 생성합니다.")
    ResponseEntity<ResponseDTO<CreateChatRoomResponse>> createChatRoom(

            @RequestBody(
                    description = "채팅방 생성 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateChatRoomRequest.class),
                            examples = @ExampleObject(
                                    name = "채팅방 생성 요청 예시",
                                    value = """
                                            {
                                              "name": "우리 동네 채팅방",
                                              "roomType": "ONE_TO_ONE",
                                              "targetId": 2
                                            }
                                            """
                            )
                    )
            )
            CreateChatRoomRequest request
    );

    /**
     * 채팅방 초대
     * POST /api/chats/{chatRoomId}/participants
     */
    @Operation(summary = "채팅방 초대", description = "특정 채팅방에 사용자를 초대합니다.")
    ResponseEntity<ResponseDTO<String>> inviteChatRoom(

            @Parameter(description = "채팅방 ID", example = "1", required = true)
            @PathVariable Long chatRoomId,

            @RequestBody(
                    description = "채팅방 초대 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = InviteChatRoomRequest.class),
                            examples = @ExampleObject(
                                    name = "채팅방 초대 요청 예시",
                                    value = """
                                            {
                                              "userIds": [5,6]
                                            }
                                            """
                            )
                    )
            )
            InviteChatRoomRequest request
    );

    /**
     * 채팅방 수정
     * PATCH /api/chats/{chatRoomId}
     */
    @Operation(summary = "채팅방 수정", description = "채팅방 이름을 수정합니다.")
    ResponseEntity<ResponseDTO<String>> updateChatRoom(

            @Parameter(description = "채팅방 ID", example = "1", required = true)
            @PathVariable Long chatRoomId,

            @RequestBody(
                    description = "채팅방 수정 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateChatRoomRequest.class),
                            examples = @ExampleObject(
                                    name = "채팅방 수정 요청 예시",
                                    value = """
                                            {
                                              "name": "새로운 채팅방 이름"
                                            }
                                            """
                            )
                    )
            )
            UpdateChatRoomRequest request
    );

    /**
     * 채팅 메시지 읽음 처리
     * POST /api/chats/{chatRoomId}/read
     */
    @Operation(summary = "채팅 메시지 읽음 처리", description = "특정 메시지까지 읽음 처리합니다.")
    ResponseEntity<ResponseDTO<String>> readMessage(

            @Parameter(description = "채팅방 ID", example = "1", required = true)
            @PathVariable Long chatRoomId,

            @RequestBody(
                    description = "채팅 읽음 처리 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReadMessageRequest.class),
                            examples = @ExampleObject(
                                    name = "채팅 읽음 처리 요청 예시",
                                    value = """
                                            {
                                              "messageId": 1
                                            }
                                            """
                            )
                    )
            )
            ReadMessageRequest request
    );

    /**
     * 내가 참여한 채팅방 목록 조회
     * GET /api/chats
     */
    @Operation(summary = "참여 채팅방 목록 조회", description = "현재 사용자가 참여한 채팅방 목록을 조회합니다.")
    ResponseEntity<ResponseDTO<ChatRoomsResponse>> getChatRoomSummary();

    /**
     * 채팅 메시지 조회 (커서 기반)
     * GET /api/chats/{chatRoomId}/messages
     */
    @Operation(summary = "채팅 메시지 조회", description = "특정 채팅방의 메시지를 커서 기반으로 조회합니다.")
    ResponseEntity<ResponseDTO<ChatRoomMessagesResponse>> findChatMessage(

            @Parameter(description = "채팅방 ID", example = "1", required = true)
            @PathVariable Long chatRoomId,

            @Parameter(description = "커서 ID (이전 메시지 조회용)", example = "200")
            @RequestParam(required = false)
            Long cursorId
    );

    /**
     * 채팅방 탈퇴
     * DELETE /api/chats/{chatRoomId}
     */
    @Operation(summary = "채팅방 탈퇴", description = "채팅방에서 탈퇴합니다.")
    ResponseEntity<ResponseDTO<Void>> leaveChatRoom(

            @Parameter(description = "채팅방 ID", example = "1", required = true)
            @PathVariable Long chatRoomId
    );

    /**
     * 채팅방 입장(메시지 읽음 처리)
     * POST /api/chats/{chatRoomId}
     */
    @Operation(summary = "채팅방 입장", description = "채팅방에 입장할때 메시지를 읽음 처리를 합니다.")
    ResponseEntity<ResponseDTO<String>> enterChatRoom(
            @Parameter(description = "채팅방 ID", example = "1", required = true)
            @PathVariable Long chatRoomId
    );
}

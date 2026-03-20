package com.backend.onharu.application;

import com.backend.onharu.domain.chat.dto.ChatMessageQueryService;
import com.backend.onharu.domain.chat.dto.ChatParticipantQueryService;
import com.backend.onharu.domain.chat.dto.ChatRoomCommand.LeaveChatRoomCommand;
import com.backend.onharu.domain.chat.dto.ChatRoomCommand.UpdateChatRoomCommand;
import com.backend.onharu.domain.chat.model.ChatMessage;
import com.backend.onharu.domain.chat.model.ChatParticipant;
import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.chat.service.ChatMessageCommandService;
import com.backend.onharu.domain.chat.service.ChatParticipantCommandService;
import com.backend.onharu.domain.chat.service.ChatRoomCommandService;
import com.backend.onharu.domain.chat.service.ChatRoomQueryService;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.domain.user.service.UserQueryService;
import com.backend.onharu.infra.db.chat.ChatRoomSummary;
import com.backend.onharu.infra.websocket.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.backend.onharu.domain.chat.dto.ChatMessageCommand.*;
import static com.backend.onharu.domain.chat.dto.ChatMessageQuery.CountUnreadMessageQuery;
import static com.backend.onharu.domain.chat.dto.ChatMessageQuery.FindChatMessageQuery;
import static com.backend.onharu.domain.chat.dto.ChatParticipantCommand.CreateChatParticipantCommand;
import static com.backend.onharu.domain.chat.dto.ChatParticipantCommand.updateChatParticipantCommand;
import static com.backend.onharu.domain.chat.dto.ChatParticipantQuery.*;
import static com.backend.onharu.domain.chat.dto.ChatRoomCommand.CreateChatRoomCommand;
import static com.backend.onharu.domain.chat.dto.ChatRoomCommand.InviteChatRoomCommand;
import static com.backend.onharu.domain.chat.dto.ChatRoomQuery.FindChatRoomByIdQuery;
import static com.backend.onharu.domain.user.dto.UserQuery.GetUserByIdQuery;
import static com.backend.onharu.interfaces.api.dto.ChatControllerDto.ChatRoomMessageResponse;
import static com.backend.onharu.interfaces.api.dto.ChatControllerDto.ChatRoomResponse;

/**
 * 채팅 관련 Facade 입니다.
 */
@Component
@RequiredArgsConstructor
public class ChatFacade {

    private final ChatRoomCommandService chatRoomCommandService;
    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatParticipantCommandService chatParticipantCommandService;
    private final ChatParticipantQueryService chatParticipantQueryService;
    private final ChatMessageCommandService chatMessageCommandService;
    private final ChatMessageQueryService chatMessageQueryService;

    private final UserQueryService userQueryService;

    // 채팅방 생성
    @Transactional
    public ChatRoom createChatRoom(CreateChatRoomCommand command) {
        // 채팅방 생성
        ChatRoom chatRoom = chatRoomCommandService.createChatRoom(command.name());

        // 채팅방 참가 목록 생성
        Set<Long> participantUserIds = new HashSet<>();
        participantUserIds.add(command.userId()); // 채팅방 사용자 ID(방장 ID) 추가

        if (command.participantUserIds() != null) {
            participantUserIds.addAll(command.participantUserIds()); // 방장 외 사용자 ID 목록 추가
        }

        for (Long userId : participantUserIds) {
            // 사용자 조회
            User user = userQueryService.getUser(
                    new GetUserByIdQuery(userId)
            );
            // 채팅참가자 추가
            chatParticipantCommandService.createChatParticipant(
                    new CreateChatParticipantCommand(chatRoom, user)
            );
        }

        // 채팅방 반환
        return chatRoom;
    }

    /**
     * 채팅방 초대
     */
    @Transactional
    public void inviteChatRoom(InviteChatRoomCommand command) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomQueryService.findChatRoomById(
                new FindChatRoomByIdQuery(command.chatRoomId())
        );

        // 사용자 ID 만큼 반복
        for (Long userId : command.userIds()) {
            // 사용자 조회
            User user = userQueryService.getUser(
                    new GetUserByIdQuery(userId)
            );

            // 채팅방 참가 생성
            chatParticipantCommandService.createChatParticipant(
                    new CreateChatParticipantCommand(chatRoom, user)
            );
        }
    }

    /**
     * 채팅방 수정
     */
    public void updateChatRoom(UpdateChatRoomCommand command) {
        // 채팅방 업데이트 서비스 호출
        chatRoomCommandService.updateChatRoomByName(command);
    }


    /**
     * 채팅 메시지 생성
     */
    @Transactional
    public ChatMessageResponse createChatMessage(CreateChatMessageCommand command) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomQueryService.findChatRoomById(
                new FindChatRoomByIdQuery(command.chatRoomId())
        );

        // 사용자 조회
        User sender = userQueryService.getUser(
                new GetUserByIdQuery(command.senderId())
        );

        // 채팅메시지 생성 및 저장
        ChatMessage chatMessage = chatMessageCommandService.createChatMessage(
                new CreateMessageCommand(
                        chatRoom,
                        sender,
                        command.content())
        );

        // 채팅방의 마지막으로 읽은 메시지 업데이트
        chatRoom.updateLastMessage(chatMessage.getId());

        // 채팅 메시지 응답 생성
        return new ChatMessageResponse(
                chatMessage.getId(),
                sender.getId(),
                chatMessage.getContent(),
                chatMessage.getCreatedAt()
        );
    }

    /**
     * 내가 참여한 채팅방 목록 조회
     */
    @Transactional
    public List<ChatRoomResponse> getChatRoomSummary(GetChatRoomSummaryQuery query) {
        // 참여 채팅방 목록 조회
        List<ChatRoomSummary> chatRoomSummaries = chatParticipantQueryService.getChatRoomSummary(query);

        // 채팅방 ID 목록 추출
        List<Long> chatRoomIds = chatRoomSummaries.stream().map(ChatRoomSummary::getId).toList();

        // 채팅 참가자 조회
        List<ChatParticipant> chatParticipants = chatParticipantQueryService.getParticipants(
                new GetChatParticipantsQuery(chatRoomIds)
        );

        // 채팅방을 기준으로 Map 생성
        Map<Long, List<ChatParticipant>> chatRoomMap = chatParticipants.stream()
                .collect(Collectors.groupingBy(chatParticipant -> chatParticipant.getChatRoom().getId()));

        // 각 채팅방 마다 적용
        return chatRoomSummaries.stream()
                .map(chatRoomSummary -> {
                    // 각 채팅방 마다 안 읽은 메세지 갯수 계산
                    long unreadMessage = chatMessageQueryService.countUnreadMessage(
                            new CountUnreadMessageQuery(chatRoomSummary.getId(), chatRoomSummary.getLastReadMessageId())
                    );

                    // 채팅방 참가자 이름 조회
                    List<String> chatParticipantsNames = chatRoomMap.getOrDefault(chatRoomSummary.getId(), List.of()) // 채팅방 조회
                            .stream()
                            .filter(chatParticipant -> !chatParticipant.getUser().getId().equals(query.userId())) // 채팅방 중 사용자 자신을 제외한 참가자 필터링
                            .map(chatParticipant -> chatParticipant.getUser().getName()) // 채팅 참가자의 이름을 추출
                            .toList();

                    // 반환값 생성
                    return new ChatRoomResponse(
                            chatRoomSummary.getId(),
                            chatRoomSummary.getContent(),
                            chatRoomSummary.getCreatedAt(),
                            unreadMessage,
                            chatParticipantsNames
                    );
                })
                .toList();
    }

    /**
     * 특정 채팅방의 채팅 메시지 목록 조회
     */
    public List<ChatRoomMessageResponse> findChatMessage(FindChatMessageQuery query) {
        // 채팅 메시지 목록 조회
        List<ChatMessage> chatMessage = chatMessageQueryService.findChatMessage(query);

        // 각 메시지 마다 적용 후 리턴
        return chatMessage.stream()
                .map(message -> new ChatRoomMessageResponse(
                        message.getId(),
                        message.getSender().getName(),
                        message.getContent(),
                        message.getCreatedAt()
                ))
                .toList();
    }

    /**
     * 채팅방 탈퇴
     */
    @Transactional
    public void leaveChatRoom(LeaveChatRoomCommand command) {
        // 사용자 확인
        User user = userQueryService.getUser(
                new GetUserByIdQuery(command.userId())
        );

        // 계정 검증
        user.verifyStatus();

        // 채팅방 탈퇴
        chatRoomCommandService.leaveChatRoom(command);
    }

    /**
     * 채팅 메시지 읽음 처리
     */
    @Transactional
    public void readMessage(ReadMessageCommand command) {
        // 채팅방 참가자 조회
        ChatParticipant chatParticipant = chatParticipantQueryService.getParticipant(
                new GetChatParticipantQuery(command.chatRoomId(), command.userId())
        );

        // 마지막 메시지 갱신
        chatParticipant.updateLastReadMessageId(command.messageId());

        // DB 에 수정사항 반영
        chatParticipantCommandService.updateChatParticipant(new updateChatParticipantCommand(chatParticipant));
    }


}

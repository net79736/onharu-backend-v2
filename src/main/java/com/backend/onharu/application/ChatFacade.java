package com.backend.onharu.application;

import com.backend.onharu.domain.chat.dto.ChatMessageQueryService;
import com.backend.onharu.domain.chat.dto.ChatParticipantQueryService;
import com.backend.onharu.domain.chat.dto.ChatRoomCommand.EnterChatRoomCommand;
import com.backend.onharu.domain.chat.dto.ChatRoomCommand.LeaveChatRoomCommand;
import com.backend.onharu.domain.chat.dto.ChatRoomCommand.UpdateChatRoomCommand;
import com.backend.onharu.domain.chat.model.ChatMessage;
import com.backend.onharu.domain.chat.model.ChatParticipant;
import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.chat.service.ChatMessageCommandService;
import com.backend.onharu.domain.chat.service.ChatParticipantCommandService;
import com.backend.onharu.domain.chat.service.ChatRoomCommandService;
import com.backend.onharu.domain.chat.service.ChatRoomQueryService;
import com.backend.onharu.domain.child.service.ChildQueryService;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.service.OwnerQueryService;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.service.StoreQueryService;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.domain.user.service.UserQueryService;
import com.backend.onharu.infra.db.chat.ChatRoomSummary;
import com.backend.onharu.infra.websocket.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.backend.onharu.domain.chat.dto.ChatMessageCommand.*;
import static com.backend.onharu.domain.chat.dto.ChatMessageQuery.CountUnreadMessageQuery;
import static com.backend.onharu.domain.chat.dto.ChatMessageQuery.FindChatMessageQuery;
import static com.backend.onharu.domain.chat.dto.ChatParticipantCommand.*;
import static com.backend.onharu.domain.chat.dto.ChatParticipantQuery.*;
import static com.backend.onharu.domain.chat.dto.ChatRoomCommand.CreateChatRoomCommand;
import static com.backend.onharu.domain.chat.dto.ChatRoomCommand.InviteChatRoomCommand;
import static com.backend.onharu.domain.chat.dto.ChatRoomQuery.FindChatRoomByIdQuery;
import static com.backend.onharu.domain.child.dto.ChildQuery.GetChildByUserIdQuery;
import static com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByUserIdQuery;
import static com.backend.onharu.domain.store.dto.StoreQuery.FindByOwnerIdQuery;
import static com.backend.onharu.domain.support.error.ErrorType.Chat.CAN_NOT_CHAT_WITH_ONESELF;
import static com.backend.onharu.domain.support.error.ErrorType.Store.STORE_OWNER_MISMATCH;
import static com.backend.onharu.domain.support.error.ErrorType.User.USER_TYPE_MUST_BE_CHILD_OR_OWNER;
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
    private final StoreQueryService storeQueryService;
    private final ChildQueryService childQueryService;
    private final OwnerQueryService ownerQueryService;

    /**
     * 채팅방 생성(일대일 채팅방, 참여자까지 생성)
     */
    @Transactional
    public ChatRoom createChatRoom(CreateChatRoomCommand command) {
        // 채팅방 대상 사용자가 자기자신과 같을 경우 예외 처리
        if (command.userId().equals(command.targetId())) {
            throw new CoreException(CAN_NOT_CHAT_WITH_ONESELF);
        }

        // 사용자 조회 및 계정 상태 검증
        User requestUser = userQueryService.getUser(
                new GetUserByIdQuery(command.userId())
        );
        requestUser.verifyStatus();

        // 상대방 사용자 조회 및 계정 상태 검증
        User targetUser = userQueryService.getUser(
                new GetUserByIdQuery(command.targetId())
        );
        targetUser.verifyStatus();

        // 새 채팅방 생성
        ChatRoom chatRoom = chatRoomCommandService.createChatRoom(command);

        // 채팅 참여자 생성(사용자)
        chatParticipantCommandService.createChatParticipant(
                new CreateChatParticipantCommand(chatRoom, requestUser)
        );

        // 채팅 참여자 생성(상대방)
        chatParticipantCommandService.createChatParticipant(
                new CreateChatParticipantCommand(chatRoom, targetUser)
        );

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

        // 사용자 읽음 처리
        chatParticipantCommandService.updateLastReadMessage(
                new UpdateLastReadMessageCommand(
                        chatRoom.getId(),
                        sender.getId(),
                        chatMessage.getId()
                )
        );

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
        List<Long> chatRoomIds = chatRoomSummaries.stream()
                .map(ChatRoomSummary::getId)
                .toList();

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
                    // 채팅방 참가자 이름 조회
                    List<String> chatParticipantsNames = chatRoomMap.getOrDefault(chatRoomSummary.getId(), List.of()) // 채팅방 조회
                            .stream()
                            .filter(chatParticipant -> !chatParticipant.getUser().getId().equals(query.userId())) // 채팅방 중 사용자 자신을 제외한 참가자 필터링
                            .map(chatParticipant -> {
                                return displayName(chatParticipant.getUser());
                            }) // 응답값으로 반환할 이름 적용
                            .toList();

                    // 각 채팅방 마다 안 읽은 메세지 갯수 계산
                    long unreadMessage = chatMessageQueryService.countUnreadMessage(
                            new CountUnreadMessageQuery(chatRoomSummary.getId(), chatRoomSummary.getLastReadMessageId())
                    );

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
     * 사용자별 표시할 이름 반환(사업자일 경우 가게명, 아동일 경우 닉네임)
     */
    public String displayName(User user) {
        // 아동일 경우 닉네임 반환
        if (user.getUserType() == UserType.CHILD) {
            return childQueryService.getChildByUserId(
                    new GetChildByUserIdQuery(user.getId())
            ).getNickname();
        }

        // 사업자일 경우 연관된 가게명 반환
        if (user.getUserType() == UserType.OWNER) {
            Owner owner = ownerQueryService.getOwnerByUserId(
                    new GetOwnerByUserIdQuery(user.getId())
            );

            // 사업자의 가게 조회
            List<Store> stores = storeQueryService.findByOwnerId(new FindByOwnerIdQuery(owner.getId()));

            // 사업자의 가게가 없는 경우
            if (stores.isEmpty()) {
                throw new CoreException(STORE_OWNER_MISMATCH);
            }

            // 첫번째 가게 반환=
            return stores.get(0).getName();
        }

        // 다른 타입일 경우 예외발생
        throw new CoreException(USER_TYPE_MUST_BE_CHILD_OR_OWNER);
    }

    /**
     * 특정 채팅방의 채팅 메시지 목록 조회
     */
    public List<ChatRoomMessageResponse> findChatMessage(FindChatMessageQuery query) {
        // 채팅 메시지 목록 조회
        List<ChatMessage> chatMessage = chatMessageQueryService.findChatMessage(query);

        // 마지막으로 읽은 메시지 업데이트
        if (!chatMessage.isEmpty()) {
            Long chatMessageId = chatMessage.get(0).getId();

            chatParticipantCommandService.updateLastReadMessage(
                    new UpdateLastReadMessageCommand(
                            query.chatRoomId(),
                            query.userId(),
                            chatMessageId
                    )
            );
        }

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
        // 채팅방 조회
        chatRoomQueryService.findChatRoomById(
                new FindChatRoomByIdQuery(command.chatRoomId())
        );

        // 채팅방 참가자 조회
        ChatParticipant chatParticipant = chatParticipantQueryService.getParticipant(
                new GetChatParticipantQuery(command.chatRoomId(), command.userId())
        );

        // 채팅참여자 채팅방 탈퇴
        chatParticipant.leaveChatRoom();

        // todo: 제거 스케줄러(채팅방, 메시지, 채팅 참여자) 필요
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

    /**
     * 채팅방 입장
     */
    @Transactional
    public void enterChatRoom(EnterChatRoomCommand command) {
        // 채팅방의 마지막 메시지 조회
        ChatRoom chatRoom = chatRoomQueryService.findChatRoomById(
                new FindChatRoomByIdQuery(command.chatRoomId())
        );
        Long lastMessageId = chatRoom.getLastMessageId();

        // 채팅방에 메시지가 없는 경우
        if (lastMessageId == null) {
            return;
        }

        // 채팅참가자들이 해당 메시지를 읽었음을 업데이트
        chatParticipantCommandService.updateLastReadMessage(
                new UpdateLastReadMessageCommand(
                        command.chatRoomId(),
                        command.userId(),
                        lastMessageId
                )
        );
    }
}

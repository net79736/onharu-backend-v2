package com.backend.onharu.infra.db.chat;

import com.backend.onharu.domain.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 채팅방 JPA 구현체
 */
public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, Long> {
}

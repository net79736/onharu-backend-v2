package com.backend.onharu.infra.db.chat;

import com.backend.onharu.domain.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoom, Long> {
}

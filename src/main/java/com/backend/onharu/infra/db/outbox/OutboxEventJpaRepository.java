package com.backend.onharu.infra.db.outbox;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.onharu.domain.outbox.OutboxEventStatus;
import com.backend.onharu.domain.outbox.model.OutboxEvent;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * 상태가 대기 중인 아웃박스 행을 최대 50개 조회합니다.
     * @param status
     * @return
     */
    List<OutboxEvent> findTop50ByStatusOrderByIdAsc(OutboxEventStatus status);
}
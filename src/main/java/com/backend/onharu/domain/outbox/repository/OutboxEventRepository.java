package com.backend.onharu.domain.outbox.repository;

import java.util.List;
import java.util.Optional;

import com.backend.onharu.domain.outbox.OutboxEventStatus;
import com.backend.onharu.domain.outbox.model.OutboxEvent;

/**
 * 트랜잭션 아웃박스 영속성 포트.
 */
public interface OutboxEventRepository {

    void save(OutboxEvent event);

    /**
     * ID 로 조회합니다. 행이 없으면 구현체에서 예외를 던집니다.
     */
    OutboxEvent getById(Long id);

    Optional<OutboxEvent> findById(Long id);

    List<OutboxEvent> findTop50ByStatusOrderByIdAsc(OutboxEventStatus status);
}

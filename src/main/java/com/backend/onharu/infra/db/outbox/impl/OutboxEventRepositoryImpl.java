package com.backend.onharu.infra.db.outbox.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.outbox.OutboxEventStatus;
import com.backend.onharu.domain.outbox.model.OutboxEvent;
import com.backend.onharu.domain.outbox.repository.OutboxEventRepository;
import com.backend.onharu.infra.db.outbox.OutboxEventJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final OutboxEventJpaRepository outboxEventJpaRepository;

    @Override
    public void save(OutboxEvent event) {
        outboxEventJpaRepository.save(event);
    }

    @Override
    public OutboxEvent getById(Long id) {
        return outboxEventJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("outbox row not found: " + id));
    }

    @Override
    public Optional<OutboxEvent> findById(Long id) {
        return outboxEventJpaRepository.findById(id);
    }

    @Override
    public List<OutboxEvent> findTop50ByStatusOrderByIdAsc(OutboxEventStatus status) {
        return outboxEventJpaRepository.findTop50ByStatusOrderByIdAsc(status);
    }
}
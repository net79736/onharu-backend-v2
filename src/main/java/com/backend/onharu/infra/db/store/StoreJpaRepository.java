package com.backend.onharu.infra.db.store;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.onharu.domain.store.model.Store;

/**
 * 가게 JPA Repository
 */
public interface StoreJpaRepository extends JpaRepository<Store, Long> {
}

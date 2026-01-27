package com.backend.onharu.infra.db.owner;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.onharu.domain.owner.model.Owner;

/**
 * 사업자 JPA Repository
 */
public interface OwnerJpaRepository extends JpaRepository<Owner, Long> {

}

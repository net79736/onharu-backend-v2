package com.backend.onharu.infra.db.child;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.onharu.domain.child.model.Child;

/**
 * 아동 JPA Repository
 */
public interface ChildJpaRepository extends JpaRepository<Child, Long> {

}

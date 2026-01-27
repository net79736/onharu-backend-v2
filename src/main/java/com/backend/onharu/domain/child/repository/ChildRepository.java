package com.backend.onharu.domain.child.repository;

import com.backend.onharu.domain.child.model.Child;

/**
 * 아동 Repository 인터페이스
 */
public interface ChildRepository {

    /**
     * 아동을 저장합니다.
     * 
     * @param child 저장할 아동 엔티티
     * @return 저장된 아동 엔티티
     */
    Child save(Child child);
}

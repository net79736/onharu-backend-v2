package com.backend.onharu.domain.owner.repository;

import com.backend.onharu.domain.owner.model.Owner;

/**
 * 사업자 Repository 인터페이스
 * 
 * 사업자 도메인 모델의 영속성을 관리하는 Repository 인터페이스입니다.
 * 도메인 레이어에 속하며, 실제 구현은 Infrastructure 레이어에서 제공됩니다.
 */
public interface OwnerRepository {

    /**
     * 사업자를 저장합니다.
     * 
     * @param owner 저장할 사업자 엔티티
     * @return 저장된 사업자 엔티티
     */
    Owner save(Owner owner);
}

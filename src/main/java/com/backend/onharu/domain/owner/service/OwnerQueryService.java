package com.backend.onharu.domain.owner.service;

import com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByIdQuery;
import com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByLoginIdQuery;
import com.backend.onharu.domain.owner.dto.OwnerRepositoryParam.GetOwnerByIdParam;
import com.backend.onharu.domain.owner.dto.OwnerRepositoryParam.GetOwnerByLoginIdParam;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByUserIdQuery;
import static com.backend.onharu.domain.owner.dto.OwnerRepositoryParam.GetOwnerByUserIdParam;

/**
 * 사업자 Query Service
 * <p>
 * 사업자 도메인의 상태를 조회하는 비즈니스 로직을 처리하는 서비스입니다.
 * Query 패턴을 사용하여 도메인 모델의 조회 작업을 캡슐화합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerQueryService {

    private final OwnerRepository ownerRepository;

    /**
     * 사업자 ID로 사업자를 조회합니다.
     *
     * @param query 사업자 ID를 포함한 Query
     * @return 조회된 사업자 엔티티
     */
    public Owner getOwnerById(GetOwnerByIdQuery query) {
        return ownerRepository.getOwnerById(new GetOwnerByIdParam(query.id()));
    }

    /**
     * 로그인 ID로 사용자를 조회합니다.
     *
     * @param query 로그인 ID를 포함한 Query
     * @return 조회된 사용자 엔티티
     */
    public Owner getOwnerByLoginId(GetOwnerByLoginIdQuery query) {
        return ownerRepository.getOwnerByLoginId(new GetOwnerByLoginIdParam(query.loginId()));
    }

    /**
     * 사용자 ID 로 사업자를 조회합니다.
     *
     * @param query 사용자 ID를 포함한 Query
     * @return 조회된 사용자 엔티티
     */
    public Owner getOwnerByUserId(GetOwnerByUserIdQuery query) {
        return ownerRepository.getOwnerByUserId(new GetOwnerByUserIdParam(query.userId()));
    }
}

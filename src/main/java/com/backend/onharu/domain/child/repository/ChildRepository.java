package com.backend.onharu.domain.child.repository;

import com.backend.onharu.domain.child.dto.ChildRepositoryParam.GetChildByIdParam;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.support.error.CoreException;

import static com.backend.onharu.domain.child.dto.ChildRepositoryParam.GetChildByUserIdParam;

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

    /**
     * 아동 ID로 아동을 조회합니다.
     *
     * @param param 아동 ID를 포함한 파라미터
     * @return 조회된 아동 엔티티
     * @throws CoreException CHILD_NOT_FOUND 해당 ID 의 아동 엔티티가 없는 경우
     */
    Child getChildById(GetChildByIdParam param);

    /**
     * 사용자 ID 로 아동을 조회합니다.
     *
     * @param param 사용자 ID 를 포함한 파라미터
     * @return 조회한 아동 엔티티
     * @throws CoreException CHILD_NOT_FOUND 해당 ID 의 아동 엔티티가 없는 경우
     */
    Child getChildByUserId(GetChildByUserIdParam param);
}

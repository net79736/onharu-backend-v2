package com.backend.onharu.domain.child.repository;

import com.backend.onharu.domain.child.dto.ChildRepositoryParam.GetChildByIdParam;
import com.backend.onharu.domain.child.dto.ChildRepositoryParam.GetChildByLoginIdParam;
import com.backend.onharu.domain.child.model.Child;

import static com.backend.onharu.domain.child.dto.ChildRepositoryParam.*;

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
     * 아동 ID로 아동 조회합니다.
     * 
     * @param param 아동 ID를 포함한 파라미터
     * @return 조회된 아동 엔티티
     */
    Child getChildById(GetChildByIdParam param);

    /**
     * 로그인 ID로 아동 조회합니다.
     * 
     * @param param 로그인 ID를 포함한 파라미터
     * @return 조회된 아동 엔티티
     */
    Child getChildByLoginId(GetChildByLoginIdParam param);

    /**
     * 아동 ID 와 닉네임을 업데이트 합니다.
     * @param param 아동 ID 와 닉네임을 포함한 파라미터
     */
    void updateChildNicknameById(UpdateChildNicknameByIdParam param);
}

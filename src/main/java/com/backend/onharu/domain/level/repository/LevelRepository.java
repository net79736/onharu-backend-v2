package com.backend.onharu.domain.level.repository;

import com.backend.onharu.domain.level.dto.LevelRepositoryParam.GetLevelByIdParam;
import com.backend.onharu.domain.level.dto.LevelRepositoryParam.GetLevelByNameParam;
import com.backend.onharu.domain.level.model.Level;

import static com.backend.onharu.domain.level.dto.LevelRepositoryParam.UpdateNameByIdParam;

/**
 * 등급 Repository 인터페이스
 * <p>
 * 등급 도메인 모델의 영속성을 관리하는 Repository 구현체 입니다.
 * 등급 레이어에 속하며, 실제 구현은 Infrastructure 레이어에서 제공됩니다.
 */
public interface LevelRepository {

    /**
     * 등급을 저장합니다.
     *
     * @param level 저장할 등급 엔티티
     * @return 저장된 등급 엔티티
     */
    Level save(Level level);

    /**
     * 등급 ID로 등급을 조회합니다.
     *
     * @param param 등급 ID를 포함한 파라미터
     * @return 조회된 등급 엔티티 (Optional)
     */
    Level getLevel(GetLevelByIdParam param);

    /**
     * 이름으로 등급을 조회합니다.
     *
     * @param param 등급명을 포함한 파라미터
     * @return 조회된 등급 엔티티 (Optional)
     */
    Level getLevelByName(GetLevelByNameParam param);

    /**
     * 등급 정보를 업데이트 합니다.
     */
    void updateNameById(UpdateNameByIdParam param);
}

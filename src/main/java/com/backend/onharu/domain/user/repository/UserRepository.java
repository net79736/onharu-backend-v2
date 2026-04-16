package com.backend.onharu.domain.user.repository;

import com.backend.onharu.domain.user.dto.UserRepositoryParam;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.GetUserByIdParam;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.GetUserByLoginIdParam;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.GetUserByNameAndPhoneParam;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.SearchUsersByLoginIdLikeParam;
import com.backend.onharu.domain.user.dto.UserRepositoryParam.UpdateUserByIdAndPasswordParam;
import com.backend.onharu.domain.user.model.User;

import java.util.List;

/**
 * 사용자 Repository 인터페이스
 * <p>
 * 사용자 도메인 모델의 영속성을 관리하는 Repository 인터페이스입니다.
 * 도메인 레이어에 속하며, 실제 구현은 Infrastructure 레이어에서 제공됩니다.
 */
public interface UserRepository {

    /**
     * 사용자를 저장합니다.
     *
     * @param user 저장할 사용자 엔티티
     * @return 저장된 사용자 엔티티
     */
    User save(User user);

    /**
     * 사용자 ID로 사용자를 조회합니다.
     *
     * @param param 사용자 ID를 포함한 파라미터
     * @return 조회된 사용자 엔티티 (Optional)
     */
    User getUser(GetUserByIdParam param);

    /**
     * 로그인 ID로 사용자를 조회합니다.
     *
     * @param param 로그인 ID를 포함한 파라미터
     * @return 조회된 사용자 엔티티 (Optional)
     */
    User getUserByLoginId(GetUserByLoginIdParam param);

    /**
     * 로그인 ID로 사용자 존재 여부를 확인합니다.
     *
     * @param param 로그인 ID를 포함한 파라미터
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByLoginId(GetUserByLoginIdParam param);

    /**
     * 이름과 전화번호로 사용자를 조회합니다.
     *
     * @return 조회된 사용자 엔티티 (Optional)
     */
    User getUserByNameAndPhone(GetUserByNameAndPhoneParam param);

    /**
     * 사용자 ID 와 임시 비밀번호로 사용자 비밀번호를 초기화합니다.
     */
    void updateUserByIdAndPassword(UpdateUserByIdAndPasswordParam param);

    /**
     * 제거된 사용자를 업데이트 합니다. (소프트 삭제)
     *
     * @param param 사용자 ID, 사용자 계정 상태가 포함된 파라미터
     */
    void updateDeletedUser(UserRepositoryParam.UpdateDeletedUserParam param);

    /**
     * 로그인 ID에 키워드가 포함된 활성 사용자를 조회합니다 (본인 제외, 최대 20건).
     */
    List<User> searchUsersByLoginIdLike(SearchUsersByLoginIdLikeParam param);
}

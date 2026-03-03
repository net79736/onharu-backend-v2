package com.backend.onharu.domain.owner.model;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static com.backend.onharu.domain.support.error.ErrorType.Owner.*;

/**
 * 사업자 엔티티
 * <p>
 * 사업자(OWNER) 사용자의 추가 정보를 담는 도메인 모델입니다.
 * USERS 테이블과 1:1 관계를 가지며, 사업자 전용 정보를 저장합니다.
 * <p>
 * 주요 필드:
 * userId: 사용자 ID (FK to USERS)
 * levelId: 등급 ID (FK to LEVELS)
 * businessNumber: 사업자 번호
 * distributionCount: 사업자가 나눔을 완료한 횟수
 */
@Entity
@Table(name = "owners")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Owner extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;

    @JoinColumn(name = "LEVEL_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Level level;

    @Column(name = "BUSINESS_NUMBER", nullable = false, length = 10)
    private String businessNumber;

    @Column(name = "DISTRIBUTION_COUNT", nullable = false)
    private int distributionCount;

    @Builder
    public Owner(User user, Level level, String businessNumber, int distributionCount) {
        this.user = user;
        this.level = level;
        this.businessNumber = businessNumber;
        this.distributionCount = distributionCount;
    }

    /**
     * 사업자 정보를 업데이트합니다.
     *
     * @param businessNumber 변경할 사업자 번호
     */
    public void verifyAndUpdate(String businessNumber) {
        if (businessNumber == null || businessNumber.isBlank()) {
            throw new CoreException(BUSINESS_NUMBER_MUST_NOT_BE_BLANK);
        }

        if (businessNumber.length() != 10) {
            throw new CoreException(BUSINESS_NUMBER_MUST_BE_TEN_DIGITS);
        }

        this.businessNumber = businessNumber;
    }

    /**
     * 사업자에 등급 정보를 변경합니다.
     */
    public void changeLevel(Level level) {
        if (this.level.equals(level)) {
            throw new CoreException(SAME_LEVEL_CAN_NOT_BE_ASSIGNED);
        }

        this.level = level;
    }

    /**
     * 사업자의 나눔 횟수를 증가 시킵니다.
     */
    public void increaseDistribution(int number) {
        this.distributionCount += number;
    }
}

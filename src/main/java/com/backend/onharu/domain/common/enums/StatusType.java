package com.backend.onharu.domain.common.enums;

/**
 * 계정 상태를 정의하는 enum입니다.
 * 
 * 시스템에서 사용할 수 있는 계정 상태를 나타냅니다.
 * 
 * 계정 상태:
 * - PENDING: 대기 상태
 * - ACTIVE: 활성 상태
 * - LOCKED: 잠금 상태
 * - DELETED: 삭제 상태
 * - BLOCKED: 차단 상태
 */
public enum StatusType {
    PENDING,
    ACTIVE,
    LOCKED,
    DELETED,
    BLOCKED
}

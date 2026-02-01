package com.backend.onharu.domain.common.enums;

/**
 * 사용자 유형을 정의하는 enum입니다.
 * 
 * 시스템에서 사용할 수 있는 사용자 유형을 나타냅니다.
 * 
 * 사용자 유형:
 * - CHILD: 결식 아동
 * - OWNER: 사업자
 * - ADMIN: 관리자
 * - NONE: 사용자 유형을 배정받지 못한 사용자
 */
public enum UserType {
    CHILD,
    OWNER,
    ADMIN,
    NONE
}

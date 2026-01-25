package com.backend.onharu.domain.common.base;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 모든 엔티티의 기본 클래스
 * 
 * <p>공통 필드(id, createdAt, updatedAt, createdBy, updatedBy)를 제공합니다.</p>
 * 
 * <h3>자동 설정되는 필드:</h3>
 * <ul>
 *   <li>createdAt: 엔티티 생성 시 자동 설정 (KST 시간)</li>
 *   <li>updatedAt: 엔티티 수정 시 자동 갱신 (KST 시간)</li>
 *   <li>createdBy: 엔티티 생성한 사용자 (현재는 "system")</li>
 *   <li>updatedBy: 엔티티 수정한 사용자 (현재는 "system")</li>
 * </ul>
 * 
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * @Entity
 * public class User extends BaseEntity {
 *     private String email;
 *     // id, createdAt, updatedAt, createdBy, updatedBy는 자동 상속
 * }
 * }</pre>
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(insertable = false, columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(insertable = false)
    private String updatedBy;
}
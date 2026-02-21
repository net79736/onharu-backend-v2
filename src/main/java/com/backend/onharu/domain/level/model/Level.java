package com.backend.onharu.domain.level.model;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.owner.model.Owner;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 등급 엔티티
 *
 * 사업자의 등급을 나타내는 도메인 모델입니다.
 */
@Getter
@Entity
@Table(name = "levels")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Level extends BaseEntity {

    @OneToMany(mappedBy = "level", fetch = FetchType.LAZY)
    private final List<Owner> owners = new ArrayList<>();

    @Column(name = "NAME", nullable = false, length = 30)
    private String name;

    @Builder
    public Level(String name) {
        this.name = name;
    }
}

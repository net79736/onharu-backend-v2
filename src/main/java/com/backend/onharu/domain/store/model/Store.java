package com.backend.onharu.domain.store.model;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.tag.model.Tag;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가게 엔티티
 */
@Entity
@Table(name = "stores")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Store extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_ID", nullable = false)
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private Category category;

    @Column(name = "NAME", nullable = false, length = 30)
    private String name;

    @Column(name = "ADDRESS", nullable = false, length = 255)
    private String address;

    @Column(name = "PHONE", nullable = false, length = 15)
    private String phone;

    @Column(name = "LAT", length = 20)
    private String lat;

    @Column(name = "LNG", length = 20)
    private String lng;

    @Column(name = "IMAGE", nullable = false, length = 255)
    private String image;

    @Column(name = "INTRODUCTION", columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "INTRO", length = 100)
    private String intro;

    @Column(name = "IS_OPEN", nullable = false)
    private Boolean isOpen = true;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessHours> businessHours = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreTag> storeTags = new ArrayList<>();

    @Builder
    public Store(Owner owner, Category category, String name, String address, String phone,
                 String lat, String lng, String image, String introduction, String intro, Boolean isOpen) {
        this.owner = owner;
        this.category = category;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.lat = lat;
        this.lng = lng;
        this.image = image;
        this.introduction = introduction;
        this.intro = intro;
        this.isOpen = isOpen != null ? isOpen : true;
    }

    /**
     * 가게 정보를 업데이트합니다.
     * 
     * @param category 변경할 카테고리
     * @param image 변경할 이미지 경로
     * @param phone 변경할 전화번호
     * @param address 변경할 주소
     * @param lat 변경할 위도
     * @param lng 변경할 경도
     * @param introduction 변경할 가게 소개
     * @param intro 변경할 한줄 소개
     * @param isOpen 변경할 영업 여부
     */
    public void update(Category category, String image, String phone, String address, String lat, String lng, String introduction, String intro, Boolean isOpen) {
        ofNullable(category).ifPresent(v -> this.category = v);
        ofNullable(image).ifPresent(v -> this.image = v);
        ofNullable(phone).ifPresent(v -> this.phone = v);
        ofNullable(address).ifPresent(v -> this.address = v);
        ofNullable(lat).ifPresent(v -> this.lat = v);
        ofNullable(lng).ifPresent(v -> this.lng = v);
        ofNullable(introduction).ifPresent(v -> this.introduction = v);
        ofNullable(intro).ifPresent(v -> this.intro = v);
        ofNullable(isOpen).ifPresent(v -> this.isOpen = v);
    }

    /**
     * 가게 카테고리를 수정합니다.
     * 
     * @param category 변경할 카테고리
     */
    public void updateCategory(Category category) {
        this.category = category; // 카테고리 수정
    }

    /**
     * 사업자가 가게의 주인인지 여부를 반환합니다.
     * 
     * @param owner 사업자
     * @return 사업자가 가게의 주인인지 여부
     */
    public void BelongsTo(Owner owner) {
        if (!this.owner.getId().equals(owner.getId())) {
            throw new CoreException(ErrorType.Store.STORE_OWNER_MISMATCH);
        }
    }

    /**
     * 카테고리 ID를 반환합니다. (기존 코드 호환성을 위해)
     * 
     * @return 카테고리 ID
     */
    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }

    /**
     * 가게 영업 상태를 변경합니다.
     * 
     * @param isOpen 영업 여부
     */
    public void changeOpenStatus(Boolean isOpen) {
        this.isOpen = isOpen;
    }

    /**
     * 영업시간을 추가합니다.
     * 
     * @param businessHours 추가할 영업시간
     */
    public void addBusinessHours(BusinessHours businessHours) {
        this.businessHours.add(businessHours);
        businessHours.setStore(this);
    }

    /**
     * 영업시간을 제거합니다.
     * 
     * @param businessHours 제거할 영업시간
     */
    public void removeBusinessHours(BusinessHours businessHours) {
        this.businessHours.remove(businessHours);
        businessHours.setStore(null);
    }

    /**
     * 태그를 추가합니다.
     * 
     * @param tag 추가할 태그
     */
    public void addTag(Tag tag) {
        StoreTag storeTag = StoreTag.builder()
                .store(this)
                .tag(tag)
                .build();
        this.storeTags.add(storeTag);
    }

    /**
     * 태그를 제거합니다.
     * 
     * @param tag 제거할 태그
     */
    public void removeTag(Tag tag) {
        this.storeTags.removeIf(storeTag -> storeTag.getTag().equals(tag));
    }
}

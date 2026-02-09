package com.backend.onharu.application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.common.enums.WeekType;
import com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByIdQuery;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.service.OwnerQueryService;
import com.backend.onharu.domain.store.dto.CategoryQuery.GetCategoryByIdQuery;
import com.backend.onharu.domain.store.dto.StoreCommand.ChangeOpenStatusCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.CreateStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.DeleteStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.UpdateStoreCommand;
import com.backend.onharu.domain.store.dto.StoreQuery.FindByOwnerIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreByIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.SearchStoresQuery;
import com.backend.onharu.domain.store.model.BusinessHours;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.repository.CategoryRepository;
import com.backend.onharu.domain.store.service.StoreCommandService;
import com.backend.onharu.domain.store.service.StoreQueryService;
import com.backend.onharu.domain.tag.dto.TagCommand.CreateTagCommand;
import com.backend.onharu.domain.tag.dto.TagQuery.FindByNameQuery;
import com.backend.onharu.domain.tag.model.Tag;
import com.backend.onharu.domain.tag.service.TagCommandService;
import com.backend.onharu.domain.tag.service.TagQueryService;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.BusinessHourRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoreFacade {

    private final StoreQueryService storeQueryService;
    private final StoreCommandService storeCommandService;
    private final OwnerQueryService ownerQueryService;
    private final CategoryRepository categoryRepository;
    private final TagQueryService tagQueryService;
    private final TagCommandService tagCommandService;
    private final FileFacade fileFacade;
    
    /**
     * 가게 단건 조회
     * 
     * @param storeId 가게 ID
     * @return 조회된 가게 엔티티
     */
    public Store getStore(Long storeId) {
        return storeQueryService.getStore(new GetStoreByIdQuery(storeId));
    }

    /**
     * 가게 목록 조회
     * 
     * @param ownerId 사업자 ID
     * @return 조회된 가게 목록
     */
    public List<Store> getStores(Long ownerId) {
        return storeQueryService.findByOwnerId(new FindByOwnerIdQuery(ownerId));
    }

    /**
     * 가게 목록 조회 (위치 기반 검색)
     * @param searchStoresQuery 검색 쿼리
     * @param pageable 페이징 정보
     * @return 가게 목록
     */
    public Page<Store> searchStores(
            SearchStoresQuery searchStoresQuery, 
            Pageable pageable) {

        return storeQueryService.findByLocation(searchStoresQuery, pageable);
    }

    /**
     * 가게 등록
     * 
     * @param command 가게 생성 커맨드
     * @param ownerId 사업자 ID
     * @return 생성된 가게 엔티티
     */
    @Transactional
    public Store createStore(CreateStoreCommand command, Long ownerId) {
        // 사업자 정보 조회
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(ownerId));
        
        // 카테고리 정보 조회
        Category category = categoryRepository.getCategoryById(new GetCategoryByIdQuery(command.categoryId()));
        
        // 가게 생성
        Store store = storeCommandService.createStore(command, owner, category);

        // 태그 생성 및 추가
        if (command.tagNames() != null && !command.tagNames().isEmpty()) {
            updateTags(store, command.tagNames());
        }

        // 영업시간 업데이트
        if (command.businessHours() != null && !command.businessHours().isEmpty()) {
            updateBusinessHours(store, command.businessHours());
        }

        // 가게 저장
        store = storeCommandService.save(store);

        // 이미 업로드된 이미지 메타데이터 일괄 등록 (이미지 먼저 → 상점 저장 시 한 번에 연결)
        fileFacade.registerFiles(AttachmentType.STORE, store.getId(), command.images());

        return store;
    }

    /**
     * 태그 생성 및 추가
     * 
     * @param tagName 태그 이름
     * @return
     */
    private Tag getOrCreateTag(String tagName) {
        return tagQueryService.findByName(new FindByNameQuery(tagName))
            .stream()
            .findFirst()
            .orElseGet(() -> tagCommandService.createTag(new CreateTagCommand(tagName)));
    }

    /**
     * 가게 정보 수정
     * 
     * @param command 가게 수정 커맨드
     * @param ownerId 사업자 ID
     */
    @Transactional
    public void updateStore(UpdateStoreCommand command, Long ownerId) {
        // 가게 정보 조회
        Store store = storeQueryService.getStore(new GetStoreByIdQuery(command.id()));
        
        // 사업자 정보 조회
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(ownerId));
        
        // 사업자가 가게의 주인인지 확인
        store.BelongsTo(owner);
        
        // 카테고리 정보 조회
        Category category = categoryRepository.getCategoryById(new GetCategoryByIdQuery(command.categoryId()));
        
        // 가게 정보 수정
        storeCommandService.updateStore(command, category);
        
        // 태그 업데이트
        if (command.tagNames() != null && !command.tagNames().isEmpty()) {
            updateTags(store, command.tagNames());
        }
        
        // 영업시간 업데이트
        if (command.businessHours() != null && !command.businessHours().isEmpty()) {
            updateBusinessHours(store, command.businessHours());
        }

        // 이미지 목록 교체 (images가 넘어오면 기존 첨부 삭제 후 새 목록으로 등록)
        if (command.images() != null) {
            fileFacade.replaceFiles(AttachmentType.STORE, store.getId(), command.images());
        }
    }
    
    /**
     * 가게 태그 업데이트
     * 기존 태그를 모두 제거하고 새로운 태그를 추가합니다.
     * 
     * @param store 가게 엔티티
     * @param tagNames 새로운 태그 이름 목록
     */
    private void updateTags(Store store, List<String> tagNames) {
        // 기존 태그 모두 제거
        List<Tag> existingTags = store.getStoreTags().stream()
                .map(storeTag -> storeTag.getTag())
                .collect(Collectors.toList());
        existingTags.forEach(store::removeTag);
        
        // 새로운 태그 추가
        tagNames.stream()
                .map(this::getOrCreateTag)
                .forEach(store::addTag);
    }
    
    /**
     * 가게 영업시간 업데이트
     * 기존 영업시간을 모두 제거하고 새로운 영업시간을 추가합니다.
     * 
     * @param store 가게 엔티티
     * @param businessHourRequests 새로운 영업시간 요청 목록
     */
    private void updateBusinessHours(Store store, List<BusinessHourRequest> businessHourRequests) {
        // 기존 영업시간 모두 제거
        List<BusinessHours> existingBusinessHours = new ArrayList<>(store.getBusinessHours());
        existingBusinessHours.forEach(store::removeBusinessHours);
        
        // 새로운 영업시간 추가
        businessHourRequests.stream()
                .map(request -> BusinessHours.builder()
                        .store(store)
                        .businessDay(WeekType.fromLocalDate(request.businessDay()))
                        .openTime(request.openTime())
                        .closeTime(request.closeTime())
                        .build())
                .forEach(store::addBusinessHours);
    }

    /**
     * 가게 카테고리 수정
     * 
     * @param storeId 가게 ID
     * @param categoryId 카테고리 ID
     * @param ownerId 사업자 ID
     */
    public void updateCategory(Long storeId, Long categoryId, Long ownerId) {
        // 가게 정보 조회
        Store store = storeQueryService.getStore(new GetStoreByIdQuery(storeId));
        
        // 사업자 정보 조회
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(ownerId));
        
        // 사업자가 가게의 주인인지 확인
        store.BelongsTo(owner);
        
        // 카테고리 정보 조회
        Category category = categoryRepository.getCategoryById(new GetCategoryByIdQuery(categoryId));
        
        // 가게 카테고리 수정
        store.updateCategory(category);
    }

    /**
     * 가게 삭제
     * 
     * @param storeId 가게 ID
     * @param ownerId 사업자 ID
     */
    public void deleteStore(DeleteStoreCommand command, Long ownerId) {
        // 가게 정보 조회
        Store store = storeQueryService.getStore(new GetStoreByIdQuery(command.id()));
        
        // 사업자 정보 조회
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(ownerId));
        
        // 사업자가 가게의 주인인지 확인
        store.BelongsTo(owner);
        
        // 가게 삭제
        storeCommandService.deleteStore(command);
    }

    /**
     * 가게 영업 상태 변경
     * 
     * @param storeId 가게 ID
     * @param isOpen 영업 여부
     * @param ownerId 사업자 ID
     */
    public void changeOpenStatus(Long storeId, Boolean isOpen, Long ownerId) {
        // 가게 정보 조회
        Store store = storeQueryService.getStore(new GetStoreByIdQuery(storeId));
        
        // 사업자 정보 조회
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(ownerId));
        
        // 사업자가 가게의 주인인지 확인
        store.BelongsTo(owner);
        
        // 영업 상태 변경
        storeCommandService.changeOpenStatus(new ChangeOpenStatusCommand(storeId, isOpen));
    }
}

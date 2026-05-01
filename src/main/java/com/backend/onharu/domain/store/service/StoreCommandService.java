package com.backend.onharu.domain.store.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.store.dto.StoreCommand.ChangeOpenStatusCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.CreateStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.DeleteStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.UpdateStoreCommand;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.GetStoreByIdParam;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.model.StoreCount;
import com.backend.onharu.domain.store.repository.StoreRepository;
import com.backend.onharu.infra.db.store.StoreCountJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreCommandService {
    private final StoreRepository storeRepository;
    private final StoreJpaRepository storeJpaRepository;
    private final StoreCountJpaRepository storeCountJpaRepository;

    /**
     * 가게 생성
     * 
     * 주의: Owner와 Category 엔티티는 별도로 조회해서 전달해야 합니다.
     */
    public Store createStore(CreateStoreCommand command, Owner owner, Category category) {
        Store store = Store.builder()
                .owner(owner)
                .category(category)
                .name(command.name())
                .address(command.address())
                .phone(command.phone())
                .lat(command.lat())
                .lng(command.lng())
                .introduction(command.introduction())
                .intro(command.intro())
                .isOpen(true) // 가게 생성 시 영업 상태는 영업 상태로 설정
                .isSharing(false) // 가게 생성 시 공유중 상태는 비공유 상태로 설정
                .build();

        // IDENTITY 전략은 flush 시점에 ID가 할당됩니다. (PK=FK StoreCount 저장을 위해 saveAndFlush로 강제)
        Store saved = storeJpaRepository.saveAndFlush(store);
        storeCountJpaRepository.save(StoreCount.create(saved));
        return saved;
    }

    /**
     * 가게 정보 수정
     */
    public void updateStore(UpdateStoreCommand command, Category category) {
        Store store = storeRepository.getStoreById(new GetStoreByIdParam(command.id()));
        
        store.update(
                category,
                command.phone(),
                command.address(),
                command.lat(),
                command.lng(),
                command.introduction(),
                command.intro(),
                command.isOpen(),
                command.isSharing()
        );
    }

    /**
     * 가게 저장
     */
    public Store save(Store store) {
        return storeRepository.save(store);
    }
    
    /**
     * 가게 삭제
     */
    public void deleteStore(DeleteStoreCommand command) {
        Store store = storeRepository.getStoreById(new GetStoreByIdParam(command.id()));
        // StoreCount가 Store를 FK로 참조하므로, Store 삭제 전에 StoreCount를 먼저 삭제합니다.
        // (테스트/H2 환경에서 FK 무결성 위반 방지)
        storeCountJpaRepository.findById(store.getId()).ifPresent(storeCountJpaRepository::delete);
        storeRepository.delete(store);
    }

    /**
     * 가게 영업 상태 변경
     */
    public void changeOpenStatus(ChangeOpenStatusCommand command) {
        Store store = storeRepository.getStoreById(new GetStoreByIdParam(command.id()));
        
        store.changeOpenStatus(command.isOpen());
    }
}

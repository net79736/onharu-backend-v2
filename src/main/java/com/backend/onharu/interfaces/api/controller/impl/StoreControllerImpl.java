package com.backend.onharu.interfaces.api.controller.impl;

import static com.backend.onharu.interfaces.api.common.util.PageableUtil.getCurrentPage;
import static com.backend.onharu.interfaces.api.dto.StoreRequestMapperDto.toImageMetadataList;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.onharu.application.StoreFacade;
import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.file.dto.FileQuery.ListByRefQuery;
import com.backend.onharu.domain.file.dto.FileQuery.ListByRefsQuery;
import com.backend.onharu.domain.file.model.File;
import com.backend.onharu.domain.file.service.FileQueryService;
import com.backend.onharu.domain.store.dto.CategoryQuery.FindAllByNameQuery;
import com.backend.onharu.domain.store.dto.StoreCommand.CreateStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.DeleteStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.UpdateStoreCommand;
import com.backend.onharu.domain.store.dto.StoreQuery.SearchStoresQuery;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.repository.CategoryRepository;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.common.util.PageableUtil;
import com.backend.onharu.interfaces.api.controller.IStoreController;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.CategoryResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.GetStoreDetailResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.OpenStoreRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.OpenStoreResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.SearchStoresRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.SearchStoresResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.StoreResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.UpdateStoreRequest;
import com.backend.onharu.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 가게 관련 API를 제공하는 컨트롤러 구현체입니다.
 * 
 * 가게의 상세 정보 조회, 목록 조회, 생성, 수정, 삭제 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreControllerImpl implements IStoreController {

    private final CategoryRepository categoryRepository;
    private final StoreFacade storeFacade;
    private final FileQueryService fileQueryService;

    /**
     * 가게 상세 정보 조회
     * 
     * GET /api/stores/{storeId}
     * 특정 가게의 상세 정보를 반환합니다.
     *
     * @param storeId 가게 ID
     * @return 가게 상세 정보
     */
    @Override
    @GetMapping("/{storeId}")
    public ResponseEntity<ResponseDTO<GetStoreDetailResponse>> getStore(
            @PathVariable("storeId") Long storeId
    ) {
        log.info("가게 상세 정보 조회 요청: storeId={}", storeId);
        
        Store store = storeFacade.getStore(storeId);

        // 가게에 첨부된 이미지 목록 조회
        List<File> files = fileQueryService.listByRef(
                new ListByRefQuery(AttachmentType.STORE, store.getId())
        );
        List<String> imagePaths = files.stream()
                .map(File::getFilePath)
                .toList();

        GetStoreDetailResponse response = new GetStoreDetailResponse(store, imagePaths);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 가게 목록 조회
     * 
     * GET /api/stores
     * 검색 및 페이징이 가능한 가게 목록을 반환합니다.
     *
     * @param request 가게 목록 조회 요청 (위도, 경도, 반경)
     * @return 가게 목록
     */
    @Override
    @GetMapping
    public ResponseEntity<ResponseDTO<SearchStoresResponse>> searchStores(
            @ModelAttribute SearchStoresRequest request
    ) {
        log.info("가게 목록 조회 요청: latitude={}, longitude={}, radius={}, pageNum={}, perPage={}, sortField={}, sortDirection={}", 
                request.latitude(), request.longitude(), request.radius(), 
                request.pageNum(), request.perPage(), request.sortField(), request.sortDirection());
        
        // Pageable 생성 (유틸리티 클래스 사용 - 1-based 페이지 번호 지원)
        Pageable pageable = PageableUtil.ofOneBased(
                request.pageNum(), 
                request.perPage(), 
                request.sortField(), 
                request.sortDirection()
        );
        
        // 검색 쿼리 생성
        SearchStoresQuery searchQuery = new SearchStoresQuery(
                request.latitude(), 
                request.longitude(), 
                request.radius()
        );
        
        // 페이징된 결과 조회
        Page<Store> storePage = storeFacade.searchStores(searchQuery, pageable);
        
        // 가게 ID 목록 추출
        List<Long> storeIds = storePage.getContent().stream()
                .map(Store::getId)
                .collect(Collectors.toList());
        
        // 배치로 이미지 파일 목록 조회 (N+1 문제 방지)
        List<File> allFiles = storeIds.isEmpty() 
                ? List.of() 
                : fileQueryService.listByRefs(new ListByRefsQuery(AttachmentType.STORE, storeIds));
        
        // 가게 ID별로 이미지 목록 그룹화
        Map<Long, List<String>> imagesByStoreId = allFiles.stream()
                .collect(Collectors.groupingBy(
                        File::getRefId,
                        Collectors.mapping(File::getFilePath, Collectors.toList())
                ));
        
        // DTO 변환 (이미지 목록 포함)
        List<StoreResponse> storeResponses = storePage.getContent().stream()
                .map(store -> {
                    List<String> images = imagesByStoreId.getOrDefault(store.getId(), List.of());
                    return new StoreResponse(store, images);
                })
                .collect(Collectors.toList());
        
        SearchStoresResponse response = new SearchStoresResponse(
                storeResponses,
                storePage.getTotalElements(),
                getCurrentPage(storePage), // 0-based → 1-based 변환
                storePage.getTotalPages(),
                storePage.getSize()
        );
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 가게 정보 작성
     * 
     * POST /api/stores
     * 신규 가게 정보를 생성합니다.
     *
     * @param request 가게 정보 생성 요청
     * @return 생성된 가게 정보
     */
    @Override
    @PostMapping
    public ResponseEntity<ResponseDTO<OpenStoreResponse>> openStore(
            @RequestBody OpenStoreRequest request
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        log.info("가게 정보 작성 요청: ownerId={}, request={}", ownerId, request);

        Store store = storeFacade.createStore(new CreateStoreCommand(
            ownerId,
            request.categoryId(),
            request.name(),
            request.address(),
            request.phone(),
            request.lat(),
            request.lng(), 
            request.intro(),
            request.introduction(),
            request.tagNames(),
            request.businessHours(),
            toImageMetadataList(request.images())
        ), ownerId);

        OpenStoreResponse response = new OpenStoreResponse(store.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(response));
    }

    /**
     * 가게 정보 삭제
     * 
     * DELETE /api/stores/{storeId}
     * 특정 가게 정보를 삭제합니다.
     *
     * @param storeId 가게 ID
     * @return 삭제 결과
     */
    @Override
    @DeleteMapping("/{storeId}")
    public ResponseEntity<ResponseDTO<Void>> closeStore(
            @PathVariable("storeId") Long storeId
    ) {        
        Long ownerId = SecurityUtils.getCurrentUserId();
        log.info("가게 정보 삭제 요청: ownerId={}, storeId={}", ownerId, storeId);

        storeFacade.deleteStore(new DeleteStoreCommand(storeId), ownerId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 가게 정보 수정
     * 
     * PUT /api/stores/{storeId}
     * 특정 가게 정보를 수정합니다.
     *
     * @param storeId 가게 ID
     * @param request 가게 정보 수정 요청
     * @return 수정 결과
     */
    @Override
    @PutMapping("/{storeId}")
    public ResponseEntity<ResponseDTO<Void>> updateMyStore(
            @PathVariable("storeId") Long storeId,
            @RequestBody UpdateStoreRequest request
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        log.info("가게 정보 수정 요청: ownerId={}, storeId={}, request={}", ownerId, storeId, request);

        storeFacade.updateStore(new UpdateStoreCommand(
            storeId, 
            request.categoryId(), 
            request.phone(),
            request.address(),
            request.lat(),
            request.lng(),
            request.intro(),
            request.introduction(),
            request.isOpen(),
            request.tagNames(),
            request.businessHours(),
            toImageMetadataList(request.images())
        ), ownerId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 가게 카테고리 정보 목록 반환
     */
    @Override
    @GetMapping("/categories")
    public ResponseEntity<ResponseDTO<List<CategoryResponse>>> getCategoryList() {
        log.info("가게 카테고리 정보 목록 조회 요청");

        List<Category> categories = categoryRepository.findAllByName(new FindAllByNameQuery(null));
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(categories.stream()
                        .map(CategoryResponse::new)
                        .collect(Collectors.toList())));
    }
}

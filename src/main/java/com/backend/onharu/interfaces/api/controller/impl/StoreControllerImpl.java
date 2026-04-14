package com.backend.onharu.interfaces.api.controller.impl;

import static com.backend.onharu.interfaces.api.common.util.PageableUtil.getCurrentPage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.onharu.application.StoreExcelFacade;
import com.backend.onharu.application.StoreFacade;
import com.backend.onharu.application.StoreScheduleFacade;
import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.file.dto.FileQuery.ListByRefQuery;
import com.backend.onharu.domain.file.dto.FileQuery.ListByRefsQuery;
import com.backend.onharu.domain.file.model.File;
import com.backend.onharu.domain.file.service.FileQueryService;
import com.backend.onharu.domain.store.dto.CategoryQuery.FindAllByNameQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.SearchStoresQuery;
import com.backend.onharu.domain.store.dto.StoreWithFavoriteCount;
import com.backend.onharu.domain.store.model.BusinessHours;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.repository.BusinessHoursRepository;
import com.backend.onharu.domain.store.repository.CategoryRepository;
import com.backend.onharu.domain.store.support.StoreOpenStatusCalculator;
import com.backend.onharu.domain.store.support.StoreSearchSortResolver;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.common.util.PageableUtil;
import com.backend.onharu.interfaces.api.controller.IStoreController;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.CategoryResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.GetStoreDetailByIdRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.GetStoreDetailResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.SearchStoresRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.SearchStoresResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.StoreResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.UploadStoresByExcelResponse;
import com.backend.onharu.interfaces.api.dto.StoreScheduleControllerDto.DateSummary;
import com.backend.onharu.interfaces.api.dto.StoreScheduleControllerDto.GetStoreSchedulesRequest;
import com.backend.onharu.interfaces.api.dto.StoreScheduleControllerDto.GetStoreSchedulesResponse;
import com.backend.onharu.interfaces.api.dto.StoreScheduleControllerDto.ScheduleSlot;
import com.backend.onharu.utils.NumberUtils;
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
    private final StoreExcelFacade storeExcelFacade;
    private final StoreScheduleFacade storeScheduleFacade;
    private final FileQueryService fileQueryService;
    private final BusinessHoursRepository businessHoursRepository;

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
            @PathVariable("storeId") Long storeId,
            @ModelAttribute GetStoreDetailByIdRequest request
    ) {
        log.info("가게 상세 정보 조회 요청: storeId={}", storeId);
        
        StoreWithFavoriteCount storePage = storeFacade.getStore(new GetStoreQuery(storeId, request.lat(), request.lng()));

        // 가게에 첨부된 이미지 목록 조회
        List<File> files = fileQueryService.listByRef(
                new ListByRefQuery(AttachmentType.STORE, storePage.store().getId())
        );

        // 이미지 목록 추출
        List<String> imagePaths = files.stream()
                .map(File::getFilePath)
                .toList();
        
        // 예약 가능한 가게 ID 집합 조회
        boolean hasValidSchedule = !storeScheduleFacade
                .filterReservableStoreIds(Set.of(storePage.store().getId()), LocalDate.now())
                .isEmpty();
        // 공유중
        boolean effectiveIsSharing = Boolean.TRUE.equals(storePage.store().getIsSharing()) || hasValidSchedule;

        // 영업중 여부 계산
        boolean isOpenNow = StoreOpenStatusCalculator.isOpenNow(storePage.store(), LocalDateTime.now());

        GetStoreDetailResponse response = new GetStoreDetailResponse(
            storePage.store(),
            isOpenNow,
            effectiveIsSharing,
            NumberUtils.truncateToIntegerAsDouble(storePage.distance()),
            imagePaths,
            storePage.favoriteCount()
        );

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
        log.info("가게 목록 조회 요청: lat={}, lng={}, categoryId={}, keyword={}, pageNum={}, perPage={}, sortField={}, sortDirection={}",
            request.lat(), request.lng(), request.categoryId(), request.keyword(),
            request.pageNum(), request.perPage(),
            request.sortField(), request.sortDirection()
        );

        // 위/경도 유무에 따라 쿼리 타입이 달라짐 → Pageable 생성 방식 분기
        String sortField = StoreSearchSortResolver.resolve(request.sortField(), request.hasLocation());
        
        Pageable pageable = PageableUtil.ofOneBased(
            request.pageNum(),
            request.perPage(),
            sortField,
            request.sortDirection()
        );
        
        // 검색 쿼리 생성
        SearchStoresQuery searchStoreQuery = new SearchStoresQuery(
                request.lat(), 
                request.lng(),
                request.categoryId(),
                request.keyword()
        );
        
        // 페이징된 결과 조회
        Page<StoreWithFavoriteCount> storePage = storeFacade.searchStores(searchStoreQuery, pageable);
        
        // 가게 ID 목록 추출
        List<Long> storeIds = storePage.getContent().stream()
                .map(StoreWithFavoriteCount::store)
                .map(Store::getId)
                .collect(Collectors.toList());

        // 예약 가능한 가게 ID 집합 조회
        Set<Long> validScheduleStoreIds = storeIds.isEmpty()
                ? Set.of()
                : storeScheduleFacade.filterReservableStoreIds(new HashSet<>(storeIds), LocalDate.now());
        
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

        // 배치로 영업시간 조회 (N+1 방지) → storeId별로 그룹화
        Map<Long, List<BusinessHours>> businessHoursByStoreId = businessHoursRepository.findAllByStoreIds(storeIds).stream()
                .collect(Collectors.groupingBy(bh -> bh.getStore().getId()));
        
        // DTO 변환 (이미지 목록 포함)
        LocalDateTime now = LocalDateTime.now();
        List<StoreResponse> storeResponses = storePage.getContent().stream()
                .map(storePageObject -> {
                    // 이미지 목록 추출
                    List<String> images = imagesByStoreId.getOrDefault(storePageObject.store().getId(), List.of());
                    double distanceKm = NumberUtils.truncateToIntegerAsDouble(storePageObject.distance());
                    // 공유중
                    boolean effectiveIsSharing = Boolean.TRUE.equals(storePageObject.store().getIsSharing())
                            || validScheduleStoreIds.contains(storePageObject.store().getId());
                    // 영업중 여부 계산
                    boolean isOpenNow = StoreOpenStatusCalculator.isOpenNow(
                            storePageObject.store().getIsOpen(),
                            businessHoursByStoreId.getOrDefault(storePageObject.store().getId(), List.of()),
                            now
                    );
                    // DTO 변환
                    return new StoreResponse(
                            storePageObject.store(),
                            isOpenNow,
                            effectiveIsSharing,
                            distanceKm,
                            images,
                            storePageObject.favoriteCount()
                    );
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
     * 가게 스케줄 조회
     *
     * GET /api/stores/{storeId}/schedules
     * - year, month 만 전달: 해당 월의 날짜별 예약 가능 슬롯 수 반환
     * - year, month, day 모두 전달: 해당 날짜의 시간대별 스케줄 상세 반환
     */
    @Override
    @GetMapping("/{storeId}/schedules")
    public ResponseEntity<ResponseDTO<GetStoreSchedulesResponse>> getStoreSchedules(
            @PathVariable("storeId") Long storeId,
            @ModelAttribute GetStoreSchedulesRequest request
    ) {
        log.info("가게 스케줄 조회 요청: storeId={}, year={}, month={}, day={}", storeId, request.year(), request.month(), request.day());

        if (request.day() == null) {
            List<DateSummary> summaries = storeScheduleFacade.getMonthlySchedules(
                    storeId, request.year(), request.month());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(ResponseDTO.success(GetStoreSchedulesResponse.ofDateSummaries(summaries)));
        }

        LocalDate scheduleDate = LocalDate.of(request.year(), request.month(), request.day());
        List<ScheduleSlot> slots = storeScheduleFacade.getDailyScheduleDetails(storeId, scheduleDate);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(GetStoreSchedulesResponse.ofScheduleSlots(slots)));
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

    /**
     * 엑셀 파일을 통해 가게 정보를 일괄 등록합니다.
     *
     * POST /api/stores/upload-excel
     * Content-Type: multipart/form-data
     * - file: 엑셀 파일 (첫 번째 행은 헤더, 두 번째 행부터 데이터)
     *
     * 엑셀 컬럼 포맷(0-based index):
     * 0: 카테고리 ID (Long)
     * 1: 가게 이름 (String)
     * 2: 주소 (String)
     * 3: 전화번호 (String)
     * 4: 위도 (String)
     * 5: 경도 (String)
     * 6: 가게 소개 (String)
     * 7: 한줄 소개 (String)
     * 8: 태그 목록 (쉼표로 구분된 문자열, 예: "커피, 디저트, 브런치")
     *
     * 업로드한 가게의 사업자(owner)는 현재 로그인한 사용자로 설정됩니다.
    */
    @Override
    @PostMapping(
        value = "/upload-excel",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ResponseDTO<UploadStoresByExcelResponse>> uploadStoresByExcel(
        @RequestPart("file") MultipartFile file
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId() != null ? SecurityUtils.getCurrentUserId() : 33L;

        log.info("가게 엑셀 업로드 요청: ownerId={}, fileName={}", ownerId, file != null ? file.getOriginalFilename() : null);

        int[] result = storeExcelFacade.importStoresFromExcel(file, ownerId);

        UploadStoresByExcelResponse response = new UploadStoresByExcelResponse(
                result[0],
                result[1],
                result[2]
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }
}

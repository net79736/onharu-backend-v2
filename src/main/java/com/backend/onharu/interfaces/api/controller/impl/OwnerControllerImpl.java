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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.onharu.application.OwnerFacade;
import com.backend.onharu.application.StoreFacade;
import com.backend.onharu.application.StoreScheduleFacade;
import com.backend.onharu.application.dto.StoreBookingSummaryResult;
import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.file.dto.FileQuery.ListByRefsQuery;
import com.backend.onharu.domain.file.model.File;
import com.backend.onharu.domain.file.service.FileQueryService;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.reservation.support.ReservationSearchSortResolver;
import com.backend.onharu.domain.store.dto.StoreCommand.DeleteStoreCommand;
import com.backend.onharu.domain.store.dto.StoreWithFavoriteCount;
import com.backend.onharu.domain.store.model.BusinessHours;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.repository.BusinessHoursRepository;
import com.backend.onharu.domain.store.support.StoreOpenStatusCalculator;
import com.backend.onharu.domain.store.support.StoreSearchSortResolver;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.common.util.PageableUtil;
import com.backend.onharu.interfaces.api.controller.IOwnerController;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.CancelReservationRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.CreateOwnerRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.CreateOwnerResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetMyStoresRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetMyStoresResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetOwnerResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetStoreBookingDetailResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetStoreBookingListResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetStoreBookingSummaryResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetStoreBookingsRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.RemoveAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.ReservationResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.SetAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.UpdateAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.UpdateOwnerRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.OpenStoreRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.OpenStoreResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.StoreResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.UpdateStoreRequest;
import com.backend.onharu.interfaces.api.dto.StoreRequestMapperDto;
import com.backend.onharu.utils.NumberUtils;
import com.backend.onharu.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사업주 관련 API를 제공하는 컨트롤러 구현체입니다.
 * 
 * 사업자 정보 등록, 수정, 삭제, 조회 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerControllerImpl implements IOwnerController {

    private final OwnerFacade ownerFacade;

    private final StoreFacade storeFacade;

    private final StoreScheduleFacade storeScheduleFacade;

    private final FileQueryService fileQueryService;
    
    private final BusinessHoursRepository businessHoursRepository;

    /**
     * 사업자 정보 등록
     * 
     * POST /api/owners/business
     * 사업자 정보를 등록합니다.
     *
     * @param request 사업자 정보 등록 요청
     * @param businessRegistrationFile 사업자 등록 서류 파일
     * @return 생성된 사업자 정보
     */
    @Override
    @PostMapping("/business")
    public ResponseEntity<ResponseDTO<CreateOwnerResponse>> registerBusiness(
            @RequestPart CreateOwnerRequest request,
            @RequestPart MultipartFile businessRegistrationFile
    ) {
        log.info("사업자 정보 등록 요청: request={}, fileName={}", request, businessRegistrationFile.getOriginalFilename());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사업자 정보 수정
     * 
     * PUT /api/owners/{ownerId}/business
     * 사업자 정보를 수정합니다. 
     *
     * @param ownerId 사업자 ID
     * @param request 사업자 정보 수정 요청
     * @param businessRegistrationFile 사업자 등록 서류 파일
     * @return 수정 결과
     */
    @Override
    @PutMapping("/{ownerId}/business")
    public ResponseEntity<ResponseDTO<Void>> updateBusiness(
            @PathVariable("ownerId") Long ownerId,
            @RequestPart UpdateOwnerRequest request,
            @RequestPart MultipartFile businessRegistrationFile
    ) {
        log.info("사업자 정보 수정 요청: ownerId={}, request={}, fileName={}", 
                ownerId, request, businessRegistrationFile.getOriginalFilename());
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사업자 정보 삭제
     * 
     * DELETE /api/owners/business/{ownerId}
     * 사업자 정보를 삭제합니다. 
     *
     * @param ownerId 사업자 ID
     * @return 삭제 결과
     */
    @Override
    @DeleteMapping("/business/{ownerId}")
    public ResponseEntity<ResponseDTO<Void>> closeBusiness(
            @PathVariable("ownerId") Long ownerId
    ) {
        log.info("사업자 정보 삭제 요청: ownerId={}", ownerId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 사업자 정보 조회
     * 
     * GET /api/owners/business/{ownerId}
     * 사업자 정보를 조회합니다. 
     *
     * @param ownerId 사업자 ID
     * @return 사업자 정보
     */
    @Override
    @GetMapping("/business/{ownerId}")
    public ResponseEntity<ResponseDTO<GetOwnerResponse>> getMyBusiness(
            @PathVariable("ownerId") Long ownerId
    ) {
        log.info("사업자 정보 조회 요청: ownerId={}", ownerId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 가게 정보 작성
     *
     * POST /api/owners/stores
     * 사업자가 신규 가게 정보를 생성합니다.
     */
    @Override
    @PostMapping("/stores")
    public ResponseEntity<ResponseDTO<OpenStoreResponse>> openStore(
            @Valid @RequestBody OpenStoreRequest request
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        log.info("가게 정보 작성 요청: ownerId={}, request={}", ownerId, request);

        Store store = storeFacade.createStore(
                StoreRequestMapperDto.toCreateStoreCommand(request, ownerId), ownerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(new OpenStoreResponse(store.getId())));
    }

    /**
     * 가게 정보 수정
     *
     * PUT /api/owners/stores/{storeId}
     * 사업자가 자신의 가게 정보를 수정합니다.
     */
    @Override
    @PutMapping("/stores/{storeId}")
    public ResponseEntity<ResponseDTO<Void>> updateMyStore(
            @PathVariable("storeId") Long storeId,
            @RequestBody UpdateStoreRequest request
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        log.info("가게 정보 수정 요청: ownerId={}, storeId={}, request={}", ownerId, storeId, request);

        storeFacade.updateStore(
                StoreRequestMapperDto.toUpdateStoreCommand(storeId, request), ownerId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 가게 정보 삭제
     *
     * DELETE /api/owners/stores/{storeId}
     * 사업자가 자신의 가게 정보를 삭제합니다.
     */
    @Override
    @DeleteMapping("/stores/{storeId}")
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
     * 사업자 가게 목록 조회
     * 
     * GET /api/owners/stores
     * 사업자의 가게 목록을 조회합니다.
     *
     * @param ownerId 사업자 ID
     * @return 사업자 가게 목록
     */
    @Override
    @GetMapping("/stores")
    public ResponseEntity<ResponseDTO<GetMyStoresResponse>> getMyStores(
            @ModelAttribute GetMyStoresRequest request
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId();

        log.info("사업자 가게 목록 조회 요청: ownerId={}, request={}", ownerId, request);

        // 정렬 필드 변환
        String sortField = StoreSearchSortResolver.resolve(request.sortField(), false);
        Pageable pageable = PageableUtil.ofOneBased(
                request.pageNum(),
                request.perPage(),
                sortField,
                request.sortDirection()
        );

        // 사업자의 가게 목록 조회
        Page<StoreWithFavoriteCount> storePage = ownerFacade.getMyStores(ownerId, pageable);

        // 가게 ID 목록 추출
        List<Long> storeIds = storePage.getContent().stream()
        .map(StoreWithFavoriteCount::store)
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

        // 예약 가능한 가게 ID 집합 조회
        Set<Long> validScheduleStoreIds = storeIds.isEmpty()
                ? Set.of()
                : storeScheduleFacade.filterReservableStoreIds(new HashSet<>(storeIds), LocalDate.now());

        // 배치로 영업시간 조회 (N+1 방지)
        Map<Long, List<BusinessHours>> businessHoursByStoreId = businessHoursRepository.findAllByStoreIds(storeIds).stream()
                .collect(Collectors.groupingBy(bh -> bh.getStore().getId()));

        // DTO 변환
        LocalDateTime now = LocalDateTime.now();
        List<StoreResponse> storeResponses = storePage.stream()
                .map(storePageObject -> {
                    // 이미지 목록 추출
                    List<String> images = imagesByStoreId.getOrDefault(storePageObject.store().getId(), List.of());
                    double distanceKm = NumberUtils.truncateToIntegerAsDouble(storePageObject.distance()); // 소수점 버림
                    boolean effectiveIsSharing = Boolean.TRUE.equals(storePageObject.store().getIsSharing())
                            || validScheduleStoreIds.contains(storePageObject.store().getId()); // 공유중
                    // 영업중 여부
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

        GetMyStoresResponse response = new GetMyStoresResponse(
            storeResponses,
            storePage.getTotalElements(),
            getCurrentPage(storePage),
            storePage.getTotalPages(),
            storePage.getSize()
        );
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 예약 관리 목록 조회
     * 
     * GET /api/owners/stores/{storeId}/reservations
     * 사업자의 예약 목록을 조회합니다.
     * - 파라미터 없음: 기존 동작 (store_schedule별 최신 1건)
     * - pageNum, perPage, statusFilter 등 제공 시: 페이징 + 필터 적용
     *
     * @return 예약 관리 목록
     */
    @Override
    @GetMapping("/stores/{storeId}/reservations")
    public ResponseEntity<ResponseDTO<GetStoreBookingListResponse>> getStoreBookings(
            @PathVariable("storeId") Long storeId,
            @ModelAttribute GetStoreBookingsRequest request
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId();

        log.info("예약 관리 목록 조회 요청: ownerId={}, storeId={}, request={}", ownerId, storeId, request);

        String sortField = ReservationSearchSortResolver.resolve(request.sortField());
        Pageable pageable = PageableUtil.ofOneBased(
                request.pageNum(),
                request.perPage(),
                sortField,
                request.sortDirection()
        );
        Page<Reservation> page = ownerFacade.getStoreBookings(ownerId, storeId, request.effectiveStatusFilter(), pageable);
        List<ReservationResponse> reservationResponses = page.getContent().stream()
                .map(ReservationResponse::new)
                .collect(Collectors.toList());
        GetStoreBookingListResponse response = GetStoreBookingListResponse.of(
                reservationResponses,
                page.getTotalElements(),
                getCurrentPage(page),
                page.getTotalPages(),
                page.getSize()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 예약 관리 상세 조회
     * 
     * GET /api/owners/stores/{storeId}/reservations/{reservationId}
     * 사업자의 특정 예약의 상세 정보를 조회합니다.
     *
     * @param reservationId 예약 ID
     * @return 예약 상세 정보
     */
    @Override
    @GetMapping("/stores/{storeId}/reservations/{reservationId}")
    public ResponseEntity<ResponseDTO<GetStoreBookingDetailResponse>> getStoreBooking(
        @PathVariable("storeId") Long storeId,
        @PathVariable("reservationId") Long reservationId
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId();

        log.info("예약 관리 상세 조회 요청: ownerId={}, storeId={}, reservationId={}", ownerId, storeId, reservationId);

        Reservation reservation = ownerFacade.getStoreBooking(reservationId, ownerId, storeId);
        ReservationResponse reservationResponse = new ReservationResponse(reservation);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(new GetStoreBookingDetailResponse(reservationResponse)));
    }

    /**
     * 예약 확정(승인)
     * 
     * POST /api/owners/reservations/{reservationId}/approve
     * 사업자가 예약을 확정합니다. (WAITING → CONFIRMED)
     *
     * @param reservationId 예약 ID
     * @return 확정 결과
     */
    @Override
    @PostMapping("/reservations/{reservationId}/approve")
    public ResponseEntity<ResponseDTO<Void>> approveBook(
            @PathVariable("reservationId") Long reservationId
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId();

        log.info("예약 확정(승인) 요청: ownerId={}, reservationId={}", ownerId, reservationId);

        ownerFacade.confirmReservation(reservationId, ownerId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 완료
     * 
     * POST /api/owners/reservations/{reservationId}/complete
     * 사업자가 예약을 완료 처리합니다. (CONFIRMED → COMPLETED)
     *
     * @param reservationId 예약 ID
     * @return 완료 결과
     */
    @Override
    @PostMapping("/reservations/{reservationId}/complete")
    public ResponseEntity<ResponseDTO<Void>> completeBook(
            @PathVariable("reservationId") Long reservationId
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId();

        log.info("예약 완료 요청: ownerId={}, reservationId={}", ownerId, reservationId);

        ownerFacade.completeReservation(reservationId, ownerId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 취소
     * 
     * POST /api/owners/reservations/{reservationId}/cancel
     * 사업자가 예약을 취소합니다.
     *
     * @param reservationId 예약 ID
     * @return 취소 결과
     */
    @Override
    @PostMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<ResponseDTO<Void>> cancelBook(
            @PathVariable("reservationId") Long reservationId,
            @Valid @RequestBody CancelReservationRequest request
    ) {
        log.info("예약 취소 요청: reservationId={}, request={}", reservationId, request);
        
        ownerFacade.cancelReservation(reservationId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 가게 스케줄 생성
     * 
     * POST /api/owners/stores/{storeId}/schedules
     * 사업자가 가게의 예약 가능한 스케줄을 생성합니다.
     */
    @Override
    @PostMapping("/stores/{storeId}/schedules")
    public ResponseEntity<ResponseDTO<Void>> setAvailableDates(
            @PathVariable("storeId") Long storeId,
            @Valid @RequestBody SetAvailableDatesRequest request
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId();

        log.info("예약 가능한 날짜 생성 요청: ownerId={}, storeId={}, request={}", ownerId, storeId, request);

        ownerFacade.setAvailableDates(storeId, ownerId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 가게 스케줄 수정
     * 
     * PUT /api/owners/stores/{storeId}/schedules
     * 사업자가 가게의 예약 가능한 스케줄을 수정합니다.
     */
    @Override
    @PutMapping("/stores/{storeId}/schedules")
    public ResponseEntity<ResponseDTO<Void>> updateAvailableDates(
            @PathVariable("storeId") Long storeId,
            @Valid @RequestBody UpdateAvailableDatesRequest request
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId();

        log.info("예약 가능한 날짜 수정 요청: ownerId={}, storeId={}, request={}", ownerId, storeId, request);

        ownerFacade.updateAvailableDates(storeId, ownerId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 가게 스케줄 삭제
     * 
     * DELETE /api/owners/stores/{storeId}/schedules
     * 사업자가 가게의 예약 가능한 스케줄을 삭제합니다.
     */
    @Override
    @DeleteMapping("/stores/{storeId}/schedules")
    public ResponseEntity<ResponseDTO<Void>> removeAvailableDates(
            @PathVariable("storeId") Long storeId,
            @RequestBody RemoveAvailableDatesRequest request
    ) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        
        log.info("예약 가능한 날짜 삭제 요청: ownerId={}, storeId={}, request={}", ownerId, storeId, request);

        ownerFacade.removeAvailableDates(storeId, ownerId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 요약된 예약 목록 조회
     * 
     * GET /api/owners/reservations/summary
     * 사업자의 요약된 예약 목록을 조회합니다.
     */
    @Override
    @GetMapping("/reservations/summary")
    public ResponseEntity<ResponseDTO<GetStoreBookingSummaryResponse>> getStoreBookingsSummary() {
        Long ownerId = SecurityUtils.getCurrentUserId();
        
        log.info("요약된 예약 목록 조회 요청: ownerId={}", ownerId);

        StoreBookingSummaryResult summaryResult = ownerFacade.getStoreBookingsSummary(ownerId);

        // 엔티티 → 응답 DTO 변환
        List<ReservationResponse> upcomingResponses = summaryResult.upcomingReservations().stream()
                .map(ReservationResponse::new)
                .toList();

        GetStoreBookingSummaryResponse response = new GetStoreBookingSummaryResponse(upcomingResponses);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

}

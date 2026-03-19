package com.backend.onharu.application;

import static com.backend.onharu.application.dto.StoreBookingSummaryResult.UPCOMING_LIMIT;
import static com.backend.onharu.utils.SecurityUtils.getCurrentUserId;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.application.dto.StoreBookingSummaryResult;
import com.backend.onharu.application.validator.StoreScheduleValidator;
import com.backend.onharu.application.validator.StoreScheduleValidator.ScheduleTimeRange;
import com.backend.onharu.domain.common.enums.NotificationHistoryType;
import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.dto.LevelQuery.FindFirstByConditionNumberQuery;
import com.backend.onharu.domain.level.service.LevelQueryService;
import com.backend.onharu.domain.owner.dto.OwnerCommand.UpdateOwnerCommand;
import com.backend.onharu.domain.owner.dto.OwnerQuery.GetOwnerByIdQuery;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.service.OwnerCommandService;
import com.backend.onharu.domain.owner.service.OwnerQueryService;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CancelReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CompleteReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.ConfirmReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByStoreIdAndStatusFilterQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindUpcomingByOwnerIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetReservationByIdQuery;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.reservation.service.ReservationCommandService;
import com.backend.onharu.domain.reservation.service.ReservationQueryService;
import com.backend.onharu.domain.store.dto.StoreQuery.FindWithCategoryAndFavoriteCountByOwnerIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreByIdQuery;
import com.backend.onharu.domain.store.dto.StoreWithFavoriteCount;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.service.StoreQueryService;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleCommand.DeleteStoreScheduleCommand;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleCommand.UpdateStoreScheduleCommand;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.GetStoreScheduleByIdQuery;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.storeschedule.service.StoreScheduleCommandService;
import com.backend.onharu.domain.storeschedule.service.StoreScheduleQueryService;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.event.model.ReservationEvent;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.CancelReservationRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.RemoveAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.SetAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.UpdateAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.ReservationStatusFilter;

import lombok.RequiredArgsConstructor;

/**
 * 사업자 Facade
 */
@Component
@RequiredArgsConstructor
public class OwnerFacade {

    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;
    private final StoreQueryService storeQueryService;
    private final StoreScheduleQueryService storeScheduleQueryService;
    private final OwnerQueryService ownerQueryService;
    private final StoreScheduleCommandService storeScheduleCommandService;
    private final StoreScheduleValidator storeScheduleValidator;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OwnerCommandService ownerCommandService;
    private final LevelQueryService levelQueryService;

    /**
     * 사업자의 가게 목록 조회
     * 
     * @param ownerId 사업자 ID
     * @return 사업자의 가게 목록
     */
    public Page<StoreWithFavoriteCount> getMyStores(Long ownerId, Pageable pageable) {
        // 사업자 정보 조회 (존재 여부 확인)
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(ownerId));

        // 사업자의 가게 목록 조회
        Page<StoreWithFavoriteCount> stores = storeQueryService.findWithCategoryAndFavoriteCountByOwnerId(new FindWithCategoryAndFavoriteCountByOwnerIdQuery(ownerId), pageable);

        // 각 가게가 해당 사업자의 소유인지 검증
        stores.forEach(store -> store.store().belongsTo(owner));

        return stores;
    }

    /**
     * 사업자의 가게 단건 조회
     * 
     * @param owenrId 사업자 ID
     * @param storeId 가게 ID
     * @return 사업자의 가게 단건
     */
    public Store getMyStore(Long owenrId, Long storeId) {
        // 사업자 정보 조회
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(owenrId));

        // 가게 정보 조회
        Store store = storeQueryService.getStoreById(new GetStoreByIdQuery(storeId));

        // 사업자가 가게의 주인인지 확인
        store.belongsTo(owner);

        return store;
    }

    /**
     * 사업자 가게의 예약 목록 조회 (페이징 + 상태 필터)
     *
     * @param ownerId      사업자 ID
     * @param storeId      가게 ID
     * @param statusFilter 예약 상태 필터
     * @param pageable     페이징 정보
     * @return 사업자의 예약 목록 (페이지)
     */
    public Page<Reservation> getStoreBookings(Long ownerId, Long storeId, ReservationStatusFilter statusFilter, Pageable pageable) {
        // 사업자 정보 조회
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(ownerId));
        // 가게 정보 조회
        Store store = storeQueryService.getStoreById(new GetStoreByIdQuery(storeId));
        // 사업자가 가게의 주인인지 확인
        store.belongsTo(owner);
        // 예약 목록 조회
        return reservationQueryService.findByStoreIdAndStatusFilter(
                new FindByStoreIdAndStatusFilterQuery(storeId, statusFilter.toReservationType()), pageable);
    }

    /**
     * 사업자의 특정 예약의 상세 정보를 조회
     * 
     * @param reservationId 예약 ID
     * @param ownerId 사업자 ID
     * @param storeId 가게 ID
     * 
     * @return 사업자의 특정 예약의 상세 정보
     */
    public Reservation getStoreBooking(Long reservationId, Long ownerId, Long storeId) {
        // 사업자 정보 조회
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(ownerId));

        // 가게 정보 조회
        Store store = storeQueryService.getStoreById(new GetStoreByIdQuery(storeId));

        // 사업자가 가게의 주인인지 확인
        store.belongsTo(owner);

        // 예약 정보 조회
        Reservation reservation = reservationQueryService.getReservation(new GetReservationByIdQuery(reservationId));

        // 예약이 해당 가게의 예약인지 확인
        reservation.belongsToStore(storeId);

        return reservation;
    }

    /**
     * 예약 가능한 날짜 생성
     * 
     * @param storeId 가게 ID
     * @param request 예약 가능한 날짜 생성 요청
     */
    @Transactional
    public void setAvailableDates(Long storeId, Long ownerId, SetAvailableDatesRequest request) {
        // 가게 정보 조회
        Store store = storeQueryService.getStoreById(new GetStoreByIdQuery(storeId));

        // 사업자 정보 조회
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(ownerId));

        // 사업자가 가게의 주인인지 확인
        store.belongsTo(owner);

        // 일정 중복 검증 (요청 내부 중복 + 기존 DB 일정과의 중복)
        List<ScheduleTimeRange> timeRanges = request.storeSchedules().stream()
                .map(req -> new ScheduleTimeRange(
                        req.scheduleDate(),
                        req.startTime(),
                        req.endTime()))
                .toList();
        storeScheduleValidator.validateNoDuplicates(storeId, timeRanges, null);

        // 요청 데이터를 리스트로 변환
        List<StoreSchedule> schedules = request.storeSchedules().stream()
        .map(req -> StoreSchedule.builder()
                .store(store)
                .scheduleDate(req.scheduleDate())
                .startTime(req.startTime())
                .endTime(req.endTime())
                .maxPeople(req.maxPeople())
                .build())
        .toList();

        // 저장
        storeScheduleCommandService.createStoreSchedules(schedules);
    }

    /**
     * 예약 가능한 날짜 수정
     * 
     * @param storeId 가게 ID
     * @param ownerId 사업자 ID
     * @param request 예약 가능한 날짜 수정 요청
     */
    @Transactional
    public void updateAvailableDates(Long storeId, Long ownerId, UpdateAvailableDatesRequest request) {
        // 가게 정보 조회
        Store store = storeQueryService.getStoreById(new GetStoreByIdQuery(storeId));

        // 사업자 정보 조회
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(ownerId));

        // 사업자가 가게의 주인인지 확인
        store.belongsTo(owner);

        // 일정 중복 검증 (요청 내부 중복 + 기존 DB 일정과의 중복, 자기 자신은 제외)
        // 스케쥴 아이디 목록
        Set<Long> updatingScheduleIds = request.storeSchedules().stream()
                .map(req -> req.id())
                .collect(Collectors.toSet());
        // 일정 시간 범위 객체 리스트
        List<ScheduleTimeRange> timeRanges = request.storeSchedules().stream()
                .map(req -> new ScheduleTimeRange(
                        req.scheduleDate(),
                        req.startTime(),
                        req.endTime()))
                .toList();
        // 일정 중복 검증
        storeScheduleValidator.validateNoDuplicates(storeId, timeRanges, updatingScheduleIds);

        // 각 스케줄이 해당 가게에 속하는지 검증하고 업데이트
        request.storeSchedules().forEach(req -> {
            // 스케줄 조회 및 가게 소유권 검증
            StoreSchedule schedule = storeScheduleQueryService.getStoreScheduleById(
                    new GetStoreScheduleByIdQuery(req.id()));
            
            // 스케줄이 해당 가게에 속하는지 확인
            if (!schedule.getStore().getId().equals(storeId)) {
                throw new CoreException(
                        ErrorType.Store.STORE_OWNER_MISMATCH);
            }

            // 스케줄 업데이트
            storeScheduleCommandService.updateStoreSchedule(
                    new UpdateStoreScheduleCommand(
                            req.id(),
                            req.scheduleDate(),
                            req.startTime(),
                            req.endTime(),
                            req.maxPeople()
                    )
            );
        });
    }
    
    /**
     * 예약 가능한 날짜 삭제
     * 
     * @param storeId 가게 ID
     * @param request 예약 가능한 날짜 삭제 요청
     */
    public void removeAvailableDates(Long storeId, Long ownerId, RemoveAvailableDatesRequest request) {
        // 가게 정보 조회
        Store store = storeQueryService.getStoreById(new GetStoreByIdQuery(storeId));

        // 사업자 정보 조회
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(ownerId));

        // 사업자가 가게의 주인인지 확인
        store.belongsTo(owner);

        // 요청 데이터를 리스트로 변환
        List<StoreSchedule> schedules = request.storeScheduleIds().stream()
            .map(id -> storeScheduleQueryService.getStoreScheduleById(new GetStoreScheduleByIdQuery(id)))
            .toList();

        // 삭제
        for (StoreSchedule schedule : schedules) {
            storeScheduleCommandService.deleteStoreSchedule(new DeleteStoreScheduleCommand(schedule.getId()));
        }
    }

    /**
     * 예약 확정 (승인) - WAITING → CONFIRMED
     * 사업자가 예약을 승인/확인할 때 호출
     *
     * @param reservationId 예약 ID
     * @param ownerId 사업자 ID
     */
    public void confirmReservation(Long reservationId, Long ownerId) {
        Reservation reservation = reservationQueryService.getReservation(new GetReservationByIdQuery(reservationId));
        Store store = storeQueryService.getStoreById(new GetStoreByIdQuery(reservation.getStoreSchedule().getStore().getId()));
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(ownerId));

        // 사업자가 가게의 주인인지 확인
        store.belongsTo(owner);

        // 예약 확정
        reservationCommandService.confirmReservation(new ConfirmReservationCommand(reservation.getId()));

        applicationEventPublisher.publishEvent(new ReservationEvent(
                reservation.getId(),
                owner.getId(),
                reservation.getChild().getId(),
                NotificationHistoryType.RESERVATION_CONFIRMED
        ));
    }

    /**
     * 예약 완료 - CONFIRMED → COMPLETED
     * 서비스 이용이 완료되었을 때 사업자가 호출
     *
     * @param reservationId 예약 ID
     * @param ownerId 사업자 ID
     */
    @Transactional
    public void completeReservation(Long reservationId, Long ownerId) {
        Reservation reservation = reservationQueryService.getReservation(new GetReservationByIdQuery(reservationId));
        Store store = storeQueryService.getStoreById(new GetStoreByIdQuery(reservation.getStoreSchedule().getStore().getId()));

        // Owner 조회
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(ownerId));

        // 사업자가 가게의 주인인지 확인
        store.belongsTo(owner);

        // 예약 완료
        reservationCommandService.completeReservation(new CompleteReservationCommand(reservation.getId()));

        applicationEventPublisher.publishEvent(new ReservationEvent(
                reservation.getId(),
                owner.getId(),
                reservation.getChild().getId(),
                NotificationHistoryType.RESERVATION_COMPLETED
        ));

        // 사업자(Owner) 의 나눔횟수 증가
        owner.increaseDistribution(1);

        // 현재 사업자의 나눔 횟수를 기준으로 등급 조회
        levelQueryService.findFirstByConditionNumber(
                new FindFirstByConditionNumberQuery(owner.getDistributionCount())
        ).ifPresent(nextLevel -> {
            // 사업자의 현재 등급과 조회된 등급이 다를 경우
            if (!owner.getLevel().equals(nextLevel)) {
                // 사업자의 등급을 교체
                owner.changeLevel(nextLevel);
            }
        });

        // 사업자(Owner) 변경사항 저장(더티체킹)
        ownerCommandService.updateOwner(
                new UpdateOwnerCommand(owner)
        );
    }

    /**
     * 예약 취소
     * 
     * @param reservationId 예약 ID
     */
    public void cancelReservation(Long reservationId, CancelReservationRequest request) {
        // 예약 정보 조회
        Reservation reservation = reservationQueryService.getReservation(new GetReservationByIdQuery(reservationId));
        Store store = storeQueryService.getStoreById(new GetStoreByIdQuery(reservation.getStoreSchedule().getStore().getId()));

        // Owner 조회
        Owner owner = ownerQueryService.getOwnerById(new GetOwnerByIdQuery(getCurrentUserId()));

        // 사업자가 가게의 주인인지 확인
        store.belongsTo(owner);

        // 예약 취소
        reservationCommandService.cancelReservation(new CancelReservationCommand(reservation.getId(), UserType.OWNER, request.cancelReason()));

        applicationEventPublisher.publishEvent(new ReservationEvent(
            reservation.getId(),
            owner.getId(),
            reservation.getChild().getId(),
            NotificationHistoryType.RESERVATION_REJECTED
        ));
    }

    /**
     * 요약된 예약 목록 조회
     * 
     * @param ownerId 사업자 ID
     * @return 요약된 예약 목록
     */
    public StoreBookingSummaryResult getStoreBookingsSummary(Long ownerId) {
        // 사업자 정보 조회
        ownerQueryService.getOwnerById(new GetOwnerByIdQuery(ownerId));

        // 다가오는 방문 예정 예약 (WAITING) - 오늘 이후, scheduleDate 오름차순
        List<Reservation> upcomingReservations = reservationQueryService.findUpcomingByOwnerId(
            new FindUpcomingByOwnerIdQuery(
                ownerId,
                List.of(ReservationType.WAITING),
                LocalDate.now()
            ),
            PageRequest.of(0, UPCOMING_LIMIT, Sort.by(Sort.Direction.ASC, "storeSchedule.scheduleDate"))
        );

        return new StoreBookingSummaryResult(upcomingReservations);
    }
}

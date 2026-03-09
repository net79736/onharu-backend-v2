package com.backend.onharu.application;

import static com.backend.onharu.domain.common.enums.NotificationHistoryType.RESERVATION_CANCELED;
import static com.backend.onharu.domain.common.enums.NotificationHistoryType.RESERVATION_CREATED;
import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_PEOPLE_EXCEEDS_MAX;
import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_PEOPLE_MUST_NOT_BE_NULL;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.child.dto.ChildQuery.GetChildByIdQuery;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.service.ChildQueryService;
import com.backend.onharu.domain.favorite.dto.FavoriteCommand.CreateFavoriteCommand;
import com.backend.onharu.domain.favorite.dto.FavoriteCommand.DeleteFavoriteCommand;
import com.backend.onharu.domain.favorite.dto.FavoriteQuery.FindFavoritesByChildIdQuery;
import com.backend.onharu.domain.favorite.dto.FavoriteQuery.GetFavoriteByIdQuery;
import com.backend.onharu.domain.favorite.model.Favorite;
import com.backend.onharu.domain.favorite.service.FavoriteCommandService;
import com.backend.onharu.domain.favorite.service.FavoriteQueryService;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CancelReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CreateReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByChildIdAndStatusFilterQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetByStoreScheduleIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetReservationByIdQuery;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.reservation.service.ReservationCommandService;
import com.backend.onharu.domain.reservation.service.ReservationQueryService;
import com.backend.onharu.domain.review.dto.ReviewQuery.FindReviewedReservationIdsQuery;
import com.backend.onharu.domain.review.service.ReviewQueryService;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreByIdQuery;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.service.StoreQueryService;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.GetStoreScheduleByIdQuery;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.storeschedule.service.StoreScheduleQueryService;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.event.model.ReservationEvent;
import com.backend.onharu.interfaces.api.dto.ReservationStatusFilter;
import com.backend.onharu.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

/**
 * 결식 아동 Facade
 */
@Component
@RequiredArgsConstructor
public class ChildFacade {

    private final ChildQueryService childQueryService;
    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;
    private final ReviewQueryService reviewQueryService;
    private final StoreScheduleQueryService storeScheduleQueryService;
    private final StoreQueryService storeQueryService;
    private final FavoriteCommandService favoriteCommandService;
    private final FavoriteQueryService favoriteQueryService;

    // 스프링 이벤트 발행기. 쿠폰 발급 완료 등 도메인 이벤트를 발행할 때 사용합니다.
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 예약 하기
     */
    @Transactional
    public void reserve(CreateReservationCommand command) {
        // 결식 아동 조회   
        Child child = childQueryService.getChildById(new GetChildByIdQuery(command.childId()));
        
        // 가게 일정 조회
        StoreSchedule storeSchedule = storeScheduleQueryService.getStoreScheduleById(new GetStoreScheduleByIdQuery(command.storeScheduleId()));
        Owner owner = storeSchedule.getStore().getOwner();

        // 조회한 가게 일정이 이미 예약된 일정인지 체크 (테이블 조회해서 확인)
        Reservation reservation = reservationQueryService.getByStoreScheduleId(new GetByStoreScheduleIdQuery(command.storeScheduleId()));        
        // 예약 가능 여부 확인
        boolean isUnavailable = reservation != null && !reservation.isAvailable();
        if (isUnavailable) {
            throw new CoreException(ErrorType.Reservation.RESERVATION_ALREADY_EXISTS);
        }

        // 예약 인원이 최대 수용 인원을 초과하는지 검증
        if (command.people() == null || command.people() <= 0) {
            throw new CoreException(RESERVATION_PEOPLE_MUST_NOT_BE_NULL);
        }

        // 예약 인원이 최대 수용 인원을 초과하는지 검증
        if (storeSchedule.getMaxPeople() != null && command.people() > storeSchedule.getMaxPeople()) {
            throw new CoreException(RESERVATION_PEOPLE_EXCEEDS_MAX);
        }

        // 예약 생성
        Reservation createdReservation = reservationCommandService.createReservation(command, storeSchedule, child);

        // 예약 생성 이벤트 발행
        applicationEventPublisher.publishEvent(new ReservationEvent(
            createdReservation.getId(),
            owner.getId(),
            SecurityUtils.getCurrentUserId(),
            RESERVATION_CREATED
        ));
    }

    /**
     * 예약 취소
     */
    @Transactional
    public void cancelReservation(CancelReservationCommand command, Long childId) {
        // 예약 조회
        Reservation reservation = reservationQueryService.getReservation(new GetReservationByIdQuery(command.reservationId()));
        Owner owner = reservation.getStoreSchedule().getStore().getOwner();

        // 현재 로그인한 아동 정보 조회
        Child child = childQueryService.getChildById(new GetChildByIdQuery(childId));

        // 예약이 해당 아동에 속하는지 확인
        reservation.belongsToChild(child.getId());

        // 예약이 완료 상태인 경우 취소할 수 없도록 예외를 발생시킨다.
        reservation.validateCancelable();

        // 예약 취소
        reservationCommandService.cancelReservation(command);

        // 예약 취소 이벤트 발행
        applicationEventPublisher.publishEvent(new ReservationEvent(
            command.reservationId(),
            owner.getId(),
            SecurityUtils.getCurrentUserId(),
            RESERVATION_CANCELED
        ));
    }

    /**
     * 내가 신청한 예약 목록 조회
     *
     * @return 내가 신청한 예약 목록
     */
    public Page<Reservation> getMyBookings(Long childId, ReservationStatusFilter statusFilter, Pageable pageable) {
        Child child = childQueryService.getChildById(new GetChildByIdQuery(childId));
        return reservationQueryService.findByChildIdAndStatusFilter(new FindByChildIdAndStatusFilterQuery(child.getId(), statusFilter), pageable);
    }

    /**
     * 내가 신청한 특정 예약의 상세 정보를 조회
     * @param reservationId 예약 ID
     * @return 내가 신청한 특정 예약의 상세 정보
     */
    public Reservation getMyBooking(Long reservationId, Long childId) {
        // 현재 로그인한 아동 정보 조회
        Child child = childQueryService.getChildById(new GetChildByIdQuery(childId));

        // 내가 신청한 특정 예약의 상세 정보 조회
        Reservation reservation = reservationQueryService.getReservation(new GetReservationByIdQuery(reservationId));

        // 현재 로그인한 아동 정보와 예약자 정보가 다르면 예외 발생
        reservation.belongsToChild(child.getId());

        return reservation;
    }

    /**
     * 내 예약 ID 목록 중 리뷰 작성 완료된 예약 ID 목록 조회
     *
     * @param reservationIds 예약 ID 목록
     * @return 리뷰 작성 완료된 예약 ID 목록
     */
    public List<Long> getMyReviewWrittenReservationIds(List<Long> reservationIds) {
        return reviewQueryService.findReviewedReservationIds(new FindReviewedReservationIdsQuery(reservationIds));
    }

    /**
     * (아동)내가 특정한 가게에 찜 등록
     * @param command 아동 ID, 가게 ID 가 포함된 CreateFavoriteCommand
     * @return 찜하기 엔티티 정보
     */
    public Favorite createFavorite(CreateFavoriteCommand command) {
        // 현재 로그인한 아동 정보 조회
        Child child = childQueryService.getChildById(
                new GetChildByIdQuery(command.childId())
        );

        // 가게 정보 조회
        Store store = storeQueryService.getStoreById(
                new GetStoreByIdQuery(command.storeId())
        );

        // 찜하기 생성
        return favoriteCommandService.createFavorite(command, child, store);
    }

    /**
     * (아동)내가 등록한 찜하기 목록 조회(페이징)
     * @param query
     * @return
     */
    public Page<Favorite> getMyFavorites(FindFavoritesByChildIdQuery query, Pageable pageable) {
        // 내가 등록한 찜 목록 조회
        return favoriteQueryService.findFavoritesByChildId(query, pageable);
    }

    /**
     * (아동)내가 찜하기 취소(삭제)
     * @param command
     */
    public void deleteFavorite(DeleteFavoriteCommand command) {
        // 현재 로그인한 아동 정보 조회
        childQueryService.getChildById(
                new GetChildByIdQuery(command.childId())
        );

        // 삭제할 찜 조회
        favoriteQueryService.getFavorite(
                new GetFavoriteByIdQuery(command.favoriteId())
        );

        // 찜하기 취소(삭제)
        favoriteCommandService.deleteFavorite(command);
    }
}

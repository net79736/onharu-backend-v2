package com.backend.onharu.application;

import java.util.List;

import com.backend.onharu.domain.favorite.dto.FavoriteCommand.CreateFavoriteCommand;
import com.backend.onharu.domain.favorite.dto.FavoriteCommand.DeleteFavoriteCommand;
import com.backend.onharu.domain.favorite.dto.FavoriteQuery.GetFavoriteByIdQuery;
import com.backend.onharu.domain.favorite.dto.FavoriteQuery.FindFavoritesByChildIdQuery;
import com.backend.onharu.domain.favorite.model.Favorite;
import com.backend.onharu.domain.favorite.service.FavoriteCommandService;
import com.backend.onharu.domain.favorite.service.FavoriteQueryService;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreByIdQuery;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.service.StoreQueryService;
import org.springframework.stereotype.Component;

import com.backend.onharu.domain.child.dto.ChildQuery.GetChildByIdQuery;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.service.ChildQueryService;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CancelReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CreateReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByChildIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetByStoreScheduleIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetReservationByIdQuery;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.reservation.service.ReservationCommandService;
import com.backend.onharu.domain.reservation.service.ReservationQueryService;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.GetStoreScheduleByIdQuery;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.storeschedule.service.StoreScheduleQueryService;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;

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
    private final StoreScheduleQueryService storeScheduleQueryService;
    private final StoreQueryService storeQueryService;
    private final FavoriteCommandService favoriteCommandService;
    private final FavoriteQueryService favoriteQueryService;

    /**
     * 예약 하기
     */
    public void reserve(CreateReservationCommand command) {
        // 결식 아동 조회   
        Child child = childQueryService.getChildById(new GetChildByIdQuery(command.childId()));
        
        // 가게 일정 조회
        StoreSchedule storeSchedule = storeScheduleQueryService.getStoreScheduleById(new GetStoreScheduleByIdQuery(command.storeScheduleId()));

        // 조회한 가게 일정이 이미 예약된 일정인지 체크 (테이블 조회해서 확인)
        Reservation reservation = reservationQueryService.getByStoreScheduleId(new GetByStoreScheduleIdQuery(command.storeScheduleId()));        
        if (reservation != null) {
            throw new CoreException(ErrorType.Reservation.RESERVATION_ALREADY_EXISTS);
        }

        // 예약 생성
        reservationCommandService.createReservation(command, storeSchedule, child);
    }

    /**
     * 예약 취소
     */
    public void cancelReservation(CancelReservationCommand command, Long childId) {
        // 예약 조회
        Reservation reservation = reservationQueryService.getReservation(new GetReservationByIdQuery(command.reservationId()));

        // 현재 로그인한 아동 정보 조회
        Child child = childQueryService.getChildById(new GetChildByIdQuery(childId));

        // 예약이 해당 아동에 속하는지 확인
        reservation.BelongsTo(child.getId());

        // 예약 취소
        reservationCommandService.cancelReservation(command);
    }

    /**
     * 내가 신청한 예약 목록 조회
     * 
     * @return 내가 신청한 예약 목록
     */
    public List<Reservation> getMyBookings(Long childId) {
        // 현재 로그인한 아동 정보 조회
        Child child = childQueryService.getChildById(new GetChildByIdQuery(childId));

        // 내가 신청한 예약 목록 조회
        return reservationQueryService.findByChildId(new FindByChildIdQuery(child.getId()));
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
        reservation.BelongsTo(child.getId());

        return reservation;
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
        Store store = storeQueryService.getStore(
                new GetStoreByIdQuery(command.storeId())
        );

        // 찜하기 생성
        return favoriteCommandService.createFavorite(command, child, store);
    }

    /**
     * (아동)내가 등록한 찜하기 목록 조회
     * @param query
     * @return
     */
    public List<Favorite> getMyFavorites(FindFavoritesByChildIdQuery query) {
        // 내가 등록한 찜 목록 조회
        return favoriteQueryService.findFavoritesByChildId(query);
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

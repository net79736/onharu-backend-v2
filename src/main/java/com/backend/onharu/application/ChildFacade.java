package com.backend.onharu.application;

import static com.backend.onharu.domain.common.enums.NotificationHistoryType.RESERVATION_CANCELED;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.application.dto.MyBookingSummaryResult;
import com.backend.onharu.domain.child.dto.ChildQuery.GetChildByIdQuery;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.service.ChildQueryService;
import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.favorite.dto.FavoriteCommand.CreateFavoriteCommand;
import com.backend.onharu.domain.favorite.dto.FavoriteCommand.DeleteFavoriteCommand;
import com.backend.onharu.domain.favorite.dto.FavoriteCommand.ToggleFavoriteCommand;
import com.backend.onharu.domain.favorite.dto.FavoriteQuery.FindFavoriteByChild_IdAndStore_IdQuery;
import com.backend.onharu.domain.favorite.dto.FavoriteQuery.FindFavoritesByChildIdQuery;
import com.backend.onharu.domain.favorite.model.Favorite;
import com.backend.onharu.domain.favorite.service.FavoriteCommandService;
import com.backend.onharu.domain.favorite.service.FavoriteQueryService;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CancelReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CreateReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByChildIdAndStatusFilterQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindCompletedWithoutReviewByChildIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindUpcomingByChildIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetReservationByIdQuery;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.reservation.service.ReservationCommandService;
import com.backend.onharu.domain.reservation.service.ReservationQueryService;
import com.backend.onharu.domain.reservation.service.ReservationTransactionService;
import com.backend.onharu.domain.review.dto.ReviewQuery.FindReviewedReservationIdsQuery;
import com.backend.onharu.domain.review.service.ReviewQueryService;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreByIdQuery;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.service.StoreFavoriteCountService;
import com.backend.onharu.domain.store.service.StoreQueryService;
import com.backend.onharu.domain.support.CacheName;
import com.backend.onharu.event.model.ReservationEvent;
import com.backend.onharu.infra.redis.lock.DistributeLockExecutor;
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
    private final StoreQueryService storeQueryService;
    private final StoreFavoriteCountService storeFavoriteCountService;
    private final FavoriteCommandService favoriteCommandService;
    private final FavoriteQueryService favoriteQueryService;

    // 스프링 이벤트 발행기. 쿠폰 발급 완료 등 도메인 이벤트를 발행할 때 사용합니다.
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DistributeLockExecutor distributeLockExecutor;
    private final ReservationTransactionService reservationTransactionService;

    /**
     * 예약 하기
     */
    public void reserve(CreateReservationCommand command) {
        String lockName = "lock:reservation:storeSchedule:" + command.storeScheduleId();
        distributeLockExecutor.execute(() -> {
            reservationTransactionService.reserveInTransaction(command);
            return null;
        }, lockName, 10_000, 10_000);
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
        applicationEventPublisher.publishEvent(
            new ReservationEvent(
                command.reservationId(),
                owner.getId(),
                SecurityUtils.getCurrentUserId(),
                RESERVATION_CANCELED
            )
        );
    }

    /**
     * 내가 신청한 예약 목록 조회
     *
     * @return 내가 신청한 예약 목록
     */
    public Page<Reservation> getMyBookings(Long childId, List<ReservationStatusFilter> statusFilters, Pageable pageable) {
        Child child = childQueryService.getChildById(new GetChildByIdQuery(childId));
        List<ReservationType> domainFilters = ReservationStatusFilter.toReservationTypes(statusFilters);
        return reservationQueryService.findByChildIdAndStatusFilter(new FindByChildIdAndStatusFilterQuery(child.getId(), domainFilters), pageable);
    }

    /**
     * 요약된 예약 목록 조회
     *
     * - 다가오는 방문 예정 예약 목록 (WAITING/CONFIRMED, scheduleDate 오름차순, 최대 2건)
     * - 리뷰 작성 대상 예약 목록 (COMPLETED 중 미리뷰, scheduleDate 내림차순, 최대 2건)
     *
     * @param childId 아동 ID
     * @return 요약 결과 (다가오는 예약 목록, 리뷰 대상 예약 목록)
     */
    public MyBookingSummaryResult getMyBookingSummary(Long childId) {
        childQueryService.getChildById(new GetChildByIdQuery(childId));

        // 1. 다가오는 방문 예정 예약 (WAITING, CONFIRMED) - 오늘 이후, scheduleDate 오름차순
        List<Reservation> upcomingReservations = reservationQueryService.findUpcomingByChildId(
                new FindUpcomingByChildIdQuery(
                        childId,
                        List.of(ReservationType.WAITING, ReservationType.CONFIRMED),
                        LocalDate.now()
                ),
                PageRequest.of(0, MyBookingSummaryResult.UPCOMING_LIMIT,
                        Sort.by(Sort.Direction.ASC, "storeSchedule.scheduleDate"))
        );

        // 2. 리뷰 작성 대상 예약 (COMPLETED 중 리뷰 미작성) - scheduleDate 내림차순
        List<Reservation> reviewTargetReservations = reservationQueryService.findCompletedWithoutReviewByChildId(
                new FindCompletedWithoutReviewByChildIdQuery(childId),
                PageRequest.of(0, MyBookingSummaryResult.REVIEW_TARGET_LIMIT,
                        Sort.by(Sort.Direction.DESC, "storeSchedule.scheduleDate"))
        );

        return new MyBookingSummaryResult(upcomingReservations, reviewTargetReservations);
    }

    /**
     * 내가 신청한 특정 예약의 상세 정보를 조회
     *
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
     * (아동)내가 등록한 찜하기 목록 조회(페이징)
     *
     * @param query
     * @return
     */
    public Page<Favorite> getMyFavorites(FindFavoritesByChildIdQuery query, Pageable pageable) {
        // 내가 등록한 찜 목록 조회
        return favoriteQueryService.findFavoritesByChildId(query, pageable);
    }

    /**
     * 찜하기 토글
     *
     * @param command 아동 ID 와 가게 ID 를 포함한 Command
     * @return true: 찜등록, false: 찜취소
     */
    @CacheEvict(cacheNames = CacheName.STORE_DETAIL, key = "'storeId:' + #command.storeId()")
    public boolean toggleFavorite(ToggleFavoriteCommand command) {
        Long childId = command.childId();
        Long storeId = command.storeId();

        // 찜하기 조회
        Optional<Favorite> favorite = favoriteQueryService.findFavoriteByChild_IdAndStore_Id(
                new FindFavoriteByChild_IdAndStore_IdQuery(childId, storeId)
        );

        // 찜하기 내역이 존재하다면 찜취소
        if (favorite.isPresent()) {
            favoriteCommandService.deleteFavorite(
                    new DeleteFavoriteCommand(favorite.get())
            );
            storeFavoriteCountService.changeFavoriteCount(storeId, -1);

            return false; // false: 찜취소
        }

        // 아동 조회
        Child child = childQueryService.getChildById(new GetChildByIdQuery(childId));

        // 가게 조회
        Store store = storeQueryService.getStoreById(
                new GetStoreByIdQuery(storeId)
        );

        // 찜하기 내역이 없는 경우 찜등록
        favoriteCommandService.createFavorite(
                new CreateFavoriteCommand(child, store)
        );
        storeFavoriteCountService.changeFavoriteCount(storeId, +1);

        return true; // true: 찜등록
    }
}

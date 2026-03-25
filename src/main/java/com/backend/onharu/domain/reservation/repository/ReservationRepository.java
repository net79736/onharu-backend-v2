package com.backend.onharu.domain.reservation.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindAllByStatusParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByChildIdAndStatusFilterParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByChildIdAndStatusParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByOwnerIdAndStatusInParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStatusAndScheduleDateBeforeThanParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStoreIdAndStatusFilterParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStoreIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindUpcomingByChildIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindUpcomingByOwnerIdParam;
import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.GetByStoreScheduleIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.GetReservationByIdParam;
import com.backend.onharu.domain.reservation.model.Reservation;

public interface ReservationRepository {

    /**
     * 예약 저장 및 수정
     */
    Reservation save(Reservation reservation);

    /**
     * 예약 단건 조회
     */
    Reservation getReservation(GetReservationByIdParam param);

    /**
     * 아동 ID로 예약 목록 조회
     */
    Page<Reservation> findByChildIdAndStatusFilter(FindByChildIdAndStatusFilterParam param, Pageable pageable);

    /**
     * 아동 ID로 예약 목록 조회 (List 반환 - COUNT 쿼리 없음)
     */
    List<Reservation> findByChildIdAndStatusFilterAsList(FindByChildIdAndStatusFilterParam param, Pageable pageable);

    /**
     * 아동 ID 기준으로 리뷰가 없는 COMPLETED 예약 목록 조회
     */
    List<Reservation> findCompletedWithoutReviewByChildId(Long childId, Pageable pageable);

    /**
     * 가게 일정 ID로 예약 단건 조회
     */
    Reservation getLatestByStoreScheduleId(GetByStoreScheduleIdParam param);

    /**
     * 해당 가게 일정에 {@code statuses}에 포함된 상태의 예약이 하나라도 있으면 true
     */
    boolean existsByStoreScheduleIdAndStatusIn(Long storeScheduleId, Collection<ReservationType> statuses);

    /**
     * 해당 가게 일정에 연결된 예약 행을 모두 삭제 (스케줄 삭제 전 FK 해소. 활성 예약 없음이 검증된 뒤에만 호출)
     */
    void deleteAllByStoreScheduleId(Long storeScheduleId);

    /**
     * 가게 ID로 예약 목록 조회
     */
    List<Reservation> findByStoreId(FindByStoreIdParam param);

    /**
     * 가게 ID와 상태 필터로 예약 목록 조회 (페이징)
     */
    Page<Reservation> findByStoreIdAndStatusFilter(FindByStoreIdAndStatusFilterParam param, Pageable pageable);


    /**
     * 예약 상태로 예약 목록 조회
     */
    List<Reservation> findAllByStatus(FindAllByStatusParam param);

    /**
     * 아동 ID와 상태로 예약 목록 조회
     */
    List<Reservation> findByChildIdAndStatus(FindByChildIdAndStatusParam param);

    /**
     * 예약 삭제
     */
    void delete(Reservation reservation);

    /**
     * 예약 상태와 일정 날짜 전 예약 목록 조회
     */
    List<Reservation> findByStatusAndScheduleDateBeforeThan(FindByStatusAndScheduleDateBeforeThanParam param);

    /**
     * 사업자 ID와 상태 필터로 예약 목록 조회
     */
    List<Reservation> findByOwnerIdAndStatusIn(FindByOwnerIdAndStatusInParam findByOwnerIdAndStatusFilterParam, Pageable pageable);

    /**
     * 아동 ID 기준 다가오는 예약 조회 (scheduleDate >= fromDate)
     */
    List<Reservation> findUpcomingByChildId(FindUpcomingByChildIdParam param, Pageable pageable);

    /**
     * 사업자 ID 기준 다가오는 예약 조회 (scheduleDate >= fromDate)
     */
    List<Reservation> findUpcomingByOwnerId(FindUpcomingByOwnerIdParam param, Pageable pageable);
}

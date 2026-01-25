package com.backend.onharu.interfaces.api.controller.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IReservationController;
import com.backend.onharu.interfaces.api.dto.ReservationControllerDto.BookStoreRequest;
import com.backend.onharu.interfaces.api.dto.ReservationControllerDto.BookStoreResponse;
import com.backend.onharu.interfaces.api.dto.ReservationControllerDto.GetAvailableDatesResponse;
import com.backend.onharu.interfaces.api.dto.ReservationControllerDto.GetMyBookingDetailResponse;
import com.backend.onharu.interfaces.api.dto.ReservationControllerDto.GetMyBookingListResponse;
import com.backend.onharu.interfaces.api.dto.ReservationControllerDto.GetStoreBookingDetailResponse;
import com.backend.onharu.interfaces.api.dto.ReservationControllerDto.GetStoreBookingListResponse;
import com.backend.onharu.interfaces.api.dto.ReservationControllerDto.RemoveAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.ReservationControllerDto.SetAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.ReservationControllerDto.UpdateAvailableDatesRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 예약 관련 API를 제공하는 컨트롤러 구현체입니다.
 * 
 * 예약 생성, 취소, 조회 및 예약 가능한 날짜 관리 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationControllerImpl implements IReservationController {
    // #########################################################
    // # 아동 예약 관리 API
    // #########################################################

    /**
     * 가게 예약 생성
     * 
     * POST /reservations/stores/{storeId}
     * 새로운 예약을 생성합니다.
     *
     * @param request 예약 생성 요청
     * @return 생성된 예약 정보
     */
    @Override
    @PostMapping("/stores/{storeId}")
    public ResponseEntity<ResponseDTO<BookStoreResponse>> bookStore(
            @PathVariable Long storeId,
            @RequestBody BookStoreRequest request
    ) {
        log.info("예약 생성 요청: {}", request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 취소
     * 
     * DELETE /reservations/{reservationId}
     * 기존 예약을 취소합니다.
     *
     * @param reservationId 예약 ID
     * @return 취소 결과
     */
    @Override
    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<ResponseDTO<Void>> cancelStore(
            @PathVariable Long reservationId
    ) {
        log.info("예약 취소 요청: reservationId={}", reservationId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 신청 목록 조회
     * 
     * GET /reservations/my
     * 내가 신청한 예약 목록을 조회합니다.
     *
     * @return 내 예약 목록
     */
    @Override
    @GetMapping("/my")
    public ResponseEntity<ResponseDTO<GetMyBookingListResponse>> getMyBookings() {
        log.info("예약 신청 목록 조회 요청");
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 신청 상세 조회
     * 
     * GET /reservations/{reservationId}/my
     * 내가 신청한 특정 예약의 상세 정보를 조회합니다.
     *
     * @param reservationId 예약 ID
     * @return 예약 상세 정보
     */
    @Override
    @GetMapping("/{reservationId}/my")
    public ResponseEntity<ResponseDTO<GetMyBookingDetailResponse>> getMyBooking(
            @PathVariable Long reservationId
    ) {
        log.info("예약 신청 상세 조회 요청: reservationId={}", reservationId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    // #########################################################
    // # 사업자 예약 관리 API
    // #########################################################

    /**
     * 예약 관리 목록 조회
     * 
     * GET /reservations/manage
     * 사업자의 예약 목록을 조회합니다.
     *
     * @return 예약 관리 목록
     */
    @Override
    @GetMapping("/manage")
    public ResponseEntity<ResponseDTO<GetStoreBookingListResponse>> getStoreBookings() {
        log.info("예약 관리 목록 조회 요청");
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 관리 상세 조회
     * 
     * GET /reservations/store/{reservationId}
     * 사업자의 특정 예약의 상세 정보를 조회합니다.
     *
     * @param reservationId 예약 ID
     * @return 예약 상세 정보
     */
    @Override
    @GetMapping("/{reservationId}/manage")
    public ResponseEntity<ResponseDTO<GetStoreBookingDetailResponse>> getStoreBooking(
            @PathVariable Long reservationId
    ) {
        log.info("예약 관리 상세 조회 요청: reservationId={}", reservationId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 승인
     * 
     * POST /reservations/{reservationId}/approve
     * 사업자가 예약을 승인합니다.
     *
     * @param reservationId 예약 ID
     * @return 승인 결과
     */
    @Override
    @PostMapping("/{reservationId}/approve")
    public ResponseEntity<ResponseDTO<Void>> approveBook(
            @PathVariable Long reservationId
    ) {
        log.info("예약 승인 요청: reservationId={}", reservationId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 거절
     * 
     * POST /reservations/{reservationId}/reject
     * 사업자가 예약을 거절합니다.
     *
     * @param reservationId 예약 ID
     * @return 거절 결과
     */
    @Override
    @PostMapping("/{reservationId}/reject")
    public ResponseEntity<ResponseDTO<Void>> rejectBook(
            @PathVariable Long reservationId
    ) {
        log.info("예약 거절 요청: reservationId={}", reservationId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 가능한 날짜 생성
     * 
     * POST /reservations/stores/{storeId}/available-dates
     * 예약 가능한 날짜를 생성합니다.
     *
     * @param storeId 가게 ID
     * @param request 예약 가능한 날짜 생성 요청
     * @return 생성 결과
     */
    @Override
    @PostMapping("/stores/{storeId}/available-dates")
    public ResponseEntity<ResponseDTO<Void>> setAvailableDates(
            @PathVariable Long storeId,
            @RequestBody SetAvailableDatesRequest request
    ) {
        log.info("예약 가능한 날짜 생성 요청: storeId={}, request={}", storeId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 가능한 날짜 수정
     * 
     * PUT /reservations/stores/{storeId}/available-dates
     * 예약 가능한 날짜를 수정합니다.
     *
     * @param storeId 가게 ID
     * @param request 예약 가능한 날짜 수정 요청
     * @return 수정 결과
     */
    @Override
    @PutMapping("/stores/{storeId}/available-dates")
    public ResponseEntity<ResponseDTO<Void>> updateAvailableDates(
            @PathVariable Long storeId,
            @RequestBody UpdateAvailableDatesRequest request
    ) {
        log.info("예약 가능한 날짜 수정 요청: storeId={}, request={}", storeId, request);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 가능한 날짜 삭제
     * 
     * DELETE /reservations/stores/{storeId}/available-dates
     * 예약 가능한 날짜를 삭제합니다.
     *
     * @param storeId 가게 ID
     * @param request 예약 가능한 날짜 삭제 요청
     * @return 삭제 결과
     */
    @Override
    @DeleteMapping("/stores/{storeId}/available-dates")
    public ResponseEntity<ResponseDTO<Void>> removeAvailableDates(
            @PathVariable Long storeId,
            @RequestBody RemoveAvailableDatesRequest request
    ) {
        log.info("예약 가능한 날짜 삭제 요청: storeId={}, request={}", storeId, request);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 예약 가능한 날짜 조회
     * 
     * GET /reservations/stores/{storeId}/available-dates
     * 예약 가능한 날짜를 조회합니다.
     *
     * @param storeId 가게 ID
     * @return 예약 가능한 날짜 목록
     */
    @Override
    @GetMapping("/stores/{storeId}/available-dates")
    public ResponseEntity<ResponseDTO<GetAvailableDatesResponse>> getAvailableDates(
            @PathVariable Long storeId
    ) {
        log.info("예약 가능한 날짜 조회 요청: storeId={}", storeId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }
}

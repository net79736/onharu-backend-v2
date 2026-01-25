package com.backend.onharu.interfaces.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reservation", description = "예약 API")
public interface IReservationController {

    @Operation(summary = "가게 예약 생성", description = "가게에 예약을 생성합니다.")
    ResponseEntity<ResponseDTO<BookStoreResponse>> bookStore(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @Schema(description = "가게 예약 생성 요청")
            BookStoreRequest request
    );

    @Operation(summary = "예약 취소", description = "기존 예약을 취소합니다.")
    ResponseEntity<ResponseDTO<Void>> cancelStore(
            @Schema(description = "예약 ID", example = "1")
            Long reservationId
    );

    @Operation(summary = "예약 신청 목록 조회", description = "내가 신청한 예약 목록을 조회합니다.")
    ResponseEntity<ResponseDTO<GetMyBookingListResponse>> getMyBookings();

    @Operation(summary = "예약 신청 상세 조회", description = "내가 신청한 특정 예약의 상세 정보를 조회합니다.")
    ResponseEntity<ResponseDTO<GetMyBookingDetailResponse>> getMyBooking(
            @Schema(description = "예약 ID", example = "1")
            Long reservationId
    );

    @Operation(summary = "예약 관리 목록 조회", description = "사업자의 예약 목록을 조회합니다.")
    ResponseEntity<ResponseDTO<GetStoreBookingListResponse>> getStoreBookings();

    @Operation(summary = "예약 관리 상세 조회", description = "사업자의 특정 예약의 상세 정보를 조회합니다.")
    ResponseEntity<ResponseDTO<GetStoreBookingDetailResponse>> getStoreBooking(
            @Schema(description = "예약 ID", example = "1")
            Long reservationId
    );

    @Operation(summary = "예약 승인", description = "사업자가 예약을 승인합니다.")
    ResponseEntity<ResponseDTO<Void>> approveBook(
            @Schema(description = "예약 ID", example = "1")
            Long reservationId
    );

    @Operation(summary = "예약 거절", description = "사업자가 예약을 거절합니다.")
    ResponseEntity<ResponseDTO<Void>> rejectBook(
            @Schema(description = "예약 ID", example = "1")
            Long reservationId
    );

    @Operation(summary = "예약 가능한 날짜 생성", description = "예약 가능한 날짜를 생성합니다.")
    ResponseEntity<ResponseDTO<Void>> setAvailableDates(
            @Schema(description = "가게 ID", example = "1")
            @RequestParam Long storeId,
            @Schema(description = "예약 가능한 날짜 생성 요청")
            SetAvailableDatesRequest request
    );

    @Operation(summary = "예약 가능한 날짜 수정", description = "예약 가능한 날짜를 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateAvailableDates(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @Schema(description = "예약 가능한 날짜 수정 요청")
            UpdateAvailableDatesRequest request
    );
    
    @Operation(summary = "예약 가능한 날짜 삭제", description = "예약 가능한 날짜를 삭제합니다.")
    ResponseEntity<ResponseDTO<Void>> removeAvailableDates(
            @Schema(description = "가게 ID", example = "1")
            @RequestParam Long storeId,
            @Schema(description = "예약 가능한 날짜 삭제 요청")
            RemoveAvailableDatesRequest request
    );

    @Operation(summary = "예약 가능한 날짜 조회", description = "예약 가능한 날짜를 조회합니다.")
    ResponseEntity<ResponseDTO<GetAvailableDatesResponse>> getAvailableDates(
            @Schema(description = "가게 ID", example = "1")
            Long storeId
    );

}

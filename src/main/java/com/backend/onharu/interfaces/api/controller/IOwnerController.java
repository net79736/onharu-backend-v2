package com.backend.onharu.interfaces.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.CreateOwnerRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.CreateOwnerResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetOwnerResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.UpdateOwnerRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Owner", description = "사업주 API")
public interface IOwnerController {

    @Operation(summary = "사업자 정보 등록", description = "사업자 정보를 등록합니다.")
    ResponseEntity<ResponseDTO<CreateOwnerResponse>> registerBusiness(
            @Schema(description = "사업자 정보 등록 요청")
            CreateOwnerRequest request,
            @Schema(description = "사업자 등록 서류 파일")
            MultipartFile businessRegistrationFile
    );

    @Operation(summary = "사업자 정보 수정", description = "사업자 정보를 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateBusiness(
            @Schema(description = "사업자 ID", example = "1")
            Long ownerId,
            @Schema(description = "사업자 정보 수정 요청")
            UpdateOwnerRequest request,
            @Schema(description = "사업자 등록 서류 파일")
            MultipartFile businessRegistrationFile
    );

    @Operation(summary = "사업자 정보 삭제", description = "사업자 정보를 삭제합니다.")
    ResponseEntity<ResponseDTO<Void>> closeBusiness(
            @Schema(description = "사업자 ID", example = "1")
            Long ownerId
    );

    @Operation(summary = "사업자 정보 조회", description = "사업자 정보를 조회합니다.")
    ResponseEntity<ResponseDTO<GetOwnerResponse>> getMyBusiness(
            @Schema(description = "사업자 ID", example = "1")
            Long ownerId
    );
}

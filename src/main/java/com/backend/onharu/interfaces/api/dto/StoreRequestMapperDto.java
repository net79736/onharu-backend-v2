package com.backend.onharu.interfaces.api.dto;

import java.util.List;

import com.backend.onharu.domain.file.dto.FileCommand.ImageMetadata;
import com.backend.onharu.domain.store.dto.StoreCommand.CreateStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.UpdateStoreCommand;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.ImageMetadataRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.OpenStoreRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.UpdateStoreRequest;

/**
 * API Request DTO를 도메인 Command로 변환하는 매퍼.
 * 이미지 목록 변환 등 컨트롤러에 두기 애매한 변환 로직을 한 곳에서 관리합니다.
 */
public final class StoreRequestMapperDto {

    private StoreRequestMapperDto() {
    }

    /**
     * OpenStoreRequest → CreateStoreCommand 변환.
     * images가 null 또는 비어 있으면 command의 images는 null로 설정됩니다.
     */
    public static CreateStoreCommand toCreateStoreCommand(OpenStoreRequest request, Long ownerId) {
        return new CreateStoreCommand(
                ownerId,
                request.categoryId(),
                request.name(),
                request.address(),
                request.phone(),
                request.lat(),
                request.lng(),
                request.intro(),
                request.introduction(),
                request.tagNames(),
                request.businessHours(),
                toImageMetadataList(request.images())
        );
    }

    /**
     * UpdateStoreRequest → UpdateStoreCommand 변환.
     * images가 null 또는 비어 있으면 command의 images는 null로 설정됩니다.
     */
    public static UpdateStoreCommand toUpdateStoreCommand(Long storeId, UpdateStoreRequest request) {
        return new UpdateStoreCommand(
                storeId,
                request.categoryId(),
                request.address(),
                request.phone(),
                request.lat(),
                request.lng(),
                request.introduction(),
                request.intro(),
                request.isOpen(),
                request.tagNames(),
                request.businessHours(),
                toImageMetadataList(request.images())
        );
    }

    /**
     * API 이미지 메타데이터 목록을 도메인 ImageMetadata 목록으로 변환.
     * null 또는 빈 목록이면 null을 반환합니다.
     */
    public static List<ImageMetadata> toImageMetadataList(List<ImageMetadataRequest> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .map(img -> new ImageMetadata(img.fileKey(), img.filePath(), img.displayOrder()))
                .toList();
    }
}

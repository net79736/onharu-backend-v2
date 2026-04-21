package com.backend.onharu.interfaces.api.dto;

import static com.backend.onharu.interfaces.api.common.dto.ImageMetadataRequest.toImageMetadataList;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import com.backend.onharu.domain.common.enums.WeekType;
import com.backend.onharu.domain.store.dto.StoreCacheDto;
import com.backend.onharu.domain.store.dto.StoreCommand.CreateStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.UpdateStoreCommand;
import com.backend.onharu.domain.store.model.BusinessHours;
import com.backend.onharu.domain.store.model.StoreTag;
import com.backend.onharu.domain.tag.model.Tag;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.BusinessHourResponse;
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
                request.introduction(),
                request.intro(),
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
                request.isSharing(),
                request.tagNames(),
                request.businessHours(),
                toImageMetadataList(request.images())
        );
    }

    /**
     * BusinessHours 목록을 BusinessHourResponse 목록으로 변환.
     * null 또는 빈 목록이면 빈 리스트를 반환합니다.
     */
    public static List<BusinessHourResponse> toBusinessHourResponses(List<BusinessHours> businessHours) {
        if (businessHours == null || businessHours.isEmpty()) {
            return List.of();
        }
        return businessHours.stream()
                .map(bh -> new BusinessHourResponse(
                    bh.getBusinessDay(),
                    bh.getOpenTime(),
                    bh.getCloseTime()
                ))
                .toList();
    }

    /**
     * StoreCacheDto.BusinessHoursDto 목록을 BusinessHourResponse 목록으로 변환.
     * null 또는 빈 목록이면 빈 리스트를 반환합니다.
     */
    public static List<BusinessHourResponse> toBusinessHourResponsesFromCache(List<StoreCacheDto.BusinessHoursDto> businessHours) {
        if (businessHours == null || businessHours.isEmpty()) {
            return List.of();
        }
        return businessHours.stream()
                .map(bh -> new BusinessHourResponse(
                        bh.getBusinessDay() != null ? WeekType.valueOf(bh.getBusinessDay()) : null,
                        bh.getOpenTime() != null ? LocalTime.parse(bh.getOpenTime()) : null,
                        bh.getCloseTime() != null ? LocalTime.parse(bh.getCloseTime()) : null
                ))
                .toList();
    }

    /**
     * StoreTag 목록을 태그 이름 목록으로 변환.
     * null 또는 빈 목록이면 빈 리스트를 반환합니다.
     */
    public static List<String> toTagNames(List<StoreTag> storeTags) {
        if (storeTags == null || storeTags.isEmpty()) {
            return List.of();
        }
        return storeTags.stream()
                .map(StoreTag::getTag)
                .map(Tag::getName)
                .toList();
    }

    /**
     * StoreCacheDto.TagDto 목록을 태그 이름 목록으로 변환.
     * null 또는 빈 목록이면 빈 리스트를 반환합니다.
     */
    public static List<String> toTagNamesFromCache(List<StoreCacheDto.TagDto> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .map(StoreCacheDto.TagDto::getName)
                .toList();
    }
}

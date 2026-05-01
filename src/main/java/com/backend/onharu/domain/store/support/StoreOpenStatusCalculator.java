package com.backend.onharu.domain.store.support;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.backend.onharu.domain.common.enums.WeekType;
import com.backend.onharu.domain.store.dto.StoreCacheDto;
import com.backend.onharu.domain.store.model.BusinessHours;
import com.backend.onharu.domain.store.model.Store;

/**
 * 현재 시각 기준으로 가게의 "영업중" 여부를 계산합니다.
 *
 * <p>
 * 규칙(기본):
 * - Store.isOpen 이 false 이면 무조건 영업중=false (사업자가 수동으로 닫은 상태)
 * - 오늘 요일의 BusinessHours 가 없으면 영업중=false
 * - openTime < closeTime  : 같은 날 구간(open~close)
 * - openTime >= closeTime : (자정 넘김/24시간 등) 케이스는 아직 고려하지 않음 → 영업중=false
 * </p>
 */
public final class StoreOpenStatusCalculator {

    private StoreOpenStatusCalculator() {
    }

    /**
     * 현재 시각 기준으로 가게의 "영업중" 여부를 계산합니다.
     * @param store 가게
     * @param now 현재 시각
     * @return 영업중 여부
     */
    public static boolean isOpenNow(Store store, LocalDateTime now) {
        if (store == null) return false;        
        return isOpenNow(store.getIsOpen(), store.getBusinessHours(), now);
    }

    /**
     * 현재 시각 기준으로 가게의 "영업중" 여부를 계산합니다.
     * 
     * @param manualOpenFlag 사업자가 설정한 영업 상태
     * @param businessHours 가게의 영업시간
     * @param now 현재 시각
     * @return 영업중 여부
     */
    public static boolean isOpenNow(Boolean manualOpenFlag, List<BusinessHours> businessHours, LocalDateTime now) {
        if (!Boolean.TRUE.equals(manualOpenFlag)) return false;
        if (now == null) return false;
        if (businessHours == null || businessHours.isEmpty()) return false;

        // 1) 오늘 영업시간으로 판정
        WeekType today = toWeekType(now.getDayOfWeek());
        return isOpenByDay(businessHours, today, now);
    }

    /**
     * (캐시 DTO용) 현재 시각 기준으로 가게의 "영업중" 여부를 계산합니다.
     *
     * @param manualOpenFlag 사업자가 설정한 영업 상태
     * @param businessHours 캐시 DTO의 영업시간
     * @param now 현재 시각
     * @return 영업중 여부
     */
    public static boolean isOpenNowFromCache(Boolean manualOpenFlag, List<StoreCacheDto.BusinessHoursDto> businessHours, LocalDateTime now) {
        if (!Boolean.TRUE.equals(manualOpenFlag)) return false;
        if (now == null) return false;
        if (businessHours == null || businessHours.isEmpty()) return false;

        WeekType today = toWeekType(now.getDayOfWeek());
        LocalTime t = now.toLocalTime();

        return businessHours.stream()
                .map(StoreOpenStatusCalculator::safeParseCachedBusinessHours)
                .filter(h -> h != null)
                .filter(h -> h.businessDay == today)
                .filter(h -> h.openTime != null && h.closeTime != null)
                .filter(h -> h.openTime.isBefore(h.closeTime))
                .anyMatch(h -> !t.isBefore(h.openTime) && t.isBefore(h.closeTime));
    }

    /**
     * 오늘 영업시간으로 판정합니다.
     * 
     * @param businessHours 가게의 영업시간
     * @param day 오늘 요일
     * @param baseDate 현재 시각 (yyyy-MM-dd)
     * @param now 현재 시각 (yyyy-MM-dd HH:mm:ss)
     * @return 영업중 여부
     */
    private static boolean isOpenByDay(List<BusinessHours> businessHours, WeekType day, LocalDateTime now) {
        return businessHours.stream()
                .filter(bh -> bh.getBusinessDay() == day) // 영업 요일 판정
                .filter(bh -> bh.getOpenTime() != null && bh.getCloseTime() != null) // 영업 시간 Validation 체크
                .filter(bh -> bh.getOpenTime().isBefore(bh.getCloseTime())) // 영업 시간 Validation 체크
                .anyMatch(bh -> {
                    var t = now.toLocalTime();
                    // 현재 시간이 openTime 이후, closeTime 이전인지 판정
                    return !t.isBefore(bh.getOpenTime()) && t.isBefore(bh.getCloseTime());
                });
    }

    /**
     * (캐시 DTO용) 영업시간을 파싱합니다.
     * 
     * @param bh 캐시 DTO의 영업시간
     * @return 캐시 DTO의 영업시간
     */
    private static CachedBusinessHours safeParseCachedBusinessHours(StoreCacheDto.BusinessHoursDto bh) {
        if (bh == null) return null;
        WeekType day = safeWeekType(bh.getBusinessDay());
        LocalTime open = safeLocalTime(bh.getOpenTime());
        LocalTime close = safeLocalTime(bh.getCloseTime());
        if (day == null || open == null || close == null) return null;
        return new CachedBusinessHours(day, open, close);
    }

    private static WeekType safeWeekType(String businessDay) {
        if (businessDay == null || businessDay.isBlank()) return null;
        try {
            return WeekType.valueOf(businessDay);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static LocalTime safeLocalTime(String time) {
        if (time == null || time.isBlank()) return null;
        try {
            return LocalTime.parse(time);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static final class CachedBusinessHours {
        private final WeekType businessDay;
        private final LocalTime openTime;
        private final LocalTime closeTime;

        private CachedBusinessHours(WeekType businessDay, LocalTime openTime, LocalTime closeTime) {
            this.businessDay = businessDay;
            this.openTime = openTime;
            this.closeTime = closeTime;
        }
    }

    private static WeekType toWeekType(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> WeekType.MON;
            case TUESDAY -> WeekType.TUE;
            case WEDNESDAY -> WeekType.WED;
            case THURSDAY -> WeekType.THU;
            case FRIDAY -> WeekType.FRI;
            case SATURDAY -> WeekType.SAT;
            case SUNDAY -> WeekType.SUN;
        };
    }
}
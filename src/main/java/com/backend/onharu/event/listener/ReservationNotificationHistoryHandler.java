package com.backend.onharu.event.listener;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.child.dto.ChildRepositoryParam.GetChildByIdParam;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.repository.ChildRepository;
import com.backend.onharu.domain.common.enums.NotificationHistoryType;
import com.backend.onharu.domain.notification.model.NotificationHistory;
import com.backend.onharu.domain.notification.repository.NotificationHistoryRepository;
import com.backend.onharu.domain.owner.dto.OwnerRepositoryParam.GetOwnerByIdParam;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.repository.OwnerRepository;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.GetReservationByIdParam;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.reservation.repository.ReservationRepository;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.event.ReservationNotificationMessage;
import com.backend.onharu.event.model.ReservationEvent;
import com.backend.onharu.infra.db.notification.NotificationHistoryJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 예약 이벤트 → 알림 히스토리(notification_histories) 저장하는 핸들러
 *
 * 주의: 소비자(이벤트 리스너/Rabbit consumer)에서 여러 번 호출될 수 있으니, 동일 이벤트에 대해 중복 저장되지 않도록 처리해야 합니다(멱등성 보장).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationNotificationHistoryHandler {
    private static final String CANCEL_REASON_PREFIX = "\n취소사유: ";
    private static final String RELATED_ENTITY_TYPE = Reservation.class.getSimpleName();

    private final NotificationHistoryRepository notificationHistoryRepository;
    private final NotificationHistoryJpaRepository notificationHistoryJpaRepository;
    private final OwnerRepository ownerRepository;
    private final ChildRepository childRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 예약 이벤트를 받아 알림 히스토리(notification_histories) 테이블에 저장하는 핸들러
     * 
     * @param event
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReservationEvent(ReservationEvent event) {
        log.info("Reservation notification handle start. reservationId: {}, type: {}", event.reservationId(), event.type());

        Reservation reservation = reservationRepository.getReservation(new GetReservationByIdParam(event.reservationId()));
        ReservationNotificationMessage msg = ReservationNotificationMessage.from(event.type());
        String cancelReasonSuffix = buildCancelReasonSuffix(event.type(), reservation);
        String storeName = reservation.getStoreSchedule().getStore().getName();

        saveOwnerNotification(event, msg, cancelReasonSuffix, storeName);
        saveChildNotification(event, msg, cancelReasonSuffix, storeName);
    }

    /**
     * 예약 취소 등 알림에 표시할 취소사유 문자열 생성
     * 
     * @param type
     * @param reservation
     * @return
     */
    private String buildCancelReasonSuffix(NotificationHistoryType type, Reservation reservation) {
        if (!type.requiresCancelReason()) {
            return "";
        }
        String reason = reservation.getCancelReason();
        return StringUtils.isNotEmpty(reason) ? CANCEL_REASON_PREFIX + reason : "";
    }

    /**
     * 사장용 알림 저장
     * 
     * @param event
     * @param msg
     * @param cancelReasonSuffix
     * @param storeName
     */
    private void saveOwnerNotification(ReservationEvent event, ReservationNotificationMessage msg, String cancelReasonSuffix, String storeName) {
        String ownerMessage = msg.getOwnerMessageTemplate(storeName, event.reservationId());
        if (StringUtils.isEmpty(ownerMessage)) {
            return;
        }

        Owner owner = ownerRepository.getOwnerById(new GetOwnerByIdParam(event.ownerId()));
        saveNotificationIfAbsent(owner.getUser(), event.type(), "매장 관리 알림", ownerMessage + cancelReasonSuffix, event.reservationId());
    }

    /**
     * 아동용 알림 저장
     * 
     * @param event
     * @param msg
     * @param cancelReasonSuffix
     * @param storeName
     */
    private void saveChildNotification(ReservationEvent event, ReservationNotificationMessage msg, String cancelReasonSuffix, String storeName) {
        String childMessage = msg.getChildMessageTemplate(storeName);
        if (StringUtils.isEmpty(childMessage)) {
            return;
        }

        Child child = childRepository.getChildById(new GetChildByIdParam(event.childId()));
        saveNotificationIfAbsent(child.getUser(), event.type(), "예약 안내 소식", childMessage + cancelReasonSuffix, event.reservationId());
    }

    /**
     * 알림 히스토리 저장(중복 방지)
     * 
     * @param user
     * @param type
     * @param title
     * @param message
     * @param relatedEntityId
     */
    private void saveNotificationIfAbsent(User user, NotificationHistoryType type, String title, String message, Long relatedEntityId) {
        Long userId = user.getId();
        if (userId == null) {
            return;
        }
        // 알림 히스토리 존재 여부 조회
        boolean exists = notificationHistoryJpaRepository.existsNotificationHistory(
                userId,
                type,
                RELATED_ENTITY_TYPE,
                relatedEntityId
        );
        
        if (exists) {
            log.info("Reservation notification skip (idempotent). userId={} type={} relatedEntityId={}", userId, type, relatedEntityId);
            return; // 중복 저장 건너뛰기
        }

        // 알림 히스토리 저장
        notificationHistoryRepository.save(NotificationHistory.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .relatedEntityType(RELATED_ENTITY_TYPE)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .build());
    }
}


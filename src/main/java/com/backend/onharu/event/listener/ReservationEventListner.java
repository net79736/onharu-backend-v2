package com.backend.onharu.event.listener;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListner {
    // 취소 사유 접두사
    private static final String CANCEL_REASON_PREFIX = "\n취소사유: ";

    private final NotificationHistoryRepository notificationHistoryRepository;
    private final OwnerRepository ownerRepository;
    private final ChildRepository childRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationEvent(ReservationEvent event) {
        log.info("Current Thread: {}", Thread.currentThread().getName());
        log.info("Notification process start. reservationId: {}, type: {}", event.reservationId(), event.type());

        Reservation reservation = reservationRepository.getReservation(new GetReservationByIdParam(event.reservationId()));
        ReservationNotificationMessage msg = ReservationNotificationMessage.from(event.type());
        String cancelReasonSuffix = buildCancelReasonSuffix(event.type(), reservation);
        String storeName = reservation.getStoreSchedule().getStore().getName();

        saveOwnerNotification(event, msg, cancelReasonSuffix, storeName);
        saveChildNotification(event, msg, cancelReasonSuffix, storeName);
    }

    /**
     * 취소 사유 메시지 생성
     * 
     * @param type 알림 타입
     * @param reservation 예약 정보
     * @return 취소 사유 메시지
     */
    private String buildCancelReasonSuffix(NotificationHistoryType type, Reservation reservation) {
        // 취소 또는 거절 이벤트에서만 취소 사유를 포함
        if (!type.requiresCancelReason()) return "";

        String reason = reservation.getCancelReason(); // 취소 사유
        return StringUtils.isNotEmpty(reason) ? CANCEL_REASON_PREFIX + reason : "";
    }

    /**
     * 사장용 알림 저장
     * 
     * @param event 예약 이벤트
     * @param msg 예약 알림 메시지
     * @param cancelReasonSuffix 취소 사유 메시지
     */
    private void saveOwnerNotification(ReservationEvent event, ReservationNotificationMessage msg, String cancelReasonSuffix, String storeName) {
        String ownerMessage = msg.getOwnerMessageTemplate(storeName, event.reservationId());
        if (StringUtils.isEmpty(ownerMessage)) return;

        Owner owner = ownerRepository.getOwnerById(new GetOwnerByIdParam(event.ownerId()));
        log.info("Owner notification saved. reservationId: {}, ownerId: {}", event.reservationId(), event.ownerId());

        saveNotification(owner.getUser(), event.type(), "매장 관리 알림", ownerMessage + cancelReasonSuffix, event.reservationId());
    }

    /**
     * 아동용 알림 저장
     * 
     * @param event 예약 이벤트
     * @param msg 예약 알림 메시지
     * @param cancelReasonSuffix 취소 사유 메시지
     */
    private void saveChildNotification(ReservationEvent event, ReservationNotificationMessage msg, String cancelReasonSuffix, String storeName) {
        String childMessage = msg.getChildMessageTemplate(storeName);
        if (StringUtils.isEmpty(childMessage)) return;

        Child child = childRepository.getChildById(new GetChildByIdParam(event.childId()));
        log.info("Child notification saved. reservationId: {}, childId: {}", event.reservationId(), event.childId());

        saveNotification(child.getUser(), event.type(), "예약 안내 소식", childMessage + cancelReasonSuffix, event.reservationId());
    }

    /**
     * 알림 저장
     * 
     * @param user 사용자
     * @param type 알림 타입
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param relatedEntityId 관련 엔티티 ID
     */
    private void saveNotification(User user, NotificationHistoryType type, String title, String message, Long relatedEntityId) {
        notificationHistoryRepository.save(NotificationHistory.builder()
            .user(user)
            .type(type)
            .title(title)
            .message(message)
            .relatedEntityType(Reservation.class.getSimpleName())
            .relatedEntityId(relatedEntityId)
            .isRead(false)
            .build());
    }
}

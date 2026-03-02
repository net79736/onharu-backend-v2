package com.backend.onharu.application;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.service.ChildQueryService;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.reservation.service.ReservationQueryService;
import com.backend.onharu.domain.review.dto.ReviewCommand.CreateReviewCommand;
import com.backend.onharu.domain.review.dto.ReviewCommand.UpdateReviewCommand;
import com.backend.onharu.domain.review.model.Review;
import com.backend.onharu.domain.review.service.ReviewCommandService;
import com.backend.onharu.domain.review.service.ReviewQueryService;
import com.backend.onharu.domain.store.dto.StoreQuery;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.service.StoreQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.backend.onharu.domain.child.dto.ChildQuery.GetChildByIdQuery;
import static com.backend.onharu.domain.reservation.dto.ReservationQuery.GetReservationByIdQuery;
import static com.backend.onharu.domain.review.dto.ReviewCommand.DeleteReviewCommand;
import static com.backend.onharu.domain.review.dto.ReviewQuery.*;

@Component
@RequiredArgsConstructor
public class ReviewFacade {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;
    private final ChildQueryService childQueryService;
    private final StoreQueryService storeQueryService;
    private final ReservationQueryService reservationQueryService;

    /**
     * 리뷰 생성(등록)
     *
     * @param command 아동 ID, 가게 ID, 예약 ID 와 리뷰 내용이 포함된 command
     * @return 생성한 리뷰 엔티티
     */
    public Review createReview(CreateReviewCommand command) {

        Child child = childQueryService.getChildById(new GetChildByIdQuery(command.childId()));// 리뷰를 작성할 아동 조회
        Store store = storeQueryService.getStoreById(new StoreQuery.GetStoreByIdQuery(command.storeId())); // 리뷰가 달릴 가게 조회
        Reservation reservation = reservationQueryService.getReservation(new GetReservationByIdQuery(command.reservationId())); // 리뷰할 예약 조회

        reservation.verifyWriteable(child.getId()); // 리뷰를 작성할 수 있는 예약인지 확인

        return reviewCommandService.createReview(command, child, store, reservation); // 리뷰 생성 및 반환
    }

    /**
     * 전체 리뷰 목록 조회(페이징)
     * @return 조회된 리뷰 목록
     */
    public Page<Review> findAll(Pageable pageable) {
        return reviewQueryService.findAll(pageable);
    }

    /**
     * 본인(아동)이 작성한 리뷰 목록 조회
     * @param query 아동 ID 가 포함된 query
     * @return 조회된 리뷰 목록
     */
    public List<Review> findAllByChildId(FindAllByChildIdQuery query) {
        return reviewQueryService.findAllByChildId(query);
    }

    /**
     * 본인(아동)이 작성한 리뷰 목록 조회(페이징)
     * @param query 아동 ID 가 포함된 query
     * @param pageable 페이징 정보
     * @return 조회된 리뷰 목록
     */
    public Page<Review> findByChildId(FindAllByChildIdQuery query, Pageable pageable) {
        return reviewQueryService.findByChildId(query, pageable);
    }

    /**
     * 가게에 달린 리뷰 목록 조회
     * @param query 가게 ID 가 포함된 query
     * @return 조회된 리뷰 목록
     */
    public List<Review> findAllByStoreId(findAllByStoreIdQuery query) {
        return reviewQueryService.findAllByStoreId(query);
    }

    /**
     * 가게에 달린 리뷰 목록 조회(페이징)
     * @param query 가게 ID 가 포함된 query
     * @param pageable 페이징 정보
     * @return 조회된 리뷰 목록
     */
    public Page<Review> findByStoreId(findAllByStoreIdQuery query, Pageable pageable) {
        return reviewQueryService.findByStoreId(query, pageable);
    }

    /**
     * 리뷰 수정
     * @param command 리뷰 ID 와 리뷰 내용이 포함된 command
     */
    public void updateReview(UpdateReviewCommand command) {
        reviewCommandService.updateReview(command); // 리뷰 업데이트
    }

    /**
     * 리뷰 삭제
     * @param command 삭제할 리뷰 ID 가 포함된 command
     */
    public void deleteReview(DeleteReviewCommand command) {
        reviewCommandService.deleteReview(command); // 리뷰 삭제
    }
}

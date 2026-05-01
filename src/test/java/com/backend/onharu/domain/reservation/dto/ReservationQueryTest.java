package com.backend.onharu.domain.reservation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindAllByStatusQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindAllByStoreIdAndStatusQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByChildIdAndStatusFilterQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByChildIdAndStatusQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByStoreIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetByStoreScheduleIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetReservationByIdQuery;

@DisplayName("ReservationQuery 단위 테스트")
class ReservationQueryTest {

    @Nested
    @DisplayName("GetReservationByIdQuery 생성자 테스트")
    class GetReservationByIdQueryTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        void shouldCreateGetReservationByIdQuery() {
            // given
            Long reservationId = 1L;

            // when
            GetReservationByIdQuery query = new GetReservationByIdQuery(reservationId);

            // then
            assertThat(query.reservationId()).isEqualTo(reservationId);
        }
    }

    @Nested
    @DisplayName("FindAllByChildIdQuery 생성자 테스트")
    class FindAllByChildIdQueryTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        void shouldCreateFindAllByChildIdQuery() {
            // given
            Long childId = 855L;

            // when
            FindByChildIdAndStatusFilterQuery query = new FindByChildIdAndStatusFilterQuery(childId, List.of());

            // then
            assertThat(query.childId()).isEqualTo(childId);
        }
    }

    @Nested
    @DisplayName("FindAllByStoreScheduleIdQuery 생성자 테스트")
    class FindAllByStoreScheduleIdQueryTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        void shouldCreateFindAllByStoreScheduleIdQuery() {
            // given
            Long storeScheduleId = 1L;

            // when
            GetByStoreScheduleIdQuery query = new GetByStoreScheduleIdQuery(storeScheduleId);

            // then
            assertThat(query.storeScheduleId()).isEqualTo(storeScheduleId);
        }
    }

    @Nested
    @DisplayName("FindAllByStoreIdQuery 생성자 테스트")
    class FindAllByStoreIdQueryTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        void shouldCreateFindAllByStoreIdQuery() {
            // given
            Long storeId = 1L;

            // when
            FindByStoreIdQuery query = new FindByStoreIdQuery(storeId);

            // then
            assertThat(query.storeId()).isEqualTo(storeId);
        }
    }

    @Nested
    @DisplayName("FindAllByStatusQuery 생성자 테스트")
    class FindAllByStatusQueryTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        void shouldCreateFindAllByStatusQuery() {
            // given
            ReservationType status = ReservationType.WAITING;

            // when
            FindAllByStatusQuery query = new FindAllByStatusQuery(status);

            // then
            assertThat(query.status()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("FindAllByChildIdAndStatusQuery 생성자 테스트")
    class FindAllByChildIdAndStatusQueryTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        void shouldCreateFindAllByChildIdAndStatusQuery() {
            // given
            Long childId = 855L;
            ReservationType status = ReservationType.WAITING;

            // when
            FindByChildIdAndStatusQuery query = new FindByChildIdAndStatusQuery(childId, status);

            // then
            assertThat(query.childId()).isEqualTo(childId);
            assertThat(query.status()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("FindAllByStoreIdAndStatusQuery 생성자 테스트")
    class FindAllByStoreIdAndStatusQueryTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        void shouldCreateFindAllByStoreIdAndStatusQuery() {
            // given
            Long storeId = 1L;
            ReservationType status = ReservationType.WAITING;

            // when
            FindAllByStoreIdAndStatusQuery query = new FindAllByStoreIdAndStatusQuery(storeId, status);

            // then
            assertThat(query.storeId()).isEqualTo(storeId);
            assertThat(query.status()).isEqualTo(status);
        }
    }
}

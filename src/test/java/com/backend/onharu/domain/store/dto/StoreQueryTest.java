package com.backend.onharu.domain.store.dto;

import static com.backend.onharu.domain.store.dto.StoreQuery.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.backend.onharu.domain.store.dto.StoreQuery.FindByCategoryIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.FindByNameQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.FindWithCategoryAndFavoriteCountByOwnerIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreByIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.SearchStoresQuery;

@DisplayName("StoreQuery 단위 테스트")
class StoreQueryTest {

    @Nested
    @DisplayName("GetStoreByIdQuery 생성자 테스트")
    class GetStoreByIdQueryTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        void shouldCreateGetStoreByIdQuery() {
            // given
            Long storeId = 1L;

            // when
            GetStoreByIdQuery query = new GetStoreByIdQuery(storeId);

            // then
            assertThat(query.storeId()).isEqualTo(storeId);
        }
    }

    @Nested
    @DisplayName("SearchStoresQuery 생성자 테스트")
    class SearchStoresQueryTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        void shouldCreateSearchStoresQuery() {
            // given
            Double latitude = 37.5665;
            Double longitude = 126.9780;
            String keyword = "빵집";

            // when
            SearchStoresQuery query = new SearchStoresQuery(latitude, longitude, null, keyword);

            // then
            assertThat(query.lat()).isEqualTo(latitude);
            assertThat(query.lng()).isEqualTo(longitude);
        }
    }

    @Nested
    @DisplayName("FindAllByOwnerIdQuery 생성자 테스트")
    class FindAllByOwnerIdQueryTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        void shouldCreateFindAllByOwnerIdQuery() {
            // given
            Long ownerId = 1L;

            // when
            FindWithCategoryAndFavoriteCountByOwnerIdQuery query = new FindWithCategoryAndFavoriteCountByOwnerIdQuery(ownerId);

            // then
            assertThat(query.ownerId()).isEqualTo(ownerId);
        }
    }

    @Nested
    @DisplayName("FindByOwnerIdQuery 테스트")
    class FindByOwnerIdQueryTest {

        @Test
        @DisplayName("FindByOwnerIdQuery 생성 성공")
        void shouldCreateFindByOwnerIdQuery() {
            // given
            Long ownerId = 1L;

            // when
            FindByOwnerIdQuery query = new FindByOwnerIdQuery(ownerId);

            // then
            assertThat(query.ownerId()).isEqualTo(ownerId);
        }
    }

    @Nested
    @DisplayName("FindAllByCategoryIdQuery 생성자 테스트")
    class FindAllByCategoryIdQueryTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        void shouldCreateFindAllByCategoryIdQuery() {
            // given
            Long categoryId = 1L;

            // when
            FindByCategoryIdQuery query = new FindByCategoryIdQuery(categoryId);

            // then
            assertThat(query.categoryId()).isEqualTo(categoryId);
        }
    }

    @Nested
    @DisplayName("FindByNameQuery 생성자 테스트")
    class FindByNameQueryTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        void shouldCreateFindByNameQuery() {
            // given
            String name = "테스트 가게";

            // when
            FindByNameQuery query = new FindByNameQuery(name);

            // then
            assertThat(query.name()).isEqualTo(name);
        }
    }
}

package com.backend.onharu.domain.owner.dto;

import com.backend.onharu.domain.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.backend.onharu.domain.support.error.ErrorType.Owner.OWNER_ID_MUST_NOT_BE_NULL;
import static com.backend.onharu.domain.support.error.ErrorType.Owner.OWNER_USER_ID_MUST_NOT_BE_NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("OwnerQuery 단위 테스트")
class OwnerQueryTest {

    @Nested
    @DisplayName("GetOwnerByIdQuery 생성 테스트")
    class GetOwnerByIdQueryTest {

        @Test
        @DisplayName("id가 null이면 예외 발생")
        void shouldThrowExceptionWhenIdNull() {
            // GIVEN, WHEN
            CoreException exception = assertThrows(CoreException.class, () -> new OwnerQuery.GetOwnerByIdQuery(null));

            // THEN
            assertEquals(OWNER_ID_MUST_NOT_BE_NULL, exception.getErrorType());
        }

        @Test
        @DisplayName("GetOwnerByIdQuery 생성 성공")
        void shouldCreate() {
            // GIVEN
            Long ownerId = 1L;

            // WHEN
            OwnerQuery.GetOwnerByIdQuery query = new OwnerQuery.GetOwnerByIdQuery(ownerId);

            // THEN
            assertEquals(ownerId, query.id());
        }
    }

    @Nested
    @DisplayName("GetOwnerByUserIdQuery 생성 테스트")
    class GetOwnerByUserIdQueryTest {

        @Test
        @DisplayName("userId가 null이면 예외 발생")
        void shouldThrowExceptionWhenUserIdNull() {
            // GIVEN, WHEN
            CoreException exception = assertThrows(CoreException.class, () -> new OwnerQuery.GetOwnerByUserIdQuery(null));

            // THEN
            assertEquals(OWNER_USER_ID_MUST_NOT_BE_NULL, exception.getErrorType());
        }

        @Test
        @DisplayName("GetOwnerByUserIdQuery 생성 성공")
        void shouldCreate() {
            // GIVEN
            Long userId = 10L;

            // WHEN
            OwnerQuery.GetOwnerByUserIdQuery query = new OwnerQuery.GetOwnerByUserIdQuery(userId);

            // THEN
            assertEquals(userId, query.userId());
        }
    }
}
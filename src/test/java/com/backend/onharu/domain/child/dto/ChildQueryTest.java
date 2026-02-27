package com.backend.onharu.domain.child.dto;

import com.backend.onharu.domain.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.backend.onharu.domain.child.dto.ChildQuery.GetChildByIdQuery;
import static com.backend.onharu.domain.child.dto.ChildQuery.GetChildByUserIdQuery;
import static com.backend.onharu.domain.support.error.ErrorType.Child.CHILD_ID_MUST_NOT_BE_NULL;
import static com.backend.onharu.domain.support.error.ErrorType.Child.CHILD_USER_ID_MUST_NOT_BE_NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ChildQuery 단위 테스트")
class ChildQueryTest {

    @Nested
    @DisplayName("GetChildByIdQuery 생성 테스트")
    class GetChildByIdQueryTest {

        @Test
        @DisplayName("childId가 null이면 예외 발생")
        void shouldThrowExceptionWhenChildIdNull() {
            // GIVEN, WHEN
            CoreException exception = assertThrows(CoreException.class, () -> new GetChildByIdQuery(null));

            // THEN
            assertEquals(CHILD_ID_MUST_NOT_BE_NULL, exception.getErrorType());
        }

        @Test
        @DisplayName("GetChildByIdQuery 생성 성공")
        void shouldCreate() {
            // GIVEN
            Long childId = 1L;

            // WHEN
            GetChildByIdQuery query = new GetChildByIdQuery(childId);

            // THEN
            assertEquals(childId, query.childId());
        }
    }

    @Nested
    @DisplayName("GetChildByUserIdQuery 생성 테스트")
    class GetChildByUserIdQueryTest {

        @Test
        @DisplayName("userId가 null이면 예외 발생")
        void shouldThrowExceptionWhenUserIdNull() {
            // GIVEN, WHEN
            CoreException exception = assertThrows(CoreException.class, () -> new GetChildByUserIdQuery(null));

            // THEN
            assertEquals(CHILD_USER_ID_MUST_NOT_BE_NULL, exception.getErrorType());
        }

        @Test
        @DisplayName("GetChildByUserIdQuery 생성 성공")
        void shouldCreate() {
            // GIVEN
            Long userId = 10L;

            // WHEN
            GetChildByUserIdQuery query = new GetChildByUserIdQuery(userId);

            // THEN
            assertEquals(userId, query.userId());
        }
    }
}
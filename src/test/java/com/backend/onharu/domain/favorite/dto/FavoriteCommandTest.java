package com.backend.onharu.domain.favorite.dto;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.favorite.model.Favorite;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static com.backend.onharu.domain.favorite.dto.FavoriteCommand.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 찜하기 Command DTO 테스트 코드 입니다.
 */
class FavoriteCommandTest {

    @Mock
    User user;

    @Mock
    Owner owner;

    @Mock
    Category category;

    @Mock
    Store store;

    @Test
    @DisplayName("찜하기 생성 Command 생성 테스트")
    void createFavoriteCommand() {
        // GIVEN
        Child child = Child.builder()
                .user(user)
                .nickname("찜하기테스트닉네임1")
                .isVerified(true)
                .build();

        Store store = Store.builder()
                .owner(owner)
                .category(category)
                .name("찜하기테스트이름1")
                .address("서울시 강남구 테헤란로 123")
                .phone("0211111111")
                .lat("37.5665")
                .lng("126.9780")
                .introduction("따뜻한 마음으로 환영합니다!")
                .intro("따뜻한 한 끼 식사")
                .isOpen(true)
                .isSharing(true)
                .build();

        // WHEN
        CreateFavoriteCommand command = new CreateFavoriteCommand(child, store);

        // THEN
        assertThat(command.child()).isEqualTo(child);
        assertThat(command.store()).isEqualTo(store);
    }

    @Test
    @DisplayName("찜하기 취소 Command 생성 테스트")
    void deleteFavoriteCommand() {
        // GIVEN
        Child child = Child.builder()
                .user(user)
                .nickname("찜하기테스트닉네임1")
                .isVerified(true)
                .build();

        Store store = Store.builder()
                .owner(owner)
                .category(category)
                .name("찜하기테스트이름1")
                .address("서울시 강남구 테헤란로 123")
                .phone("0211111111")
                .lat("37.5665")
                .lng("126.9780")
                .introduction("따뜻한 마음으로 환영합니다!")
                .intro("따뜻한 한 끼 식사")
                .isOpen(true)
                .isSharing(true)
                .build();

        Favorite favorite = Favorite.builder()
                .child(child)
                .store(store)
                .build();

        // WHEN
        DeleteFavoriteCommand command = new DeleteFavoriteCommand(favorite);

        // THEN
        assertThat(command.favorite()).isEqualTo(favorite);
    }

    @Test
    @DisplayName("찜하기 토글 Command 생성 테스트")
    void toggleFavoriteCommand() {
        // GIVEN
        Long childId = 1L;
        Long storeId = 10L;

        // WHEN
        ToggleFavoriteCommand command = new ToggleFavoriteCommand(childId, storeId);

        // THEN
        assertThat(command.childId()).isEqualTo(childId);
        assertThat(command.storeId()).isEqualTo(storeId);
    }
}
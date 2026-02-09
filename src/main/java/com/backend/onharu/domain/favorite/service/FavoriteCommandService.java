package com.backend.onharu.domain.favorite.service;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.favorite.dto.FavoriteCommand.CreateFavoriteCommand;
import com.backend.onharu.domain.favorite.dto.FavoriteCommand.DeleteFavoriteCommand;
import com.backend.onharu.domain.favorite.dto.FavoriteRepositoryParam.GetFavoriteByIdParam;
import com.backend.onharu.domain.favorite.model.Favorite;
import com.backend.onharu.domain.favorite.repository.FavoriteRepository;
import com.backend.onharu.domain.store.model.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 찜하기 Command Service
 */
@Service
@Transactional
@RequiredArgsConstructor
public class FavoriteCommandService {

    private final FavoriteRepository favoriteRepository;

    /**
     * 찜하기 생성(등록)
     */
    public Favorite createFavorite(CreateFavoriteCommand command, Child child, Store store) {
        // 도메인 Favorite 생성
        Favorite favorite = Favorite.builder()
                .child(child)
                .store(store)
                .build();

        // 찜하기 등록(저장)
        return favoriteRepository.save(favorite);
    }

    /**
     * 찜하기 삭제(취소)
     */
    public void deleteFavorite(DeleteFavoriteCommand command) {
        // 삭제할 찜하기 단건 조회
        Favorite favorite = favoriteRepository.getFavorite(new GetFavoriteByIdParam(command.favoriteId()));

        // 찜하기 취소(삭졔)
        favoriteRepository.delete(favorite);
    }
}

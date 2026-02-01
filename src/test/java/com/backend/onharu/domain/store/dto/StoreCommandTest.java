package com.backend.onharu.domain.store.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.backend.onharu.domain.store.dto.StoreCommand.ChangeOpenStatusCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.CreateStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.DeleteStoreCommand;
import com.backend.onharu.domain.store.dto.StoreCommand.UpdateStoreCommand;

@DisplayName("StoreCommand 단위 테스트")
class StoreCommandTest {

    @Nested
    @DisplayName("CreateStoreCommand 생성자 테스트")
    class CreateStoreCommandTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        public void shouldCreateStoreCommand() {
            // given
            Long ownerId = 1L;
            Long categoryId = 1L;
            String name = "테스트 가게";
            String address = "서울시 강남구 테헤란로 123";
            String phone = "0212345678";
            String lat = "37.5665";
            String lng = "126.9780";
            String image = "https://onharu.com/images/store1.jpg";
            String introduction = "따뜻한 마음으로 환영합니다!";
            String intro = "따뜻한 한 끼 식사";
            List<String> tagNames = List.of("테스트 태그1", "테스트 태그2");

            // when
            CreateStoreCommand command = new CreateStoreCommand(
                ownerId, categoryId, name, address, phone, lat, lng, image, introduction, intro, tagNames, List.of()
            );

            // then
            assertThat(command.ownerId()).isEqualTo(ownerId);
            assertThat(command.categoryId()).isEqualTo(categoryId);
            assertThat(command.name()).isEqualTo(name);
            assertThat(command.address()).isEqualTo(address);
            assertThat(command.phone()).isEqualTo(phone);
        }
    }

    @Nested
    @DisplayName("UpdateStoreCommand 생성자 테스트")
    class UpdateStoreCommandTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        public void shouldCreateUpdateStoreCommand() {
            // given
            Long id = 1L;
            Long categoryId = 2L;
            String image = "/images/store2.jpg";
            String phone = "0298765432";
            String address = "서울시 서초구 서초대로 456";
            String lat = "37.4838";
            String lng = "127.0324";
            String introduction = "업데이트된 소개";
            String intro = "업데이트된 한줄 소개";
            Boolean isOpen = false;

            // when
            UpdateStoreCommand command = new UpdateStoreCommand(
                id, categoryId, image, phone, address, lat, lng, introduction, intro, isOpen, List.of(), List.of()
            );

            // then
            assertThat(command.id()).isEqualTo(id);
            assertThat(command.categoryId()).isEqualTo(categoryId);
            assertThat(command.isOpen()).isEqualTo(isOpen);
        }
    }

    @Nested
    @DisplayName("DeleteStoreCommand 생성자 테스트")
    class DeleteStoreCommandTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        public void shouldCreateDeleteStoreCommand() {
            // given
            Long id = 1L;

            // when
            DeleteStoreCommand command = new DeleteStoreCommand(id);

            // then
            assertThat(command.id()).isEqualTo(id);
        }
    }

    @Nested
    @DisplayName("ChangeOpenStatusCommand 생성자 테스트")
    class ChangeOpenStatusCommandTest {
        
        @Test
        @DisplayName("생성자 생성 성공")
        public void shouldCreateChangeOpenStatusCommand() {
            // given
            Long id = 1L;
            Boolean isOpen = true;

            // when
            ChangeOpenStatusCommand command = new ChangeOpenStatusCommand(id, isOpen);

            // then
            assertThat(command.id()).isEqualTo(id);
            assertThat(command.isOpen()).isEqualTo(isOpen);
        }
    }
}

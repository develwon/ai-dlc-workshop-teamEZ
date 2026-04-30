package com.tableorder.service;

import com.tableorder.dto.*;
import com.tableorder.entity.Category;
import com.tableorder.entity.Menu;
import com.tableorder.exception.ConflictException;
import com.tableorder.exception.NotFoundException;
import com.tableorder.repository.CategoryRepository;
import com.tableorder.repository.MenuRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private MenuService menuService;

    private static final Long STORE_ID = 1L;

    // ==================== 카테고리 테스트 ====================

    @Nested
    @DisplayName("카테고리 조회")
    class GetCategories {

        @Test
        @DisplayName("매장별 카테고리 목록을 조회한다")
        void getCategoriesByStore() {
            Category cat = createCategory(1L, "메인", 0);
            when(categoryRepository.findByStoreIdOrderByDisplayOrder(STORE_ID))
                    .thenReturn(List.of(cat));

            List<CategoryResponse> result = menuService.getCategoriesByStore(STORE_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("메인");
        }
    }

    @Nested
    @DisplayName("카테고리 등록")
    class CreateCategory {

        @Test
        @DisplayName("카테고리를 정상 등록한다")
        void createCategory_success() {
            CreateCategoryRequest request = new CreateCategoryRequest("메인", 0);
            Category saved = createCategory(1L, "메인", 0);

            when(categoryRepository.existsByStoreIdAndName(STORE_ID, "메인")).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenReturn(saved);

            CategoryResponse result = menuService.createCategory(STORE_ID, request);

            assertThat(result.getName()).isEqualTo("메인");
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("중복 카테고리명이면 ConflictException 발생")
        void createCategory_duplicate() {
            CreateCategoryRequest request = new CreateCategoryRequest("메인", 0);
            when(categoryRepository.existsByStoreIdAndName(STORE_ID, "메인")).thenReturn(true);

            assertThatThrownBy(() -> menuService.createCategory(STORE_ID, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("이미 존재하는 카테고리명");
        }
    }

    @Nested
    @DisplayName("카테고리 수정")
    class UpdateCategory {

        @Test
        @DisplayName("카테고리를 정상 수정한다")
        void updateCategory_success() {
            Category category = createCategory(1L, "메인", 0);
            UpdateCategoryRequest request = new UpdateCategoryRequest("메인메뉴", 1);

            when(categoryRepository.findByIdAndStoreId(1L, STORE_ID)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByStoreIdAndNameAndIdNot(STORE_ID, "메인메뉴", 1L)).thenReturn(false);

            CategoryResponse result = menuService.updateCategory(STORE_ID, 1L, request);

            assertThat(result.getName()).isEqualTo("메인메뉴");
        }

        @Test
        @DisplayName("존재하지 않는 카테고리면 NotFoundException 발생")
        void updateCategory_notFound() {
            UpdateCategoryRequest request = new UpdateCategoryRequest("메인", 0);
            when(categoryRepository.findByIdAndStoreId(999L, STORE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> menuService.updateCategory(STORE_ID, 999L, request))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("수정 시 다른 카테고리와 이름 중복이면 ConflictException 발생")
        void updateCategory_duplicateName() {
            Category category = createCategory(1L, "메인", 0);
            UpdateCategoryRequest request = new UpdateCategoryRequest("음료", 0);

            when(categoryRepository.findByIdAndStoreId(1L, STORE_ID)).thenReturn(Optional.of(category));
            when(categoryRepository.existsByStoreIdAndNameAndIdNot(STORE_ID, "음료", 1L)).thenReturn(true);

            assertThatThrownBy(() -> menuService.updateCategory(STORE_ID, 1L, request))
                    .isInstanceOf(ConflictException.class);
        }
    }

    @Nested
    @DisplayName("카테고리 삭제")
    class DeleteCategory {

        @Test
        @DisplayName("카테고리를 정상 삭제한다")
        void deleteCategory_success() {
            Category category = createCategory(1L, "메인", 0);
            when(categoryRepository.findByIdAndStoreId(1L, STORE_ID)).thenReturn(Optional.of(category));
            when(menuRepository.existsByCategoryId(1L)).thenReturn(false);

            menuService.deleteCategory(STORE_ID, 1L);

            verify(categoryRepository).delete(category);
        }

        @Test
        @DisplayName("메뉴가 존재하는 카테고리는 삭제 불가")
        void deleteCategory_hasMenus() {
            Category category = createCategory(1L, "메인", 0);
            when(categoryRepository.findByIdAndStoreId(1L, STORE_ID)).thenReturn(Optional.of(category));
            when(menuRepository.existsByCategoryId(1L)).thenReturn(true);

            assertThatThrownBy(() -> menuService.deleteCategory(STORE_ID, 1L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("메뉴가 존재하는 카테고리");
        }
    }

    // ==================== 메뉴 테스트 ====================

    @Nested
    @DisplayName("메뉴 조회")
    class GetMenus {

        @Test
        @DisplayName("매장별 전체 메뉴를 조회한다")
        void getMenusByStore_all() {
            Menu menu = createMenu(1L, "김치찌개", 8000, 1L);
            when(menuRepository.findByStoreIdOrderByDisplayOrder(STORE_ID)).thenReturn(List.of(menu));

            List<MenuResponse> result = menuService.getMenusByStore(STORE_ID, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("김치찌개");
        }

        @Test
        @DisplayName("카테고리별 메뉴를 조회한다")
        void getMenusByStore_byCategory() {
            Menu menu = createMenu(1L, "김치찌개", 8000, 1L);
            when(menuRepository.findByStoreIdAndCategoryIdOrderByDisplayOrder(STORE_ID, 1L))
                    .thenReturn(List.of(menu));

            List<MenuResponse> result = menuService.getMenusByStore(STORE_ID, 1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("메뉴 상세를 조회한다")
        void getMenuDetail_success() {
            Menu menu = createMenu(1L, "김치찌개", 8000, 1L);
            Category category = createCategory(1L, "메인", 0);

            when(menuRepository.findByIdAndStoreId(1L, STORE_ID)).thenReturn(Optional.of(menu));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

            MenuDetailResponse result = menuService.getMenuDetail(STORE_ID, 1L);

            assertThat(result.getName()).isEqualTo("김치찌개");
            assertThat(result.getCategoryName()).isEqualTo("메인");
        }

        @Test
        @DisplayName("존재하지 않는 메뉴 상세 조회 시 NotFoundException 발생")
        void getMenuDetail_notFound() {
            when(menuRepository.findByIdAndStoreId(999L, STORE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> menuService.getMenuDetail(STORE_ID, 999L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("메뉴 등록")
    class CreateMenu {

        @Test
        @DisplayName("메뉴를 정상 등록한다")
        void createMenu_success() {
            CreateMenuRequest request = new CreateMenuRequest("김치찌개", 8000, "맛있는 김치찌개", 1L, null);
            Category category = createCategory(1L, "메인", 0);
            Menu saved = createMenu(1L, "김치찌개", 8000, 1L);

            when(categoryRepository.findByIdAndStoreId(1L, STORE_ID)).thenReturn(Optional.of(category));
            when(menuRepository.save(any(Menu.class))).thenReturn(saved);

            MenuResponse result = menuService.createMenu(STORE_ID, request, null);

            assertThat(result.getName()).isEqualTo("김치찌개");
            assertThat(result.getPrice()).isEqualTo(8000);
        }

        @Test
        @DisplayName("이미지 URL과 함께 메뉴를 등록한다")
        void createMenu_withImage() {
            CreateMenuRequest request = new CreateMenuRequest("김치찌개", 8000, "맛있는 김치찌개", 1L, null);
            Category category = createCategory(1L, "메인", 0);
            Menu saved = createMenuWithImage(1L, "김치찌개", 8000, 1L, "https://s3.com/image.jpg");

            when(categoryRepository.findByIdAndStoreId(1L, STORE_ID)).thenReturn(Optional.of(category));
            when(menuRepository.save(any(Menu.class))).thenReturn(saved);

            MenuResponse result = menuService.createMenu(STORE_ID, request, "https://s3.com/image.jpg");

            assertThat(result.getImageUrl()).isEqualTo("https://s3.com/image.jpg");
        }

        @Test
        @DisplayName("유효하지 않은 카테고리면 NotFoundException 발생")
        void createMenu_invalidCategory() {
            CreateMenuRequest request = new CreateMenuRequest("김치찌개", 8000, null, 999L, null);
            when(categoryRepository.findByIdAndStoreId(999L, STORE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> menuService.createMenu(STORE_ID, request, null))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("카테고리");
        }
    }

    @Nested
    @DisplayName("메뉴 수정")
    class UpdateMenu {

        @Test
        @DisplayName("메뉴를 정상 수정한다")
        void updateMenu_success() {
            Menu menu = createMenu(1L, "김치찌개", 8000, 1L);
            UpdateMenuRequest request = new UpdateMenuRequest("된장찌개", 7000, "맛있는 된장찌개", 1L, null);
            Category category = createCategory(1L, "메인", 0);

            when(menuRepository.findByIdAndStoreId(1L, STORE_ID)).thenReturn(Optional.of(menu));
            when(categoryRepository.findByIdAndStoreId(1L, STORE_ID)).thenReturn(Optional.of(category));

            MenuResponse result = menuService.updateMenu(STORE_ID, 1L, request, null);

            assertThat(result.getName()).isEqualTo("된장찌개");
            assertThat(result.getPrice()).isEqualTo(7000);
        }

        @Test
        @DisplayName("새 이미지 업로드 시 기존 이미지 삭제")
        void updateMenu_replaceImage() {
            Menu menu = createMenuWithImage(1L, "김치찌개", 8000, 1L, "https://s3.com/old.jpg");
            UpdateMenuRequest request = new UpdateMenuRequest("김치찌개", 8000, null, 1L, null);
            Category category = createCategory(1L, "메인", 0);

            when(menuRepository.findByIdAndStoreId(1L, STORE_ID)).thenReturn(Optional.of(menu));
            when(categoryRepository.findByIdAndStoreId(1L, STORE_ID)).thenReturn(Optional.of(category));

            menuService.updateMenu(STORE_ID, 1L, request, "https://s3.com/new.jpg");

            verify(fileStorageService).deleteImage("https://s3.com/old.jpg");
        }
    }

    @Nested
    @DisplayName("메뉴 삭제")
    class DeleteMenu {

        @Test
        @DisplayName("메뉴를 정상 삭제한다")
        void deleteMenu_success() {
            Menu menu = createMenu(1L, "김치찌개", 8000, 1L);
            when(menuRepository.findByIdAndStoreId(1L, STORE_ID)).thenReturn(Optional.of(menu));

            menuService.deleteMenu(STORE_ID, 1L);

            verify(menuRepository).delete(menu);
        }

        @Test
        @DisplayName("이미지가 있는 메뉴 삭제 시 이미지도 삭제")
        void deleteMenu_withImage() {
            Menu menu = createMenuWithImage(1L, "김치찌개", 8000, 1L, "https://s3.com/image.jpg");
            when(menuRepository.findByIdAndStoreId(1L, STORE_ID)).thenReturn(Optional.of(menu));

            menuService.deleteMenu(STORE_ID, 1L);

            verify(menuRepository).delete(menu);
            verify(fileStorageService).deleteImage("https://s3.com/image.jpg");
        }

        @Test
        @DisplayName("존재하지 않는 메뉴 삭제 시 NotFoundException 발생")
        void deleteMenu_notFound() {
            when(menuRepository.findByIdAndStoreId(999L, STORE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> menuService.deleteMenu(STORE_ID, 999L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("메뉴 순서 변경")
    class UpdateMenuOrder {

        @Test
        @DisplayName("메뉴 순서를 정상 변경한다")
        void updateMenuDisplayOrder_success() {
            Menu menu1 = createMenu(1L, "김치찌개", 8000, 1L);
            Menu menu2 = createMenu(2L, "된장찌개", 7000, 1L);

            when(menuRepository.findByIdAndStoreId(1L, STORE_ID)).thenReturn(Optional.of(menu1));
            when(menuRepository.findByIdAndStoreId(2L, STORE_ID)).thenReturn(Optional.of(menu2));

            List<MenuOrderRequest> requests = List.of(
                    new MenuOrderRequest(1L, 2),
                    new MenuOrderRequest(2L, 1)
            );

            menuService.updateMenuDisplayOrder(STORE_ID, requests);

            assertThat(menu1.getDisplayOrder()).isEqualTo(2);
            assertThat(menu2.getDisplayOrder()).isEqualTo(1);
        }
    }

    // ==================== Helper Methods ====================

    private Category createCategory(Long id, String name, int displayOrder) {
        Category category = Category.builder()
                .storeId(STORE_ID)
                .name(name)
                .displayOrder(displayOrder)
                .build();
        // Reflection으로 id 설정
        try {
            var field = Category.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(category, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return category;
    }

    private Menu createMenu(Long id, String name, int price, Long categoryId) {
        return createMenuWithImage(id, name, price, categoryId, null);
    }

    private Menu createMenuWithImage(Long id, String name, int price, Long categoryId, String imageUrl) {
        Menu menu = Menu.builder()
                .storeId(STORE_ID)
                .categoryId(categoryId)
                .name(name)
                .price(price)
                .imageUrl(imageUrl)
                .displayOrder(0)
                .build();
        try {
            var field = Menu.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(menu, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return menu;
    }
}

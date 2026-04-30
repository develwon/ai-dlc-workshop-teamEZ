package com.tableorder.service;

import com.tableorder.dto.*;
import com.tableorder.entity.Category;
import com.tableorder.entity.Menu;
import com.tableorder.exception.ConflictException;
import com.tableorder.exception.NotFoundException;
import com.tableorder.repository.CategoryRepository;
import com.tableorder.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    // ==================== 카테고리 ====================

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByStore(Long storeId) {
        log.debug("카테고리 목록 조회 [storeId={}]", storeId);
        return categoryRepository.findByStoreIdOrderByDisplayOrder(storeId)
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(Long storeId, CreateCategoryRequest request) {
        log.info("카테고리 등록 [storeId={}] [name={}]", storeId, request.getName());

        if (categoryRepository.existsByStoreIdAndName(storeId, request.getName())) {
            throw new ConflictException("이미 존재하는 카테고리명입니다.");
        }

        Category category = Category.builder()
                .storeId(storeId)
                .name(request.getName())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        Category saved = categoryRepository.save(category);
        return toCategoryResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long storeId, Long categoryId, UpdateCategoryRequest request) {
        log.info("카테고리 수정 [storeId={}] [categoryId={}]", storeId, categoryId);

        Category category = categoryRepository.findByIdAndStoreId(categoryId, storeId)
                .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다."));

        if (categoryRepository.existsByStoreIdAndNameAndIdNot(storeId, request.getName(), categoryId)) {
            throw new ConflictException("이미 존재하는 카테고리명입니다.");
        }

        category.updateName(request.getName());
        if (request.getDisplayOrder() != null) {
            category.updateDisplayOrder(request.getDisplayOrder());
        }

        return toCategoryResponse(category);
    }

    @Transactional
    public void deleteCategory(Long storeId, Long categoryId) {
        log.info("카테고리 삭제 [storeId={}] [categoryId={}]", storeId, categoryId);

        Category category = categoryRepository.findByIdAndStoreId(categoryId, storeId)
                .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다."));

        if (menuRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("메뉴가 존재하는 카테고리는 삭제할 수 없습니다.");
        }

        categoryRepository.delete(category);
    }

    // ==================== 메뉴 ====================

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenusByStore(Long storeId, Long categoryId) {
        log.debug("메뉴 목록 조회 [storeId={}] [categoryId={}]", storeId, categoryId);

        List<Menu> menus;
        if (categoryId != null) {
            menus = menuRepository.findByStoreIdAndCategoryIdOrderByDisplayOrder(storeId, categoryId);
        } else {
            menus = menuRepository.findByStoreIdOrderByDisplayOrder(storeId);
        }

        return menus.stream()
                .map(this::toMenuResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MenuDetailResponse getMenuDetail(Long storeId, Long menuId) {
        log.debug("메뉴 상세 조회 [storeId={}] [menuId={}]", storeId, menuId);

        Menu menu = menuRepository.findByIdAndStoreId(menuId, storeId)
                .orElseThrow(() -> new NotFoundException("메뉴를 찾을 수 없습니다."));

        String categoryName = categoryRepository.findById(menu.getCategoryId())
                .map(Category::getName)
                .orElse(null);

        return toMenuDetailResponse(menu, categoryName);
    }

    @Transactional
    public MenuResponse createMenu(Long storeId, CreateMenuRequest request, String imageUrl) {
        log.info("메뉴 등록 [storeId={}] [name={}] [categoryId={}]", storeId, request.getName(), request.getCategoryId());

        validateCategoryBelongsToStore(storeId, request.getCategoryId());

        Menu menu = Menu.builder()
                .storeId(storeId)
                .categoryId(request.getCategoryId())
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .imageUrl(imageUrl)
                .displayOrder(0)
                .build();

        Menu saved = menuRepository.save(menu);
        return toMenuResponse(saved);
    }

    @Transactional
    public MenuResponse updateMenu(Long storeId, Long menuId, UpdateMenuRequest request, String newImageUrl) {
        log.info("메뉴 수정 [storeId={}] [menuId={}]", storeId, menuId);

        Menu menu = menuRepository.findByIdAndStoreId(menuId, storeId)
                .orElseThrow(() -> new NotFoundException("메뉴를 찾을 수 없습니다."));

        validateCategoryBelongsToStore(storeId, request.getCategoryId());

        String oldImageUrl = menu.getImageUrl();
        String imageUrlToSet = newImageUrl != null ? newImageUrl : menu.getImageUrl();

        menu.update(
                request.getName(),
                request.getPrice(),
                request.getDescription(),
                request.getCategoryId(),
                imageUrlToSet
        );

        // 새 이미지가 업로드된 경우 기존 이미지 삭제 (best-effort)
        if (newImageUrl != null && oldImageUrl != null) {
            fileStorageService.deleteImage(oldImageUrl);
        }

        return toMenuResponse(menu);
    }

    @Transactional
    public void deleteMenu(Long storeId, Long menuId) {
        log.info("메뉴 삭제 [storeId={}] [menuId={}]", storeId, menuId);

        Menu menu = menuRepository.findByIdAndStoreId(menuId, storeId)
                .orElseThrow(() -> new NotFoundException("메뉴를 찾을 수 없습니다."));

        String imageUrl = menu.getImageUrl();
        menuRepository.delete(menu);

        // 이미지 삭제 (best-effort)
        if (imageUrl != null) {
            fileStorageService.deleteImage(imageUrl);
        }
    }

    @Transactional
    public void updateMenuDisplayOrder(Long storeId, List<MenuOrderRequest> requests) {
        log.info("메뉴 순서 변경 [storeId={}] [count={}]", storeId, requests.size());

        for (MenuOrderRequest request : requests) {
            Menu menu = menuRepository.findByIdAndStoreId(request.getMenuId(), storeId)
                    .orElseThrow(() -> new NotFoundException("메뉴를 찾을 수 없습니다. (menuId=" + request.getMenuId() + ")"));
            menu.updateDisplayOrder(request.getDisplayOrder());
        }
    }

    // ==================== Private Methods ====================

    private void validateCategoryBelongsToStore(Long storeId, Long categoryId) {
        if (!categoryRepository.findByIdAndStoreId(categoryId, storeId).isPresent()) {
            throw new NotFoundException("카테고리를 찾을 수 없습니다.");
        }
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .displayOrder(category.getDisplayOrder())
                .build();
    }

    private MenuResponse toMenuResponse(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .description(menu.getDescription())
                .imageUrl(menu.getImageUrl())
                .categoryId(menu.getCategoryId())
                .displayOrder(menu.getDisplayOrder())
                .build();
    }

    private MenuDetailResponse toMenuDetailResponse(Menu menu, String categoryName) {
        return MenuDetailResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .price(menu.getPrice())
                .description(menu.getDescription())
                .imageUrl(menu.getImageUrl())
                .categoryId(menu.getCategoryId())
                .categoryName(categoryName)
                .displayOrder(menu.getDisplayOrder())
                .build();
    }
}

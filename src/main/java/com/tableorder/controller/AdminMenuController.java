package com.tableorder.controller;

import com.tableorder.dto.*;
import com.tableorder.service.FileStorageService;
import com.tableorder.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;
    private final FileStorageService fileStorageService;

    // ==================== 메뉴 ====================

    @GetMapping("/menus")
    public ResponseEntity<List<MenuResponse>> getMenus(
            @RequestParam(required = false) Long categoryId,
            HttpServletRequest request) {

        Long storeId = getStoreId(request);
        List<MenuResponse> menus = menuService.getMenusByStore(storeId, categoryId);
        return ResponseEntity.ok(menus);
    }

    @PostMapping("/menus")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<MenuResponse> createMenu(
            @Valid @RequestPart("menu") CreateMenuRequest menuRequest,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpServletRequest request) {

        Long storeId = getStoreId(request);

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = fileStorageService.uploadImage(storeId, image);
        }

        try {
            MenuResponse menu = menuService.createMenu(storeId, menuRequest, imageUrl);
            return ResponseEntity.status(HttpStatus.CREATED).body(menu);
        } catch (Exception e) {
            // 보상 트랜잭션: DB 저장 실패 시 업로드된 이미지 삭제
            if (imageUrl != null) {
                fileStorageService.deleteImage(imageUrl);
            }
            throw e;
        }
    }

    @PutMapping("/menus/{menuId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<MenuResponse> updateMenu(
            @PathVariable Long menuId,
            @Valid @RequestPart("menu") UpdateMenuRequest menuRequest,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpServletRequest request) {

        Long storeId = getStoreId(request);

        String newImageUrl = null;
        if (image != null && !image.isEmpty()) {
            newImageUrl = fileStorageService.uploadImage(storeId, image);
        }

        try {
            MenuResponse menu = menuService.updateMenu(storeId, menuId, menuRequest, newImageUrl);
            return ResponseEntity.ok(menu);
        } catch (Exception e) {
            // 보상 트랜잭션: DB 수정 실패 시 새로 업로드된 이미지 삭제
            if (newImageUrl != null) {
                fileStorageService.deleteImage(newImageUrl);
            }
            throw e;
        }
    }

    @DeleteMapping("/menus/{menuId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<Void> deleteMenu(
            @PathVariable Long menuId,
            HttpServletRequest request) {

        Long storeId = getStoreId(request);
        menuService.deleteMenu(storeId, menuId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/menus/order")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<Void> updateMenuOrder(
            @RequestBody List<MenuOrderRequest> requests,
            HttpServletRequest request) {

        Long storeId = getStoreId(request);
        menuService.updateMenuDisplayOrder(storeId, requests);
        return ResponseEntity.ok().build();
    }

    // ==================== 카테고리 ====================

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories(HttpServletRequest request) {
        Long storeId = getStoreId(request);
        List<CategoryResponse> categories = menuService.getCategoriesByStore(storeId);
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest categoryRequest,
            HttpServletRequest request) {

        Long storeId = getStoreId(request);
        CategoryResponse category = menuService.createCategory(storeId, categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest categoryRequest,
            HttpServletRequest request) {

        Long storeId = getStoreId(request);
        CategoryResponse category = menuService.updateCategory(storeId, categoryId, categoryRequest);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/categories/{categoryId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long categoryId,
            HttpServletRequest request) {

        Long storeId = getStoreId(request);
        menuService.deleteCategory(storeId, categoryId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Private ====================

    private Long getStoreId(HttpServletRequest request) {
        return (Long) request.getAttribute("storeId");
    }
}

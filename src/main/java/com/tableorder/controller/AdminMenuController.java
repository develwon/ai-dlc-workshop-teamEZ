package com.tableorder.controller;

import com.tableorder.dto.*;
import com.tableorder.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;

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
            @Valid @RequestBody CreateMenuRequest menuRequest,
            HttpServletRequest request) {

        Long storeId = getStoreId(request);
        String imageUrl = menuRequest.getImageUrl();

        MenuResponse menu = menuService.createMenu(storeId, menuRequest, imageUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(menu);
    }

    @PutMapping("/menus/{menuId}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<MenuResponse> updateMenu(
            @PathVariable Long menuId,
            @Valid @RequestBody UpdateMenuRequest menuRequest,
            HttpServletRequest request) {

        Long storeId = getStoreId(request);
        String newImageUrl = menuRequest.getImageUrl();

        MenuResponse menu = menuService.updateMenu(storeId, menuId, menuRequest, newImageUrl);
        return ResponseEntity.ok(menu);
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

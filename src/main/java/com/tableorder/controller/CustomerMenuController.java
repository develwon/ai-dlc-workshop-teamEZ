package com.tableorder.controller;

import com.tableorder.dto.CategoryResponse;
import com.tableorder.dto.MenuDetailResponse;
import com.tableorder.dto.MenuResponse;
import com.tableorder.security.StoreIsolationValidator;
import com.tableorder.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/stores/{storeId}")
@RequiredArgsConstructor
public class CustomerMenuController {

    private final MenuService menuService;
    private final StoreIsolationValidator storeIsolationValidator;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @PathVariable Long storeId,
            HttpServletRequest request) {

        Long jwtStoreId = (Long) request.getAttribute("storeId");
        storeIsolationValidator.validateStoreAccess(jwtStoreId, storeId);

        List<CategoryResponse> categories = menuService.getCategoriesByStore(storeId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/menus")
    public ResponseEntity<List<MenuResponse>> getMenusByCategory(
            @PathVariable Long storeId,
            @RequestParam(required = false) Long categoryId,
            HttpServletRequest request) {

        Long jwtStoreId = (Long) request.getAttribute("storeId");
        storeIsolationValidator.validateStoreAccess(jwtStoreId, storeId);

        List<MenuResponse> menus = menuService.getMenusByStore(storeId, categoryId);
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/menus/{menuId}")
    public ResponseEntity<MenuDetailResponse> getMenuDetail(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            HttpServletRequest request) {

        Long jwtStoreId = (Long) request.getAttribute("storeId");
        storeIsolationValidator.validateStoreAccess(jwtStoreId, storeId);

        MenuDetailResponse menuDetail = menuService.getMenuDetail(storeId, menuId);
        return ResponseEntity.ok(menuDetail);
    }
}

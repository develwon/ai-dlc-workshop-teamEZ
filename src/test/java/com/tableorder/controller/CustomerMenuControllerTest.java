package com.tableorder.controller;

import com.tableorder.dto.CategoryResponse;
import com.tableorder.dto.MenuDetailResponse;
import com.tableorder.dto.MenuResponse;
import com.tableorder.exception.NotFoundException;
import com.tableorder.security.JwtAuthenticationFilter;
import com.tableorder.security.JwtTokenProvider;
import com.tableorder.security.StoreIsolationValidator;
import com.tableorder.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerMenuController.class)
class CustomerMenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuService menuService;

    @MockBean
    private StoreIsolationValidator storeIsolationValidator;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final Long STORE_ID = 1L;

    @Test
    @DisplayName("GET /api/stores/{storeId}/categories - 카테고리 목록 조회")
    @WithMockUser(roles = "TABLE")
    void getCategories() throws Exception {
        CategoryResponse category = CategoryResponse.builder()
                .id(1L).name("메인").displayOrder(0).build();

        doNothing().when(storeIsolationValidator).validateStoreAccess(any(), eq(STORE_ID));
        when(menuService.getCategoriesByStore(STORE_ID)).thenReturn(List.of(category));

        mockMvc.perform(get("/api/stores/{storeId}/categories", STORE_ID)
                        .requestAttr("storeId", STORE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("메인"));
    }

    @Test
    @DisplayName("GET /api/stores/{storeId}/menus - 메뉴 목록 조회")
    @WithMockUser(roles = "TABLE")
    void getMenusByCategory() throws Exception {
        MenuResponse menu = MenuResponse.builder()
                .id(1L).name("김치찌개").price(8000).categoryId(1L).displayOrder(0).build();

        doNothing().when(storeIsolationValidator).validateStoreAccess(any(), eq(STORE_ID));
        when(menuService.getMenusByStore(STORE_ID, null)).thenReturn(List.of(menu));

        mockMvc.perform(get("/api/stores/{storeId}/menus", STORE_ID)
                        .requestAttr("storeId", STORE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("김치찌개"))
                .andExpect(jsonPath("$[0].price").value(8000));
    }

    @Test
    @DisplayName("GET /api/stores/{storeId}/menus - 카테고리 필터링 조회")
    @WithMockUser(roles = "TABLE")
    void getMenusByCategory_withFilter() throws Exception {
        MenuResponse menu = MenuResponse.builder()
                .id(1L).name("김치찌개").price(8000).categoryId(1L).displayOrder(0).build();

        doNothing().when(storeIsolationValidator).validateStoreAccess(any(), eq(STORE_ID));
        when(menuService.getMenusByStore(STORE_ID, 1L)).thenReturn(List.of(menu));

        mockMvc.perform(get("/api/stores/{storeId}/menus", STORE_ID)
                        .param("categoryId", "1")
                        .requestAttr("storeId", STORE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/stores/{storeId}/menus/{menuId} - 메뉴 상세 조회")
    @WithMockUser(roles = "TABLE")
    void getMenuDetail() throws Exception {
        MenuDetailResponse detail = MenuDetailResponse.builder()
                .id(1L).name("김치찌개").price(8000).description("맛있는 김치찌개")
                .categoryId(1L).categoryName("메인").displayOrder(0).build();

        doNothing().when(storeIsolationValidator).validateStoreAccess(any(), eq(STORE_ID));
        when(menuService.getMenuDetail(STORE_ID, 1L)).thenReturn(detail);

        mockMvc.perform(get("/api/stores/{storeId}/menus/{menuId}", STORE_ID, 1L)
                        .requestAttr("storeId", STORE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("김치찌개"))
                .andExpect(jsonPath("$.categoryName").value("메인"));
    }

    @Test
    @DisplayName("GET /api/stores/{storeId}/menus/{menuId} - 존재하지 않는 메뉴 404")
    @WithMockUser(roles = "TABLE")
    void getMenuDetail_notFound() throws Exception {
        doNothing().when(storeIsolationValidator).validateStoreAccess(any(), eq(STORE_ID));
        when(menuService.getMenuDetail(STORE_ID, 999L))
                .thenThrow(new NotFoundException("메뉴를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/stores/{storeId}/menus/{menuId}", STORE_ID, 999L)
                        .requestAttr("storeId", STORE_ID))
                .andExpect(status().isNotFound());
    }
}

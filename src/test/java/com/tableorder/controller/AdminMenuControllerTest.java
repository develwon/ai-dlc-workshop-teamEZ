package com.tableorder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tableorder.dto.*;
import com.tableorder.exception.ConflictException;
import com.tableorder.exception.NotFoundException;
import com.tableorder.security.JwtAuthenticationFilter;
import com.tableorder.security.JwtTokenProvider;
import com.tableorder.service.FileStorageService;
import com.tableorder.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminMenuController.class)
class AdminMenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MenuService menuService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final Long STORE_ID = 1L;

    // ==================== 메뉴 테스트 ====================

    @Nested
    @DisplayName("메뉴 조회")
    class MenuRead {

        @Test
        @DisplayName("GET /api/admin/menus - 메뉴 목록 조회")
        @WithMockUser(roles = "OWNER")
        void getMenus() throws Exception {
            MenuResponse menu = MenuResponse.builder()
                    .id(1L).name("김치찌개").price(8000).categoryId(1L).displayOrder(0).build();

            when(menuService.getMenusByStore(STORE_ID, null)).thenReturn(List.of(menu));

            mockMvc.perform(get("/api/admin/menus")
                            .requestAttr("storeId", STORE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("김치찌개"));
        }
    }

    @Nested
    @DisplayName("메뉴 등록")
    class MenuCreate {

        @Test
        @DisplayName("POST /api/admin/menus - 메뉴 등록 (이미지 없이)")
        @WithMockUser(roles = "OWNER")
        void createMenu_withoutImage() throws Exception {
            CreateMenuRequest request = new CreateMenuRequest("김치찌개", 8000, "맛있는 김치찌개", 1L, null);
            MenuResponse response = MenuResponse.builder()
                    .id(1L).name("김치찌개").price(8000).categoryId(1L).displayOrder(0).build();

            when(menuService.createMenu(eq(STORE_ID), any(CreateMenuRequest.class), isNull()))
                    .thenReturn(response);

            MockMultipartFile menuPart = new MockMultipartFile(
                    "menu", "", "application/json",
                    objectMapper.writeValueAsBytes(request));

            mockMvc.perform(multipart("/api/admin/menus")
                            .file(menuPart)
                            .requestAttr("storeId", STORE_ID)
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("김치찌개"));
        }

        @Test
        @DisplayName("POST /api/admin/menus - 메뉴 등록 (이미지 포함)")
        @WithMockUser(roles = "MANAGER")
        void createMenu_withImage() throws Exception {
            CreateMenuRequest request = new CreateMenuRequest("김치찌개", 8000, "맛있는 김치찌개", 1L, null);
            MenuResponse response = MenuResponse.builder()
                    .id(1L).name("김치찌개").price(8000).imageUrl("https://s3.com/image.jpg")
                    .categoryId(1L).displayOrder(0).build();

            when(fileStorageService.uploadImage(eq(STORE_ID), any())).thenReturn("https://s3.com/image.jpg");
            when(menuService.createMenu(eq(STORE_ID), any(CreateMenuRequest.class), eq("https://s3.com/image.jpg")))
                    .thenReturn(response);

            MockMultipartFile menuPart = new MockMultipartFile(
                    "menu", "", "application/json",
                    objectMapper.writeValueAsBytes(request));
            MockMultipartFile imagePart = new MockMultipartFile(
                    "image", "test.jpg", "image/jpeg", new byte[1024]);

            mockMvc.perform(multipart("/api/admin/menus")
                            .file(menuPart)
                            .file(imagePart)
                            .requestAttr("storeId", STORE_ID)
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.imageUrl").value("https://s3.com/image.jpg"));
        }
    }

    @Nested
    @DisplayName("메뉴 삭제")
    class MenuDelete {

        @Test
        @DisplayName("DELETE /api/admin/menus/{menuId} - 메뉴 삭제")
        @WithMockUser(roles = "OWNER")
        void deleteMenu() throws Exception {
            doNothing().when(menuService).deleteMenu(STORE_ID, 1L);

            mockMvc.perform(delete("/api/admin/menus/{menuId}", 1L)
                            .requestAttr("storeId", STORE_ID)
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /api/admin/menus/{menuId} - 존재하지 않는 메뉴 404")
        @WithMockUser(roles = "OWNER")
        void deleteMenu_notFound() throws Exception {
            doThrow(new NotFoundException("메뉴를 찾을 수 없습니다."))
                    .when(menuService).deleteMenu(STORE_ID, 999L);

            mockMvc.perform(delete("/api/admin/menus/{menuId}", 999L)
                            .requestAttr("storeId", STORE_ID)
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("메뉴 순서 변경")
    class MenuOrder {

        @Test
        @DisplayName("PATCH /api/admin/menus/order - 메뉴 순서 변경")
        @WithMockUser(roles = "MANAGER")
        void updateMenuOrder() throws Exception {
            List<MenuOrderRequest> requests = List.of(
                    new MenuOrderRequest(1L, 2),
                    new MenuOrderRequest(2L, 1));

            doNothing().when(menuService).updateMenuDisplayOrder(eq(STORE_ID), anyList());

            mockMvc.perform(patch("/api/admin/menus/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requests))
                            .requestAttr("storeId", STORE_ID)
                            .with(csrf()))
                    .andExpect(status().isOk());
        }
    }

    // ==================== 카테고리 테스트 ====================

    @Nested
    @DisplayName("카테고리")
    class CategoryTests {

        @Test
        @DisplayName("GET /api/admin/categories - 카테고리 목록 조회")
        @WithMockUser(roles = "STAFF")
        void getCategories() throws Exception {
            CategoryResponse category = CategoryResponse.builder()
                    .id(1L).name("메인").displayOrder(0).build();

            when(menuService.getCategoriesByStore(STORE_ID)).thenReturn(List.of(category));

            mockMvc.perform(get("/api/admin/categories")
                            .requestAttr("storeId", STORE_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("메인"));
        }

        @Test
        @DisplayName("POST /api/admin/categories - 카테고리 등록")
        @WithMockUser(roles = "OWNER")
        void createCategory() throws Exception {
            CreateCategoryRequest request = new CreateCategoryRequest("메인", 0);
            CategoryResponse response = CategoryResponse.builder()
                    .id(1L).name("메인").displayOrder(0).build();

            when(menuService.createCategory(eq(STORE_ID), any(CreateCategoryRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/admin/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .requestAttr("storeId", STORE_ID)
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("메인"));
        }

        @Test
        @DisplayName("POST /api/admin/categories - 중복 카테고리명 409")
        @WithMockUser(roles = "OWNER")
        void createCategory_conflict() throws Exception {
            CreateCategoryRequest request = new CreateCategoryRequest("메인", 0);

            when(menuService.createCategory(eq(STORE_ID), any(CreateCategoryRequest.class)))
                    .thenThrow(new ConflictException("이미 존재하는 카테고리명입니다."));

            mockMvc.perform(post("/api/admin/categories")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .requestAttr("storeId", STORE_ID)
                            .with(csrf()))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("PUT /api/admin/categories/{categoryId} - 카테고리 수정")
        @WithMockUser(roles = "MANAGER")
        void updateCategory() throws Exception {
            UpdateCategoryRequest request = new UpdateCategoryRequest("메인메뉴", 1);
            CategoryResponse response = CategoryResponse.builder()
                    .id(1L).name("메인메뉴").displayOrder(1).build();

            when(menuService.updateCategory(eq(STORE_ID), eq(1L), any(UpdateCategoryRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/api/admin/categories/{categoryId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .requestAttr("storeId", STORE_ID)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("메인메뉴"));
        }

        @Test
        @DisplayName("DELETE /api/admin/categories/{categoryId} - 카테고리 삭제")
        @WithMockUser(roles = "OWNER")
        void deleteCategory() throws Exception {
            doNothing().when(menuService).deleteCategory(STORE_ID, 1L);

            mockMvc.perform(delete("/api/admin/categories/{categoryId}", 1L)
                            .requestAttr("storeId", STORE_ID)
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /api/admin/categories/{categoryId} - 메뉴 존재 시 409")
        @WithMockUser(roles = "OWNER")
        void deleteCategory_hasMenus() throws Exception {
            doThrow(new ConflictException("메뉴가 존재하는 카테고리는 삭제할 수 없습니다."))
                    .when(menuService).deleteCategory(STORE_ID, 1L);

            mockMvc.perform(delete("/api/admin/categories/{categoryId}", 1L)
                            .requestAttr("storeId", STORE_ID)
                            .with(csrf()))
                    .andExpect(status().isConflict());
        }
    }
}

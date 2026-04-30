package com.tableorder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tableorder.dto.*;
import com.tableorder.entity.StoreTable;
import com.tableorder.infrastructure.SseEmitterManager;
import com.tableorder.repository.StoreTableRepository;
import com.tableorder.service.AdminTableService;
import com.tableorder.service.OrderHistoryService;
import com.tableorder.service.TableSessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminTableController.class)
class AdminTableControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminTableService adminTableService;
    @MockBean
    private TableSessionService tableSessionService;
    @MockBean
    private OrderHistoryService orderHistoryService;
    @MockBean
    private StoreTableRepository storeTableRepository;
    @MockBean
    private SseEmitterManager sseEmitterManager;

    @Test
    @DisplayName("POST /api/admin/tables - 테이블 생성 성공")
    @WithMockUser(roles = "OWNER")
    void createTable_success() throws Exception {
        // given
        CreateTableRequest request = new CreateTableRequest(5, "1234");
        TableResponse response = TableResponse.builder()
                .id(1L).storeId(100L).tableNumber(5).createdAt(LocalDateTime.now())
                .build();

        given(adminTableService.createOrUpdateTable(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/admin/tables")
                        .with(csrf())
                        .requestAttr("storeId", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/admin/tables - 테이블 목록 조회")
    @WithMockUser(roles = "OWNER")
    void getTables_success() throws Exception {
        // given
        List<TableSummaryResponse> tables = List.of(
                TableSummaryResponse.builder().tableId(1L).tableNumber(1).hasActiveSession(true).build(),
                TableSummaryResponse.builder().tableId(2L).tableNumber(2).hasActiveSession(false).build()
        );

        given(adminTableService.getTables(any())).willReturn(tables);

        // when & then
        mockMvc.perform(get("/api/admin/tables")
                        .requestAttr("storeId", 100L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/admin/tables/{tableId}/complete - 이용 완료 성공")
    @WithMockUser(roles = "MANAGER")
    void completeTable_success() throws Exception {
        // given
        StoreTable table = StoreTable.builder().storeId(100L).tableNumber(1).passwordHash("hash").build();
        given(storeTableRepository.findById(1L)).willReturn(Optional.of(table));

        // when & then
        mockMvc.perform(post("/api/admin/tables/1/complete")
                        .with(csrf())
                        .requestAttr("storeId", 100L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/admin/tables/{tableId}/history - 이력 조회")
    @WithMockUser(roles = "STAFF")
    void getTableHistory_success() throws Exception {
        // given
        StoreTable table = StoreTable.builder().storeId(100L).tableNumber(1).passwordHash("hash").build();
        given(storeTableRepository.findById(1L)).willReturn(Optional.of(table));
        given(orderHistoryService.getTableHistory(eq(1L), any(), any())).willReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/admin/tables/1/history")
                        .requestAttr("storeId", 100L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/admin/tables - 유효성 검증 실패 (테이블 번호 0)")
    @WithMockUser(roles = "OWNER")
    void createTable_validationError() throws Exception {
        // given
        String invalidRequest = "{\"tableNumber\": 0, \"password\": \"1234\"}";

        // when & then
        mockMvc.perform(post("/api/admin/tables")
                        .with(csrf())
                        .requestAttr("storeId", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}

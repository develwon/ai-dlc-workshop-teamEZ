package com.tableorder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tableorder.dto.*;
import com.tableorder.security.JwtTokenProvider;
import com.tableorder.service.OrderEventService;
import com.tableorder.service.OrderService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminOrderController.class)
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderEventService orderEventService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("GET /api/admin/orders - 관리자 주문 목록 조회")
    @WithMockUser(roles = "OWNER")
    void getOrders_success() throws Exception {
        // given
        AdminOrderResponse response = AdminOrderResponse.builder()
                .id(1L)
                .orderNumber("20260430-120000-ABCD")
                .tableId(1L)
                .tableNumber(1)
                .totalAmount(18000)
                .status("PENDING")
                .itemCount(2)
                .createdAt(LocalDateTime.now())
                .build();

        given(orderService.getAdminOrders(any(), any()))
                .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/admin/orders")
                        .requestAttr("storeId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("20260430-120000-ABCD"));
    }

    @Test
    @DisplayName("GET /api/admin/orders/{orderId} - 주문 상세 조회")
    @WithMockUser(roles = "OWNER")
    void getOrderDetail_success() throws Exception {
        // given
        AdminOrderDetailResponse response = AdminOrderDetailResponse.builder()
                .id(1L)
                .orderNumber("20260430-120000-ABCD")
                .tableId(1L)
                .tableNumber(1)
                .totalAmount(18000)
                .status("PENDING")
                .items(List.of(OrderItemResponse.builder()
                        .menuId(1L)
                        .menuName("김치찌개")
                        .quantity(2)
                        .unitPrice(9000)
                        .subtotal(18000)
                        .build()))
                .createdAt(LocalDateTime.now())
                .build();

        given(orderService.getOrderDetail(eq(1L), any()))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/api/admin/orders/1")
                        .requestAttr("storeId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("20260430-120000-ABCD"))
                .andExpect(jsonPath("$.items[0].menuName").value("김치찌개"));
    }

    @Test
    @DisplayName("PATCH /api/admin/orders/{orderId}/status - 주문 상태 변경")
    @WithMockUser(roles = "OWNER")
    void updateOrderStatus_success() throws Exception {
        // given
        StatusUpdateRequest request = new StatusUpdateRequest("PREPARING");

        AdminOrderResponse response = AdminOrderResponse.builder()
                .id(1L)
                .orderNumber("20260430-120000-ABCD")
                .tableId(1L)
                .tableNumber(1)
                .totalAmount(18000)
                .status("PREPARING")
                .itemCount(2)
                .createdAt(LocalDateTime.now())
                .build();

        given(orderService.updateOrderStatus(eq(1L), any(), eq("PREPARING")))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/admin/orders/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("storeId", 1L)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREPARING"));
    }

    @Test
    @DisplayName("DELETE /api/admin/orders/{orderId} - 주문 삭제")
    @WithMockUser(roles = "OWNER")
    void deleteOrder_success() throws Exception {
        // given
        willDoNothing().given(orderService).deleteOrder(eq(1L), any());

        // when & then
        mockMvc.perform(delete("/api/admin/orders/1")
                        .with(csrf())
                        .requestAttr("storeId", 1L))
                .andExpect(status().isNoContent());
    }
}

package com.tableorder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tableorder.dto.CreateOrderRequest;
import com.tableorder.dto.OrderItemRequest;
import com.tableorder.dto.OrderItemResponse;
import com.tableorder.dto.OrderResponse;
import com.tableorder.security.JwtTokenProvider;
import com.tableorder.security.StoreIsolationValidator;
import com.tableorder.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
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

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private StoreIsolationValidator storeIsolationValidator;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("POST /api/orders - 주문 생성 성공")
    @WithMockUser(roles = "TABLE")
    void createOrder_success() throws Exception {
        // given
        CreateOrderRequest request = new CreateOrderRequest(
                1L, 1L, null,
                List.of(new OrderItemRequest(1L, 2))
        );

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .orderNumber("20260430-120000-ABCD")
                .storeId(1L)
                .tableId(1L)
                .tableNumber(1)
                .sessionId(1L)
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

        given(orderService.createOrder(any(CreateOrderRequest.class), any()))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .requestAttr("storeId", 1L)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("20260430-120000-ABCD"))
                .andExpect(jsonPath("$.totalAmount").value(18000))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/tables/{tableId}/orders - 테이블 주문 조회 성공")
    @WithMockUser(roles = "TABLE")
    void getTableOrders_success() throws Exception {
        // given
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .orderNumber("20260430-120000-ABCD")
                .storeId(1L)
                .tableId(1L)
                .tableNumber(1)
                .sessionId(1L)
                .totalAmount(18000)
                .status("PENDING")
                .items(List.of())
                .createdAt(LocalDateTime.now())
                .build();

        given(orderService.getTableOrders(eq(1L), any(), any()))
                .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/tables/1/orders")
                        .requestAttr("storeId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("20260430-120000-ABCD"));
    }
}

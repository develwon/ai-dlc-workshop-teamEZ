package com.tableorder.controller.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tableorder.dto.TableLoginRequest;
import com.tableorder.dto.TableLoginResponse;
import com.tableorder.security.JwtTokenProvider;
import com.tableorder.service.TableAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TableAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class TableAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TableAuthService tableAuthService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("테이블 로그인 성공 - 200 OK")
    void testLoginSuccess() throws Exception {
        // given
        TableLoginRequest request = new TableLoginRequest("STORE001", 1, "password123");

        TableLoginResponse response = TableLoginResponse.builder()
                .token("jwt-token")
                .tableId(10L)
                .storeId(1L)
                .sessionId(null)
                .build();

        when(tableAuthService.authenticate(any(TableLoginRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/tables/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tableId").value(10))
                .andExpect(jsonPath("$.storeId").value(1))
                .andExpect(jsonPath("$.sessionId").isEmpty());
    }

    @Test
    @DisplayName("테이블 로그인 실패 - 유효성 검증 오류 (빈 storeCode)")
    void testLoginValidationError() throws Exception {
        // given
        TableLoginRequest request = new TableLoginRequest("", 1, "password123");

        // when & then
        mockMvc.perform(post("/api/tables/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

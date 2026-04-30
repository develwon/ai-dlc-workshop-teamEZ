package com.tableorder.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tableorder.dto.AdminLoginRequest;
import com.tableorder.dto.AdminLoginResponse;
import com.tableorder.enums.AdminRole;
import com.tableorder.security.JwtTokenProvider;
import com.tableorder.service.AdminAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminAuthService adminAuthService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("관리자 로그인 성공 - 200 OK")
    void testLoginSuccess() throws Exception {
        // given
        AdminLoginRequest request = new AdminLoginRequest("STORE001", "admin", "password123");

        AdminLoginResponse response = AdminLoginResponse.builder()
                .token("admin-jwt-token")
                .role(AdminRole.OWNER)
                .storeId(1L)
                .storeName("테스트 매장")
                .build();

        when(adminAuthService.authenticate(any(AdminLoginRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("admin-jwt-token"))
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andExpect(jsonPath("$.storeId").value(1))
                .andExpect(jsonPath("$.storeName").value("테스트 매장"));
    }

    @Test
    @DisplayName("관리자 로그인 실패 - 유효성 검증 오류 (빈 username)")
    void testLoginValidationError() throws Exception {
        // given
        AdminLoginRequest request = new AdminLoginRequest("STORE001", "", "password123");

        // when & then
        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

package com.tableorder.exception;

import com.tableorder.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("NotFoundException 처리 - 404 NOT_FOUND")
    void testHandleNotFoundException() {
        NotFoundException ex = new NotFoundException("리소스를 찾을 수 없습니다.");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("UnauthorizedException 처리 - 401 UNAUTHORIZED")
    void testHandleUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("인증이 필요합니다.");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUnauthorizedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    @DisplayName("ForbiddenException 처리 - 403 FORBIDDEN")
    void testHandleForbiddenException() {
        ForbiddenException ex = new ForbiddenException("접근 권한이 없습니다.");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleForbiddenException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("FORBIDDEN");
    }

    @Test
    @DisplayName("ConflictException 처리 - 409 CONFLICT")
    void testHandleConflictException() {
        ConflictException ex = new ConflictException("리소스 충돌이 발생했습니다.");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleConflictException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("CONFLICT");
    }

    @Test
    @DisplayName("InvalidStateTransitionException 처리 - 400 BAD_REQUEST")
    void testHandleInvalidStateTransitionException() {
        InvalidStateTransitionException ex = new InvalidStateTransitionException("잘못된 상태 전환입니다.");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidStateTransitionException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("INVALID_STATE_TRANSITION");
    }

    @Test
    @DisplayName("일반 Exception 처리 - 500 INTERNAL_SERVER_ERROR")
    void testHandleGenericException() {
        Exception ex = new Exception("예상치 못한 오류");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
    }
}

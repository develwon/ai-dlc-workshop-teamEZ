package com.tableorder.controller;

import com.tableorder.dto.*;
import com.tableorder.entity.StoreTable;
import com.tableorder.exception.ForbiddenException;
import com.tableorder.exception.NotFoundException;
import com.tableorder.infrastructure.SseEmitterManager;
import com.tableorder.repository.StoreTableRepository;
import com.tableorder.service.AdminTableService;
import com.tableorder.service.OrderHistoryService;
import com.tableorder.service.TableSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/tables")
@RequiredArgsConstructor
@Slf4j
public class AdminTableController {

    private final AdminTableService adminTableService;
    private final TableSessionService tableSessionService;
    private final OrderHistoryService orderHistoryService;
    private final StoreTableRepository storeTableRepository;
    private final SseEmitterManager sseEmitterManager;

    @PostMapping
    public ResponseEntity<TableResponse> createTable(
            @Valid @RequestBody CreateTableRequest request,
            HttpServletRequest httpRequest) {

        Long storeId = getStoreId(httpRequest);
        validateManagerOrOwner();

        TableResponse response = adminTableService.createOrUpdateTable(storeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TableSummaryResponse>> getTables(HttpServletRequest httpRequest) {
        Long storeId = getStoreId(httpRequest);

        List<TableSummaryResponse> tables = adminTableService.getTables(storeId);
        return ResponseEntity.ok(tables);
    }

    @PostMapping("/{tableId}/complete")
    public ResponseEntity<Void> completeTable(
            @PathVariable Long tableId,
            HttpServletRequest httpRequest) {

        Long storeId = getStoreId(httpRequest);
        validateManagerOrOwner();
        validateTableBelongsToStore(tableId, storeId);

        tableSessionService.completeSession(tableId, storeId);

        try {
            OrderEventData eventData = OrderEventData.builder()
                    .eventType("TABLE_COMPLETED")
                    .tableId(tableId)
                    .build();
            sseEmitterManager.sendEvent(storeId, "TABLE_COMPLETED", eventData);
        } catch (Exception e) {
            log.warn("Failed to send SSE event for table completion [tableId={}]", tableId, e);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{tableId}/history")
    public ResponseEntity<List<OrderHistoryResponse>> getTableHistory(
            @PathVariable Long tableId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "completedAt") String dateType,
            HttpServletRequest httpRequest) {

        Long storeId = getStoreId(httpRequest);
        validateTableBelongsToStore(tableId, storeId);

        List<OrderHistoryResponse> history = orderHistoryService.getTableHistory(tableId, date, dateType);
        return ResponseEntity.ok(history);
    }

    private Long getStoreId(HttpServletRequest request) {
        return (Long) request.getAttribute("storeId");
    }

    private void validateManagerOrOwner() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean hasPermission = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> "ROLE_OWNER".equals(role) || "ROLE_MANAGER".equals(role));
        if (!hasPermission) {
            throw new ForbiddenException("매장주 또는 매니저만 이 작업을 수행할 수 있습니다.");
        }
    }

    private void validateTableBelongsToStore(Long tableId, Long storeId) {
        StoreTable table = storeTableRepository.findById(tableId)
                .orElseThrow(() -> new NotFoundException("테이블을 찾을 수 없습니다."));
        if (!table.getStoreId().equals(storeId)) {
            throw new ForbiddenException("해당 테이블에 대한 접근 권한이 없습니다.");
        }
    }
}

package com.tableorder.controller;

import com.tableorder.dto.AdminOrderDetailResponse;
import com.tableorder.dto.AdminOrderResponse;
import com.tableorder.dto.StatusUpdateRequest;
import com.tableorder.service.OrderEventService;
import com.tableorder.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final OrderEventService orderEventService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrders(HttpServletRequest httpRequest) {
        Long storeId = (Long) httpRequest.getAttribute("storeId");
        return orderEventService.subscribe(storeId);
    }

    @GetMapping
    public ResponseEntity<List<AdminOrderResponse>> getOrders(
            @RequestParam(required = false) Long tableId,
            HttpServletRequest httpRequest) {

        Long storeId = (Long) httpRequest.getAttribute("storeId");
        List<AdminOrderResponse> orders = orderService.getAdminOrders(storeId, tableId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<AdminOrderDetailResponse> getOrderDetail(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {

        Long storeId = (Long) httpRequest.getAttribute("storeId");
        AdminOrderDetailResponse detail = orderService.getOrderDetail(orderId, storeId);
        return ResponseEntity.ok(detail);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<AdminOrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody StatusUpdateRequest request,
            HttpServletRequest httpRequest) {

        Long storeId = (Long) httpRequest.getAttribute("storeId");
        AdminOrderResponse response = orderService.updateOrderStatus(orderId, storeId, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long orderId,
            HttpServletRequest httpRequest) {

        Long storeId = (Long) httpRequest.getAttribute("storeId");
        orderService.deleteOrder(orderId, storeId);
        return ResponseEntity.noContent().build();
    }
}

package com.tableorder.controller;

import com.tableorder.dto.CreateOrderRequest;
import com.tableorder.dto.OrderResponse;
import com.tableorder.security.StoreIsolationValidator;
import com.tableorder.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final StoreIsolationValidator storeIsolationValidator;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest) {

        Long storeId = (Long) httpRequest.getAttribute("storeId");
        storeIsolationValidator.validateStoreAccess(storeId, request.getStoreId());

        OrderResponse response = orderService.createOrder(request, storeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tables/{tableId}/orders")
    public ResponseEntity<List<OrderResponse>> getTableOrders(
            @PathVariable Long tableId,
            @RequestParam(required = false) Long sessionId,
            HttpServletRequest httpRequest) {

        Long storeId = (Long) httpRequest.getAttribute("storeId");

        List<OrderResponse> orders = orderService.getTableOrders(tableId, sessionId, storeId);
        return ResponseEntity.ok(orders);
    }
}

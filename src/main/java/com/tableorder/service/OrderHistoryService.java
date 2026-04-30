package com.tableorder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tableorder.dto.OrderHistoryResponse;
import com.tableorder.dto.OrderItemSnapshot;
import com.tableorder.entity.Order;
import com.tableorder.entity.OrderHistory;
import com.tableorder.entity.OrderItem;
import com.tableorder.repository.OrderHistoryRepository;
import com.tableorder.repository.OrderItemRepository;
import com.tableorder.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderHistoryService {

    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void archiveSessionOrders(Long tableId, Long sessionId, Long storeId) {
        List<Order> orders = orderRepository.findBySessionIdAndDeletedFalseOrderByCreatedAtDesc(sessionId);
        LocalDateTime completedAt = LocalDateTime.now();

        List<OrderHistory> histories = new ArrayList<>();
        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            String orderItemsJson = serializeOrderItems(items);

            OrderHistory history = OrderHistory.builder()
                    .storeId(storeId)
                    .tableId(tableId)
                    .sessionId(sessionId)
                    .orderNumber(order.getOrderNumber())
                    .totalAmount(order.getTotalAmount())
                    .orderItems(orderItemsJson)
                    .orderedAt(order.getCreatedAt())
                    .completedAt(completedAt)
                    .build();
            histories.add(history);
        }

        orderHistoryRepository.saveAll(histories);
        log.info("Archived {} orders for session {} [tableId={}, storeId={}]",
                histories.size(), sessionId, tableId, storeId);
    }

    @Transactional(readOnly = true)
    public List<OrderHistoryResponse> getTableHistory(Long tableId, LocalDate date, String dateType) {
        List<OrderHistory> histories;

        if (date == null) {
            histories = orderHistoryRepository.findByTableIdOrderByCompletedAtDesc(tableId);
        } else {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            if ("orderedAt".equals(dateType)) {
                histories = orderHistoryRepository
                        .findByTableIdAndOrderedAtBetweenOrderByOrderedAtDesc(tableId, startOfDay, endOfDay);
            } else {
                histories = orderHistoryRepository
                        .findByTableIdAndCompletedAtBetweenOrderByCompletedAtDesc(tableId, startOfDay, endOfDay);
            }
        }

        return histories.stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderHistoryResponse toResponse(OrderHistory history) {
        List<OrderItemSnapshot> items = deserializeOrderItems(history.getOrderItems());
        return OrderHistoryResponse.builder()
                .id(history.getId())
                .orderNumber(history.getOrderNumber())
                .totalAmount(history.getTotalAmount())
                .orderItems(items)
                .orderedAt(history.getOrderedAt())
                .completedAt(history.getCompletedAt())
                .build();
    }

    private String serializeOrderItems(List<OrderItem> items) {
        try {
            List<OrderItemSnapshot> snapshots = items.stream()
                    .map(item -> new OrderItemSnapshot(
                            item.getMenuName(),
                            item.getQuantity(),
                            item.getUnitPrice()))
                    .toList();
            return objectMapper.writeValueAsString(snapshots);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize order items", e);
            throw new RuntimeException("주문 항목 직렬화에 실패했습니다.", e);
        }
    }

    private List<OrderItemSnapshot> deserializeOrderItems(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize order items: {}", json, e);
            return List.of();
        }
    }
}

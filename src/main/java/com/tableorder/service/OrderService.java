package com.tableorder.service;

import com.tableorder.dto.*;
import com.tableorder.entity.Menu;
import com.tableorder.entity.Order;
import com.tableorder.entity.OrderItem;
import com.tableorder.entity.StoreTable;
import com.tableorder.entity.TableSession;
import com.tableorder.enums.OrderStatus;
import com.tableorder.exception.InvalidStateTransitionException;
import com.tableorder.exception.NotFoundException;
import com.tableorder.infrastructure.OrderNumberGenerator;
import com.tableorder.repository.MenuRepository;
import com.tableorder.repository.OrderRepository;
import com.tableorder.repository.StoreTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final StoreTableRepository storeTableRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final TableSessionService tableSessionService;
    private final OrderEventService orderEventService;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Long storeId) {
        // 테이블 검증
        StoreTable table = storeTableRepository.findByIdAndStoreId(request.getTableId(), storeId)
                .orElseThrow(() -> new NotFoundException(
                        "테이블을 찾을 수 없습니다. ID: " + request.getTableId()));

        // 세션 조회 또는 생성
        TableSession session;
        if (request.getSessionId() != null) {
            session = tableSessionService.getSessionById(request.getSessionId());
            if (!session.isActive()) {
                throw new InvalidStateTransitionException("종료된 세션에는 주문할 수 없습니다.");
            }
        } else {
            session = tableSessionService.getOrCreateSession(request.getTableId());
        }

        // 주문 항목 생성 및 메뉴 유효성 검증
        List<OrderItem> orderItems = new ArrayList<>();
        int totalAmount = 0;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Menu menu = menuRepository.findByIdAndStoreId(itemRequest.getMenuId(), storeId)
                    .orElseThrow(() -> new NotFoundException(
                            "메뉴를 찾을 수 없습니다. ID: " + itemRequest.getMenuId()));

            OrderItem orderItem = OrderItem.builder()
                    .menuId(menu.getId())
                    .menuName(menu.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(menu.getPrice())
                    .build();

            orderItems.add(orderItem);
            totalAmount += orderItem.getSubtotal();
        }

        // 주문 생성
        String orderNumber = orderNumberGenerator.generate();
        Order order = Order.builder()
                .storeId(storeId)
                .tableId(request.getTableId())
                .sessionId(session.getId())
                .orderNumber(orderNumber)
                .totalAmount(totalAmount)
                .build();

        for (OrderItem item : orderItems) {
            order.addOrderItem(item);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("주문 생성 완료. 주문번호: {}, 매장: {}, 테이블: {}, 금액: {}",
                orderNumber, storeId, table.getTableNumber(), totalAmount);

        // SSE 이벤트 발행
        OrderEventData eventData = OrderEventData.builder()
                .eventType("ORDER_CREATED")
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .tableId(savedOrder.getTableId())
                .tableNumber(table.getTableNumber())
                .totalAmount(savedOrder.getTotalAmount())
                .status(savedOrder.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();
        orderEventService.publishOrderCreated(storeId, eventData);

        return toOrderResponse(savedOrder, table.getTableNumber());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getTableOrders(Long tableId, Long sessionId, Long storeId) {
        // 테이블 검증
        StoreTable table = storeTableRepository.findByIdAndStoreId(tableId, storeId)
                .orElseThrow(() -> new NotFoundException("테이블을 찾을 수 없습니다. ID: " + tableId));

        List<Order> orders;
        if (sessionId != null) {
            orders = orderRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
        } else {
            // 활성 세션의 주문만 조회
            TableSession activeSession = tableSessionService.getActiveSession(tableId)
                    .orElse(null);
            if (activeSession == null) {
                return List.of();
            }
            orders = orderRepository.findBySessionIdOrderByCreatedAtDesc(activeSession.getId());
        }

        return orders.stream()
                .map(order -> toOrderResponse(order, table.getTableNumber()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminOrderResponse> getAdminOrders(Long storeId, Long tableId) {
        List<Order> orders;
        if (tableId != null) {
            orders = orderRepository.findByStoreIdAndTableIdOrderByCreatedAtDesc(storeId, tableId);
        } else {
            orders = orderRepository.findByStoreIdOrderByCreatedAtDesc(storeId);
        }

        return orders.stream()
                .map(this::toAdminOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminOrderDetailResponse getOrderDetail(Long orderId, Long storeId) {
        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다. ID: " + orderId));

        StoreTable table = storeTableRepository.findById(order.getTableId()).orElse(null);
        Integer tableNumber = table != null ? table.getTableNumber() : null;

        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .menuId(item.getMenuId())
                        .menuName(item.getMenuName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return AdminOrderDetailResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .tableId(order.getTableId())
                .tableNumber(tableNumber)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .items(items)
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Transactional
    public AdminOrderResponse updateOrderStatus(Long orderId, Long storeId, String statusStr) {
        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다. ID: " + orderId));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStateTransitionException("유효하지 않은 주문 상태입니다: " + statusStr);
        }

        order.updateStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        log.info("주문 상태 변경. 주문번호: {}, 상태: {} -> {}", order.getOrderNumber(), order.getStatus(), newStatus);

        // SSE 이벤트 발행
        StoreTable table = storeTableRepository.findById(order.getTableId()).orElse(null);
        Integer tableNumber = table != null ? table.getTableNumber() : null;

        OrderEventData eventData = OrderEventData.builder()
                .eventType("ORDER_STATUS_CHANGED")
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .tableId(savedOrder.getTableId())
                .tableNumber(tableNumber)
                .totalAmount(savedOrder.getTotalAmount())
                .status(savedOrder.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();
        orderEventService.publishOrderStatusChanged(storeId, eventData);

        return toAdminOrderResponse(savedOrder);
    }

    @Transactional
    public void deleteOrder(Long orderId, Long storeId) {
        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다. ID: " + orderId));

        StoreTable table = storeTableRepository.findById(order.getTableId()).orElse(null);
        Integer tableNumber = table != null ? table.getTableNumber() : null;

        // SSE 이벤트 발행 (삭제 전에 데이터 수집)
        OrderEventData eventData = OrderEventData.builder()
                .eventType("ORDER_DELETED")
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .tableId(order.getTableId())
                .tableNumber(tableNumber)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();

        orderRepository.delete(order);
        log.info("주문 삭제 완료. 주문번호: {}, 매장: {}", order.getOrderNumber(), storeId);

        orderEventService.publishOrderDeleted(storeId, eventData);
    }

    // === Private Helper Methods ===

    private OrderResponse toOrderResponse(Order order, Integer tableNumber) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .menuId(item.getMenuId())
                        .menuName(item.getMenuName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .storeId(order.getStoreId())
                .tableId(order.getTableId())
                .tableNumber(tableNumber)
                .sessionId(order.getSessionId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .items(items)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private AdminOrderResponse toAdminOrderResponse(Order order) {
        StoreTable table = storeTableRepository.findById(order.getTableId()).orElse(null);
        Integer tableNumber = table != null ? table.getTableNumber() : null;

        return AdminOrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .tableId(order.getTableId())
                .tableNumber(tableNumber)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .itemCount(order.getOrderItems().size())
                .createdAt(order.getCreatedAt())
                .build();
    }
}

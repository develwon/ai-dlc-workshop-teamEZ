package com.tableorder.service;

import com.tableorder.dto.*;
import com.tableorder.entity.*;
import com.tableorder.enums.OrderStatus;
import com.tableorder.exception.InvalidStateTransitionException;
import com.tableorder.exception.NotFoundException;
import com.tableorder.infrastructure.OrderNumberGenerator;
import com.tableorder.repository.MenuRepository;
import com.tableorder.repository.OrderRepository;
import com.tableorder.repository.StoreTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private StoreTableRepository storeTableRepository;

    @Mock
    private OrderNumberGenerator orderNumberGenerator;

    @Mock
    private TableSessionService tableSessionService;

    @Mock
    private OrderEventService orderEventService;

    private static final Long STORE_ID = 1L;
    private static final Long TABLE_ID = 1L;
    private static final Long SESSION_ID = 1L;

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        private StoreTable storeTable;
        private TableSession tableSession;
        private Menu menu;

        @BeforeEach
        void setUp() {
            storeTable = StoreTable.builder()
                    .storeId(STORE_ID)
                    .tableNumber(1)
                    .passwordHash("hash")
                    .build();

            tableSession = TableSession.builder()
                    .tableId(TABLE_ID)
                    .build();

            menu = Menu.builder()
                    .storeId(STORE_ID)
                    .categoryId(1L)
                    .name("김치찌개")
                    .price(9000)
                    .build();
        }

        @Test
        @DisplayName("정상적으로 주문을 생성한다")
        void createOrder_success() {
            // given
            CreateOrderRequest request = new CreateOrderRequest(
                    STORE_ID, TABLE_ID, null,
                    List.of(new OrderItemRequest(1L, 2))
            );

            given(storeTableRepository.findByIdAndStoreId(TABLE_ID, STORE_ID))
                    .willReturn(Optional.of(storeTable));
            given(tableSessionService.getOrCreateSession(TABLE_ID))
                    .willReturn(tableSession);
            given(menuRepository.findByIdAndStoreId(1L, STORE_ID))
                    .willReturn(Optional.of(menu));
            given(orderNumberGenerator.generate())
                    .willReturn("20260430-120000-ABCD");
            given(orderRepository.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            OrderResponse response = orderService.createOrder(request, STORE_ID);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getOrderNumber()).isEqualTo("20260430-120000-ABCD");
            assertThat(response.getTotalAmount()).isEqualTo(18000);
            assertThat(response.getStatus()).isEqualTo("PENDING");
            assertThat(response.getItems()).hasSize(1);

            then(orderRepository).should().save(any(Order.class));
            then(orderEventService).should().publishOrderCreated(eq(STORE_ID), any(OrderEventData.class));
        }

        @Test
        @DisplayName("존재하지 않는 테이블로 주문 시 예외 발생")
        void createOrder_tableNotFound() {
            // given
            CreateOrderRequest request = new CreateOrderRequest(
                    STORE_ID, 999L, null,
                    List.of(new OrderItemRequest(1L, 1))
            );

            given(storeTableRepository.findByIdAndStoreId(999L, STORE_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(request, STORE_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("테이블을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 메뉴로 주문 시 예외 발생")
        void createOrder_menuNotFound() {
            // given
            CreateOrderRequest request = new CreateOrderRequest(
                    STORE_ID, TABLE_ID, null,
                    List.of(new OrderItemRequest(999L, 1))
            );

            given(storeTableRepository.findByIdAndStoreId(TABLE_ID, STORE_ID))
                    .willReturn(Optional.of(storeTable));
            given(tableSessionService.getOrCreateSession(TABLE_ID))
                    .willReturn(tableSession);
            given(menuRepository.findByIdAndStoreId(999L, STORE_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(request, STORE_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("메뉴를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("주문 상태 변경")
    class UpdateOrderStatus {

        @Test
        @DisplayName("PENDING → PREPARING 상태 변경 성공")
        void updateStatus_pendingToPreparing() {
            // given
            Order order = Order.builder()
                    .storeId(STORE_ID)
                    .tableId(TABLE_ID)
                    .sessionId(SESSION_ID)
                    .orderNumber("20260430-120000-ABCD")
                    .totalAmount(18000)
                    .build();

            given(orderRepository.findByIdAndStoreId(1L, STORE_ID))
                    .willReturn(Optional.of(order));
            given(orderRepository.save(any(Order.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));
            given(storeTableRepository.findById(TABLE_ID))
                    .willReturn(Optional.empty());

            // when
            AdminOrderResponse response = orderService.updateOrderStatus(1L, STORE_ID, "PREPARING");

            // then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo("PREPARING");
            then(orderEventService).should().publishOrderStatusChanged(eq(STORE_ID), any(OrderEventData.class));
        }

        @Test
        @DisplayName("유효하지 않은 상태값으로 변경 시 예외 발생")
        void updateStatus_invalidStatus() {
            // given
            Order order = Order.builder()
                    .storeId(STORE_ID)
                    .tableId(TABLE_ID)
                    .sessionId(SESSION_ID)
                    .orderNumber("20260430-120000-ABCD")
                    .totalAmount(18000)
                    .build();

            given(orderRepository.findByIdAndStoreId(1L, STORE_ID))
                    .willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> orderService.updateOrderStatus(1L, STORE_ID, "INVALID"))
                    .isInstanceOf(InvalidStateTransitionException.class);
        }

        @Test
        @DisplayName("존재하지 않는 주문 상태 변경 시 예외 발생")
        void updateStatus_orderNotFound() {
            // given
            given(orderRepository.findByIdAndStoreId(999L, STORE_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.updateOrderStatus(999L, STORE_ID, "PREPARING"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("주문 삭제")
    class DeleteOrder {

        @Test
        @DisplayName("주문 삭제 성공")
        void deleteOrder_success() {
            // given
            Order order = Order.builder()
                    .storeId(STORE_ID)
                    .tableId(TABLE_ID)
                    .sessionId(SESSION_ID)
                    .orderNumber("20260430-120000-ABCD")
                    .totalAmount(18000)
                    .build();

            given(orderRepository.findByIdAndStoreId(1L, STORE_ID))
                    .willReturn(Optional.of(order));
            given(storeTableRepository.findById(TABLE_ID))
                    .willReturn(Optional.empty());

            // when
            orderService.deleteOrder(1L, STORE_ID);

            // then
            then(orderRepository).should().delete(order);
            then(orderEventService).should().publishOrderDeleted(eq(STORE_ID), any(OrderEventData.class));
        }

        @Test
        @DisplayName("존재하지 않는 주문 삭제 시 예외 발생")
        void deleteOrder_notFound() {
            // given
            given(orderRepository.findByIdAndStoreId(999L, STORE_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.deleteOrder(999L, STORE_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("주문 조회")
    class GetOrders {

        @Test
        @DisplayName("관리자 주문 목록 조회 성공")
        void getAdminOrders_success() {
            // given
            Order order = Order.builder()
                    .storeId(STORE_ID)
                    .tableId(TABLE_ID)
                    .sessionId(SESSION_ID)
                    .orderNumber("20260430-120000-ABCD")
                    .totalAmount(18000)
                    .build();

            given(orderRepository.findByStoreIdOrderByCreatedAtDesc(STORE_ID))
                    .willReturn(List.of(order));
            given(storeTableRepository.findById(TABLE_ID))
                    .willReturn(Optional.empty());

            // when
            List<AdminOrderResponse> result = orderService.getAdminOrders(STORE_ID, null);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOrderNumber()).isEqualTo("20260430-120000-ABCD");
        }

        @Test
        @DisplayName("주문 상세 조회 성공")
        void getOrderDetail_success() {
            // given
            Order order = Order.builder()
                    .storeId(STORE_ID)
                    .tableId(TABLE_ID)
                    .sessionId(SESSION_ID)
                    .orderNumber("20260430-120000-ABCD")
                    .totalAmount(18000)
                    .build();

            given(orderRepository.findByIdAndStoreId(1L, STORE_ID))
                    .willReturn(Optional.of(order));
            given(storeTableRepository.findById(TABLE_ID))
                    .willReturn(Optional.empty());

            // when
            AdminOrderDetailResponse result = orderService.getOrderDetail(1L, STORE_ID);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOrderNumber()).isEqualTo("20260430-120000-ABCD");
        }
    }
}

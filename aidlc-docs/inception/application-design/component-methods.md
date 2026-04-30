# 테이블오더 서비스 - 컴포넌트 메서드 정의

> **Note**: 상세 비즈니스 규칙은 Functional Design (CONSTRUCTION) 단계에서 정의됩니다.

---

## 1. Controller Layer 메서드

### TableAuthController
| 메서드 | HTTP | 경로 | 입력 | 출력 | 설명 |
|--------|------|------|------|------|------|
| login | POST | /api/tables/login | TableLoginRequest (storeCode, tableNumber, password) | TableLoginResponse (token, tableId, storeId, sessionId) | 테이블 태블릿 로그인 |

### CustomerMenuController
| 메서드 | HTTP | 경로 | 입력 | 출력 | 설명 |
|--------|------|------|------|------|------|
| getCategories | GET | /api/stores/{storeId}/categories | storeId (Path) | List\<CategoryResponse\> | 카테고리 목록 조회 |
| getMenusByCategory | GET | /api/stores/{storeId}/menus | storeId (Path), categoryId (Query, optional) | List\<MenuResponse\> | 메뉴 목록 조회 (카테고리 필터) |
| getMenuDetail | GET | /api/stores/{storeId}/menus/{menuId} | storeId, menuId (Path) | MenuDetailResponse | 메뉴 상세 조회 |

### OrderController
| 메서드 | HTTP | 경로 | 입력 | 출력 | 설명 |
|--------|------|------|------|------|------|
| createOrder | POST | /api/orders | CreateOrderRequest (storeId, tableId, sessionId, items[]) | OrderResponse (orderId, orderNumber, totalAmount) | 주문 생성 |
| getTableOrders | GET | /api/tables/{tableId}/orders | tableId (Path), sessionId (Query) | List\<OrderResponse\> | 현재 세션 주문 내역 조회 |

### AdminAuthController
| 메서드 | HTTP | 경로 | 입력 | 출력 | 설명 |
|--------|------|------|------|------|------|
| login | POST | /api/admin/login | AdminLoginRequest (storeCode, username, password) | AdminLoginResponse (token, role, storeName) | 관리자 로그인 |
| logout | POST | /api/admin/logout | - | void | 관리자 로그아웃 |

### AdminOrderController
| 메서드 | HTTP | 경로 | 입력 | 출력 | 설명 |
|--------|------|------|------|------|------|
| streamOrders | GET | /api/admin/orders/stream | - | SseEmitter | SSE 실시간 주문 스트림 |
| getOrders | GET | /api/admin/orders | tableId (Query, optional) | List\<AdminOrderResponse\> | 주문 목록 조회 |
| getOrderDetail | GET | /api/admin/orders/{orderId} | orderId (Path) | AdminOrderDetailResponse | 주문 상세 조회 |
| updateOrderStatus | PATCH | /api/admin/orders/{orderId}/status | orderId (Path), StatusUpdateRequest (status) | AdminOrderResponse | 주문 상태 변경 |
| deleteOrder | DELETE | /api/admin/orders/{orderId} | orderId (Path) | void | 주문 삭제 |

### AdminTableController
| 메서드 | HTTP | 경로 | 입력 | 출력 | 설명 |
|--------|------|------|------|------|------|
| createTable | POST | /api/admin/tables | CreateTableRequest (tableNumber, password) | TableResponse | 테이블 초기 설정 |
| getTables | GET | /api/admin/tables | - | List\<TableSummaryResponse\> | 테이블 목록 조회 |
| completeTable | POST | /api/admin/tables/{tableId}/complete | tableId (Path) | void | 테이블 이용 완료 |
| getTableHistory | GET | /api/admin/tables/{tableId}/history | tableId (Path), date (Query, optional) | List\<OrderHistoryResponse\> | 과거 주문 내역 조회 |

### AdminMenuController
| 메서드 | HTTP | 경로 | 입력 | 출력 | 설명 |
|--------|------|------|------|------|------|
| getMenus | GET | /api/admin/menus | categoryId (Query, optional) | List\<MenuResponse\> | 메뉴 목록 조회 |
| createMenu | POST | /api/admin/menus | CreateMenuRequest (name, price, description, categoryId, image) | MenuResponse | 메뉴 등록 |
| updateMenu | PUT | /api/admin/menus/{menuId} | menuId (Path), UpdateMenuRequest | MenuResponse | 메뉴 수정 |
| deleteMenu | DELETE | /api/admin/menus/{menuId} | menuId (Path) | void | 메뉴 삭제 |
| updateMenuOrder | PATCH | /api/admin/menus/order | List\<MenuOrderRequest\> (menuId, displayOrder) | void | 메뉴 노출 순서 변경 |
| getCategories | GET | /api/admin/categories | - | List\<CategoryResponse\> | 카테고리 목록 조회 |
| createCategory | POST | /api/admin/categories | CreateCategoryRequest (name, displayOrder) | CategoryResponse | 카테고리 등록 |
| updateCategory | PUT | /api/admin/categories/{categoryId} | categoryId (Path), UpdateCategoryRequest | CategoryResponse | 카테고리 수정 |
| deleteCategory | DELETE | /api/admin/categories/{categoryId} | categoryId (Path) | void | 카테고리 삭제 |

---

## 2. Service Layer 메서드

### TableAuthService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| authenticate | storeCode, tableNumber, password | TableLoginResponse | 테이블 인증 및 토큰 발급 |
| validateTableToken | token | TablePrincipal | 테이블 토큰 검증 |

### MenuService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| getCategoriesByStore | storeId | List\<Category\> | 매장별 카테고리 조회 |
| getMenusByStore | storeId, categoryId (optional) | List\<Menu\> | 매장별 메뉴 조회 |
| getMenuDetail | storeId, menuId | Menu | 메뉴 상세 조회 |
| createMenu | storeId, CreateMenuRequest | Menu | 메뉴 등록 |
| updateMenu | storeId, menuId, UpdateMenuRequest | Menu | 메뉴 수정 |
| deleteMenu | storeId, menuId | void | 메뉴 삭제 |
| updateMenuDisplayOrder | storeId, List\<MenuOrderRequest\> | void | 메뉴 순서 변경 |
| createCategory | storeId, CreateCategoryRequest | Category | 카테고리 등록 |
| updateCategory | storeId, categoryId, UpdateCategoryRequest | Category | 카테고리 수정 |
| deleteCategory | storeId, categoryId | void | 카테고리 삭제 |

### OrderService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| createOrder | CreateOrderRequest | Order | 주문 생성 |
| getOrdersBySession | tableId, sessionId | List\<Order\> | 세션별 주문 조회 |
| getOrdersByStore | storeId, tableId (optional) | List\<Order\> | 매장별 주문 조회 |
| getOrderDetail | orderId | Order | 주문 상세 조회 |
| updateOrderStatus | orderId, OrderStatus | Order | 주문 상태 변경 |
| deleteOrder | orderId | void | 주문 삭제 |

### TableSessionService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| getOrCreateSession | tableId | TableSession | 현재 활성 세션 조회 또는 생성 |
| completeSession | tableId | void | 테이블 이용 완료 (세션 종료) |
| getActiveSession | tableId | TableSession (nullable) | 활성 세션 조회 |

### AdminAuthService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| authenticate | storeCode, username, password | AdminLoginResponse | 관리자 인증 및 JWT 발급 |
| validateToken | token | AdminPrincipal | JWT 검증 |

### OrderEventService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| subscribe | storeId | SseEmitter | SSE 구독 등록 |
| publishOrderEvent | storeId, OrderEvent | void | 주문 이벤트 발행 |
| removeEmitter | storeId, SseEmitter | void | SSE 연결 제거 |

### OrderHistoryService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| archiveSessionOrders | tableId, sessionId | void | 세션 주문을 이력으로 이동 |
| getTableHistory | tableId, date (optional) | List\<OrderHistory\> | 과거 주문 이력 조회 |

### FileStorageService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| uploadImage | MultipartFile | String (imageUrl) | S3 이미지 업로드 |
| deleteImage | imageUrl | void | S3 이미지 삭제 |

---

## 3. Repository Layer 메서드

### StoreRepository
| 메서드 | 설명 |
|--------|------|
| findByStoreCode(storeCode) | 매장 코드로 매장 조회 |

### AdminRepository
| 메서드 | 설명 |
|--------|------|
| findByStoreIdAndUsername(storeId, username) | 매장+사용자명으로 관리자 조회 |

### StoreTableRepository
| 메서드 | 설명 |
|--------|------|
| findByStoreIdAndTableNumber(storeId, tableNumber) | 매장+테이블번호로 테이블 조회 |
| findAllByStoreId(storeId) | 매장별 전체 테이블 조회 |

### TableSessionRepository
| 메서드 | 설명 |
|--------|------|
| findByTableIdAndEndTimeIsNull(tableId) | 활성 세션 조회 |
| findByTableIdOrderByStartTimeDesc(tableId) | 테이블별 세션 이력 조회 |

### CategoryRepository
| 메서드 | 설명 |
|--------|------|
| findByStoreIdOrderByDisplayOrder(storeId) | 매장별 카테고리 조회 (순서) |

### MenuRepository
| 메서드 | 설명 |
|--------|------|
| findByStoreIdAndCategoryIdOrderByDisplayOrder(storeId, categoryId) | 카테고리별 메뉴 조회 |
| findByStoreIdOrderByDisplayOrder(storeId) | 매장별 전체 메뉴 조회 |

### OrderRepository
| 메서드 | 설명 |
|--------|------|
| findBySessionIdOrderByCreatedAtDesc(sessionId) | 세션별 주문 조회 |
| findByStoreIdAndStatusNotOrderByCreatedAtDesc(storeId, status) | 매장별 활성 주문 조회 |

### OrderItemRepository
| 메서드 | 설명 |
|--------|------|
| findByOrderId(orderId) | 주문별 항목 조회 |

### OrderHistoryRepository
| 메서드 | 설명 |
|--------|------|
| findByTableIdOrderByCompletedAtDesc(tableId) | 테이블별 과거 이력 조회 |
| findByTableIdAndCompletedAtBetween(tableId, start, end) | 날짜 범위 이력 조회 |

# 테이블오더 서비스 - 컴포넌트 의존성

---

## 1. 의존성 매트릭스

### Controller → Service 의존성

| Controller | 의존 Service |
|------------|-------------|
| TableAuthController | TableAuthService |
| CustomerMenuController | MenuService |
| OrderController | OrderService |
| AdminAuthController | AdminAuthService |
| AdminOrderController | OrderService, OrderEventService |
| AdminTableController | TableSessionService, OrderHistoryService |
| AdminMenuController | MenuService, FileStorageService |

### Service → Repository 의존성

| Service | 의존 Repository | 의존 Service |
|---------|-----------------|-------------|
| TableAuthService | StoreRepository, StoreTableRepository | - |
| AdminAuthService | StoreRepository, AdminRepository | - |
| MenuService | MenuRepository, CategoryRepository | FileStorageService |
| OrderService | OrderRepository, OrderItemRepository, MenuRepository | TableSessionService, OrderEventService |
| TableSessionService | TableSessionRepository | OrderHistoryService |
| OrderEventService | (없음 - 인메모리) | - |
| OrderHistoryService | OrderHistoryRepository, OrderRepository, OrderItemRepository | - |
| FileStorageService | (없음 - S3 Client) | - |

### 공통 컴포넌트 의존성

| 컴포넌트 | 사용처 |
|----------|--------|
| JwtTokenProvider | TableAuthService, AdminAuthService, JwtAuthenticationFilter |
| JwtAuthenticationFilter | SecurityConfig (모든 인증 필요 요청) |
| GlobalExceptionHandler | 모든 Controller |
| SecurityConfig | 전체 애플리케이션 |

---

## 2. 데이터 흐름 다이어그램

### 고객 주문 데이터 흐름
```
+------------------+     +------------------+     +------------------+
|   Customer UI    |     |   Spring Boot    |     |   PostgreSQL     |
|   (Browser)      |     |   Backend        |     |   Database       |
+------------------+     +------------------+     +------------------+
|                        |                        |
| 1. 자동 로그인          |                        |
|  POST /api/tables/login|                        |
| ---------------------->|                        |
|                        | 2. 테이블 인증           |
|                        | ---------------------->|
|                        | <----------------------|
| <----------------------|                        |
|   (JWT Token)          |                        |
|                        |                        |
| 3. 메뉴 조회            |                        |
|  GET /api/stores/      |                        |
|      {id}/menus        |                        |
| ---------------------->|                        |
|                        | 4. 메뉴 데이터 조회      |
|                        | ---------------------->|
|                        | <----------------------|
| <----------------------|                        |
|   (Menu List)          |                        |
|                        |                        |
| 5. 주문 생성            |                        |
|  POST /api/orders      |                        |
| ---------------------->|                        |
|                        | 6. 주문 저장             |
|                        | ---------------------->|
|                        | <----------------------|
|                        |                        |
|                        | 7. SSE 이벤트 발행       |
|                        | -----> (Admin UI)      |
| <----------------------|                        |
|   (Order Response)     |                        |
+------------------+     +------------------+     +------------------+
```

### 관리자 실시간 모니터링 데이터 흐름
```
+------------------+     +------------------+     +------------------+
|   Admin UI       |     |   Spring Boot    |     |   PostgreSQL     |
|   (Browser)      |     |   Backend        |     |   Database       |
+------------------+     +------------------+     +------------------+
|                        |                        |
| 1. 로그인               |                        |
|  POST /api/admin/login |                        |
| ---------------------->|                        |
|                        | 2. 관리자 인증           |
|                        | ---------------------->|
|                        | <----------------------|
| <----------------------|                        |
|   (JWT + Role)         |                        |
|                        |                        |
| 3. SSE 구독             |                        |
|  GET /api/admin/       |                        |
|      orders/stream     |                        |
| ---------------------->|                        |
|   (SSE Connection)     |                        |
|                        |                        |
|   [고객 주문 발생 시]     |                        |
|                        | 4. SSE 이벤트 수신       |
| <~~~~~~~~~~~~~~~~~~~~~~|                        |
|   (New Order Event)    |                        |
|                        |                        |
| 5. 주문 상태 변경         |                        |
|  PATCH /api/admin/     |                        |
|    orders/{id}/status  |                        |
| ---------------------->|                        |
|                        | 6. 상태 업데이트          |
|                        | ---------------------->|
|                        | <----------------------|
| <----------------------|                        |
+------------------+     +------------------+     +------------------+
```

### 이미지 업로드 데이터 흐름
```
+------------------+     +------------------+     +------------------+
|   Admin UI       |     |   Spring Boot    |     |   AWS S3         |
|   (Browser)      |     |   Backend        |     |   Storage        |
+------------------+     +------------------+     +------------------+
|                        |                        |
| 1. 이미지 업로드         |                        |
|  POST /api/admin/menus |                        |
|  (multipart/form-data) |                        |
| ---------------------->|                        |
|                        | 2. S3 업로드             |
|                        | ---------------------->|
|                        | <----------------------|
|                        |   (Image URL)          |
|                        |                        |
|                        | 3. 메뉴 저장 (URL 포함)  |
|                        | -----> PostgreSQL      |
| <----------------------|                        |
|   (Menu + Image URL)   |                        |
+------------------+     +------------------+     +------------------+
```

---

## 3. 프론트엔드 모듈 의존성

### 고객 UI 모듈 의존성
```
app.js (진입점)
  ├── auth.js (인증/세션)
  ├── api.js (API 통신)
  ├── menu.js (메뉴 조회)
  │     └── api.js
  ├── cart.js (장바구니)
  ├── order.js (주문 생성)
  │     ├── api.js
  │     └── cart.js
  └── orderHistory.js (주문 내역)
        └── api.js
```

### 관리자 UI 모듈 의존성
```
app.js (진입점)
  ├── auth.js (인증/JWT)
  ├── api.js (API 통신 + JWT)
  ├── dashboard.js (실시간 대시보드)
  │     └── api.js (SSE)
  ├── orderManagement.js (주문 관리)
  │     └── api.js
  ├── tableManagement.js (테이블 관리)
  │     └── api.js
  └── menuManagement.js (메뉴 관리)
        └── api.js
```

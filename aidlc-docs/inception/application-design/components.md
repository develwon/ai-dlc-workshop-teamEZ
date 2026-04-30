# 테이블오더 서비스 - 컴포넌트 정의

## 아키텍처 개요
- **패턴**: 전통적 계층형 아키텍처 (Controller → Service → Repository)
- **백엔드**: Java + Spring Boot
- **프론트엔드**: HTML + JavaScript (ES Modules) + CSS
- **프론트엔드 서빙**: Spring Boot 내장 정적 리소스 (src/main/resources/static/)
- **UI 분리**: 고객 UI / 관리자 UI 완전 분리 (별도 HTML 진입점)

---

## 1. 백엔드 컴포넌트

### 1.1 Controller Layer

| 컴포넌트 | 책임 | 관련 스토리 |
|----------|------|-------------|
| **TableAuthController** | 테이블 태블릿 인증 (로그인) | US-C01 |
| **CustomerMenuController** | 고객용 메뉴 조회 | US-C02 |
| **CartController** | (클라이언트 전용 - 서버 컨트롤러 없음) | US-C03 |
| **OrderController** | 주문 생성, 주문 내역 조회 | US-C04, US-C05 |
| **AdminAuthController** | 관리자 인증 (로그인/로그아웃) | US-A01 |
| **AdminOrderController** | 관리자 주문 관리 (조회, 상태 변경, 삭제, SSE 스트림) | US-A02 |
| **AdminTableController** | 테이블 관리 (설정, 이용 완료, 과거 내역) | US-A03 |
| **AdminMenuController** | 메뉴 관리 (CRUD, 순서 변경) | US-A04 |

### 1.2 Service Layer

| 컴포넌트 | 책임 | 관련 스토리 |
|----------|------|-------------|
| **TableAuthService** | 테이블 인증 로직, 세션 토큰 발급 | US-C01 |
| **MenuService** | 메뉴/카테고리 조회, CRUD, 순서 관리 | US-C02, US-A04 |
| **OrderService** | 주문 생성, 조회, 상태 변경, 삭제 | US-C04, US-C05, US-A02 |
| **TableSessionService** | 테이블 세션 관리 (시작, 종료, 이용 완료) | US-A03 |
| **AdminAuthService** | 관리자 인증, JWT 발급/검증 | US-A01 |
| **OrderEventService** | SSE 이벤트 발행/구독 (실시간 주문 알림) | US-A02 |
| **OrderHistoryService** | 과거 주문 이력 관리 (이동, 조회) | US-A03 |
| **FileStorageService** | S3 이미지 업로드/삭제 | US-A04 |

### 1.3 Repository Layer

| 컴포넌트 | 책임 |
|----------|------|
| **StoreRepository** | 매장 데이터 접근 |
| **AdminRepository** | 관리자 계정 데이터 접근 |
| **StoreTableRepository** | 테이블 데이터 접근 |
| **TableSessionRepository** | 테이블 세션 데이터 접근 |
| **CategoryRepository** | 카테고리 데이터 접근 |
| **MenuRepository** | 메뉴 데이터 접근 |
| **OrderRepository** | 주문 데이터 접근 |
| **OrderItemRepository** | 주문 항목 데이터 접근 |
| **OrderHistoryRepository** | 과거 주문 이력 데이터 접근 |

### 1.4 공통/인프라 컴포넌트

| 컴포넌트 | 책임 |
|----------|------|
| **JwtTokenProvider** | JWT 토큰 생성, 검증, 파싱 |
| **SecurityConfig** | Spring Security 설정 (CORS, 인증 필터, 권한) |
| **JwtAuthenticationFilter** | JWT 토큰 기반 인증 필터 |
| **RoleBasedAccessControl** | 역할 기반 접근 제어 (매장주/매니저/직원) |
| **GlobalExceptionHandler** | 전역 예외 처리 (@ControllerAdvice) |
| **S3Config** | AWS S3 클라이언트 설정 |
| **SseEmitterManager** | SSE 연결 관리 (emitter 등록/제거/브로드캐스트) |

---

## 2. 프론트엔드 컴포넌트

### 2.1 고객 UI (static/customer/)

| 모듈 | 책임 |
|------|------|
| **customer/index.html** | 고객 UI 진입점 |
| **customer/js/app.js** | 앱 초기화, 라우팅, 자동 로그인 |
| **customer/js/api.js** | API 통신 모듈 (fetch wrapper) |
| **customer/js/auth.js** | 테이블 인증/세션 관리 (localStorage) |
| **customer/js/menu.js** | 메뉴 조회/탐색 UI 렌더링 |
| **customer/js/cart.js** | 장바구니 관리 (localStorage, 수량 조절, 금액 계산) |
| **customer/js/order.js** | 주문 생성/확정 처리 |
| **customer/js/orderHistory.js** | 주문 내역 조회 UI |
| **customer/css/style.css** | 고객 UI 스타일 (터치 친화적, 카드 레이아웃) |

### 2.2 관리자 UI (static/admin/)

| 모듈 | 책임 |
|------|------|
| **admin/index.html** | 관리자 UI 진입점 |
| **admin/js/app.js** | 앱 초기화, 라우팅, 인증 상태 관리 |
| **admin/js/api.js** | API 통신 모듈 (JWT 포함 fetch wrapper) |
| **admin/js/auth.js** | 관리자 인증 (로그인/로그아웃, JWT 관리) |
| **admin/js/dashboard.js** | 실시간 주문 대시보드 (SSE, 그리드 레이아웃) |
| **admin/js/orderManagement.js** | 주문 상태 변경, 주문 삭제 |
| **admin/js/tableManagement.js** | 테이블 관리 (설정, 이용 완료, 과거 내역) |
| **admin/js/menuManagement.js** | 메뉴 관리 (CRUD, 순서 변경, 이미지 업로드) |
| **admin/css/style.css** | 관리자 UI 스타일 (대시보드, 그리드) |

---

## 3. 엔티티 (Domain Model)

| 엔티티 | 설명 |
|--------|------|
| **Store** | 매장 (매장 식별자, 매장명) |
| **Admin** | 관리자 계정 (사용자명, 비밀번호 해시, 역할, 소속 매장) |
| **StoreTable** | 테이블 (테이블 번호, 비밀번호, 소속 매장) |
| **TableSession** | 테이블 세션 (세션 ID, 테이블, 시작/종료 시각) |
| **Category** | 메뉴 카테고리 (카테고리명, 노출 순서, 소속 매장) |
| **Menu** | 메뉴 항목 (메뉴명, 가격, 설명, 이미지 URL, 카테고리, 노출 순서) |
| **Order** | 주문 (주문 번호, 테이블 세션, 주문 시각, 총 금액, 상태) |
| **OrderItem** | 주문 항목 (주문, 메뉴, 수량, 단가) |
| **OrderHistory** | 과거 주문 이력 (이용 완료 시 이동) |

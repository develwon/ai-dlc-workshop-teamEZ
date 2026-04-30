# 테이블오더 서비스 - Unit of Work 정의

## 분해 방식
**기능 도메인별 유닛 + 4명 병렬 작업 최적화**: 백엔드를 기능 도메인별로 분리하고, 프론트엔드는 별도 유닛으로 구성. 4명의 개발자가 최대한 병렬로 작업할 수 있도록 의존성을 최소화.

> **Note**: 모든 유닛은 하나의 Spring Boot 모놀리식 애플리케이션 내 논리적 모듈입니다. 독립 배포 단위가 아닙니다.

---

## 병렬 작업 전략 (4명 개발자)

### Sprint 1: 기반 구축 (1명 담당)
공통 기반을 1명이 일관성 있게 구축합니다. 나머지 3명은 설계 리뷰, API 문서 검토, 테스트 케이스 설계 등을 수행합니다.

| 담당자 | 작업 |
|--------|------|
| **개발자 A** | Spring Boot 프로젝트 초기화, Entity, Security, 인프라, DTO, DB 스키마 전체 |
| **개발자 B,C,D** | 설계 리뷰, 담당 도메인 API 문서 검토, 테스트 케이스 설계 |

### Sprint 2: 도메인 백엔드 병렬 개발
기반 완료 후, 4명이 각각 독립적인 도메인 유닛을 담당합니다.

| 담당자 | 유닛 | 의존성 |
|--------|------|--------|
| **개발자 A** | Unit 2: auth-domain | Unit 1 (기반만) |
| **개발자 B** | Unit 3: menu-domain | Unit 1 (기반만) |
| **개발자 C** | Unit 4: order-domain | Unit 1 (기반만) - 인증은 인터페이스 기반으로 개발, 통합은 Sprint 3 |
| **개발자 D** | Unit 5: table-management-domain | Unit 1 (기반만) - OrderHistory는 독립 구현, 통합은 Sprint 3 |

> **병렬화 핵심**: Unit 4(주문)와 Unit 5(테이블 관리)는 공유 컴포넌트(TableSessionService)에 대해 인터페이스를 먼저 정의하고, 각자 구현 후 Sprint 3에서 통합합니다.

### Sprint 3: 통합 + 프론트엔드 병렬 개발
백엔드 통합 테스트와 프론트엔드 개발을 병렬로 진행합니다.

| 담당자 | 유닛 | 작업 |
|--------|------|------|
| **개발자 A** | Unit 6: customer-frontend | 고객 UI 전체 |
| **개발자 B** | Unit 7: admin-frontend | 관리자 UI 전체 |
| **개발자 C** | 백엔드 통합 | Unit 2~5 통합 테스트, TableSessionService 통합 |
| **개발자 D** | 백엔드 통합 | 크로스 도메인 테스트, E2E 시나리오 검증 |

---

## Unit 1: 공통 기반 (Common Foundation)

| 항목 | 내용 |
|------|------|
| **유닛명** | common-foundation |
| **유형** | 백엔드 - 공통 인프라 |
| **책임** | 프로젝트 초기 설정, 엔티티 정의, 보안 설정, 공통 컴포넌트 |
| **Sprint** | Sprint 1 (개발자 A 단독) |

**포함 컴포넌트**:
- Spring Boot 프로젝트 초기화 (pom.xml, application.yml)
- 전체 JPA Entity 클래스 (Store, Admin, StoreTable, TableSession, Category, Menu, Order, OrderItem, OrderHistory)
- DTO 클래스 (Request/Response)
- Enum 클래스 (OrderStatus, AdminRole)
- SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter
- RoleBasedAccessControl
- GlobalExceptionHandler
- S3Config
- SseEmitterManager
- DB 스키마 (schema.sql), 시드 데이터 (data.sql)

---

## Unit 2: 인증 도메인 (Auth Domain)

| 항목 | 내용 |
|------|------|
| **유닛명** | auth-domain |
| **유형** | 백엔드 - 기능 도메인 |
| **책임** | 테이블 인증, 관리자 인증, JWT 발급/검증 |
| **Sprint** | Sprint 2 (개발자 A) |
| **의존** | Unit 1 (기반만) |

**포함 컴포넌트**:
- TableAuthController, TableAuthService
- AdminAuthController, AdminAuthService
- StoreRepository, AdminRepository, StoreTableRepository

**관련 스토리**: US-C01, US-A01

---

## Unit 3: 메뉴 도메인 (Menu Domain)

| 항목 | 내용 |
|------|------|
| **유닛명** | menu-domain |
| **유형** | 백엔드 - 기능 도메인 |
| **책임** | 메뉴/카테고리 CRUD, 조회, 이미지 업로드, 순서 관리 |
| **Sprint** | Sprint 2 (개발자 B) |
| **의존** | Unit 1 (기반만) |

**포함 컴포넌트**:
- CustomerMenuController
- AdminMenuController
- MenuService
- FileStorageService
- CategoryRepository, MenuRepository

**관련 스토리**: US-C02, US-A04

---

## Unit 4: 주문 도메인 (Order Domain)

| 항목 | 내용 |
|------|------|
| **유닛명** | order-domain |
| **유형** | 백엔드 - 기능 도메인 |
| **책임** | 주문 생성, 조회, 상태 관리, 삭제, SSE 실시간 알림 |
| **Sprint** | Sprint 2 (개발자 C) |
| **의존** | Unit 1 (기반만) - 인증 통합은 Sprint 3 |

**포함 컴포넌트**:
- OrderController
- AdminOrderController
- OrderService
- OrderEventService (SSE)
- TableSessionService
- OrderRepository, OrderItemRepository, TableSessionRepository

**관련 스토리**: US-C04, US-C05, US-A02

**병렬화 참고**: TableSessionService는 이 유닛에서 정의하되, Unit 5에서 사용하는 completeSession 메서드의 인터페이스를 Sprint 2 초기에 확정합니다.

---

## Unit 5: 테이블 관리 도메인 (Table Management Domain)

| 항목 | 내용 |
|------|------|
| **유닛명** | table-management-domain |
| **유형** | 백엔드 - 기능 도메인 |
| **책임** | 테이블 설정, 이용 완료, 과거 주문 이력 관리 |
| **Sprint** | Sprint 2 (개발자 D) |
| **의존** | Unit 1 (기반만) - TableSessionService 통합은 Sprint 3 |

**포함 컴포넌트**:
- AdminTableController
- TableSessionService 확장 (이용 완료 로직)
- OrderHistoryService
- OrderHistoryRepository

**관련 스토리**: US-A03

**병렬화 참고**: Sprint 2에서는 TableSessionService의 인터페이스를 기반으로 독립 개발. Sprint 3에서 Unit 4와 통합.

---

## Unit 6: 고객 프론트엔드 (Customer Frontend)

| 항목 | 내용 |
|------|------|
| **유닛명** | customer-frontend |
| **유형** | 프론트엔드 - 고객 UI |
| **책임** | 고객용 웹 인터페이스 (메뉴 조회, 장바구니, 주문, 주문 내역) |
| **Sprint** | Sprint 3 (개발자 A) |
| **의존** | Unit 2, 3, 4 API |

**포함 컴포넌트**:
- customer/index.html
- customer/js/app.js, api.js, auth.js, menu.js, cart.js, order.js, orderHistory.js
- customer/css/style.css

**관련 스토리**: US-C01, US-C02, US-C03, US-C04, US-C05

---

## Unit 7: 관리자 프론트엔드 (Admin Frontend)

| 항목 | 내용 |
|------|------|
| **유닛명** | admin-frontend |
| **유형** | 프론트엔드 - 관리자 UI |
| **책임** | 관리자용 웹 인터페이스 (로그인, 대시보드, 주문/테이블/메뉴 관리) |
| **Sprint** | Sprint 3 (개발자 B) |
| **의존** | Unit 2, 3, 4, 5 API |

**포함 컴포넌트**:
- admin/index.html
- admin/js/app.js, api.js, auth.js, dashboard.js, orderManagement.js, tableManagement.js, menuManagement.js
- admin/css/style.css

**관련 스토리**: US-A01, US-A02, US-A03, US-A04

---

## 구현 순서 요약 (4명 병렬)

```
Sprint 1: 1명 담당 (공통 기반)
+-------+-------+-------+-------+
| Dev A | Dev B | Dev C | Dev D |
| 공통   | 설계  | 설계  | 설계  |
| 기반   | 리뷰  | 리뷰  | 리뷰  |
| 전체   | +API  | +API  | +API  |
|       | 문서  | 문서  | 문서  |
+-------+-------+-------+-------+
              |
              v
Sprint 2: 4명 병렬 (도메인 백엔드)
+-------+-------+-------+-------+
| Dev A | Dev B | Dev C | Dev D |
| Unit2 | Unit3 | Unit4 | Unit5 |
| 인증   | 메뉴  | 주문   | 테이블 |
|       |       | +SSE  | 관리   |
+-------+-------+-------+-------+
              |
              v
Sprint 3: 4명 병렬 (통합 + 프론트엔드)
+-------+-------+-------+-------+
| Dev A | Dev B | Dev C | Dev D |
| Unit6 | Unit7 | 백엔드 | 백엔드 |
| 고객UI | 관리자 | 통합   | 통합   |
|       | UI    | 테스트 | 테스트 |
+-------+-------+-------+-------+
```

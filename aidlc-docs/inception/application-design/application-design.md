# 테이블오더 서비스 - Application Design 통합 문서

---

## 1. 아키텍처 결정사항

| 결정 항목 | 선택 | 근거 |
|-----------|------|------|
| 백엔드 아키텍처 | 전통적 계층형 (Controller→Service→Repository) | 단순하고 직관적, 당사 환경과 일치 |
| 프론트엔드 구조 | 모듈 기반 (ES Modules) | 재사용성 높음, 바닐라 JS에 적합 |
| UI 분리 방식 | 완전 분리 (별도 HTML 진입점) | 고객/관리자 독립 운영, 보안 분리 |
| 정적 리소스 서빙 | Spring Boot 내장 (static/) | 단일 배포, 운영 단순화 |

---

## 2. 시스템 구성도

```
+================================================================+
|                    Spring Boot Application                      |
+================================================================+
|                                                                  |
|  +---------------------------+  +----------------------------+  |
|  |    Static Resources       |  |      REST API Layer        |  |
|  |  (src/main/resources/     |  |                            |  |
|  |   static/)                |  |  +---------------------+   |  |
|  |                           |  |  | TableAuthController |   |  |
|  |  +--------+ +--------+   |  |  | CustomerMenuCtrl    |   |  |
|  |  |Customer| | Admin  |   |  |  | OrderController     |   |  |
|  |  |  UI    | |  UI    |   |  |  | AdminAuthController |   |  |
|  |  +--------+ +--------+   |  |  | AdminOrderCtrl      |   |  |
|  |                           |  |  | AdminTableCtrl      |   |  |
|  +---------------------------+  |  | AdminMenuController |   |  |
|                                  |  +---------------------+   |  |
|                                  +----------------------------+  |
|                                              |                   |
|                                  +----------------------------+  |
|                                  |     Service Layer          |  |
|                                  |                            |  |
|                                  |  TableAuthService          |  |
|                                  |  AdminAuthService          |  |
|                                  |  MenuService               |  |
|                                  |  OrderService              |  |
|                                  |  TableSessionService       |  |
|                                  |  OrderEventService (SSE)   |  |
|                                  |  OrderHistoryService       |  |
|                                  |  FileStorageService (S3)   |  |
|                                  +----------------------------+  |
|                                              |                   |
|                                  +----------------------------+  |
|                                  |    Repository Layer        |  |
|                                  |                            |  |
|                                  |  StoreRepository           |  |
|                                  |  AdminRepository           |  |
|                                  |  StoreTableRepository      |  |
|                                  |  TableSessionRepository    |  |
|                                  |  CategoryRepository        |  |
|                                  |  MenuRepository            |  |
|                                  |  OrderRepository           |  |
|                                  |  OrderItemRepository       |  |
|                                  |  OrderHistoryRepository    |  |
|                                  +----------------------------+  |
|                                              |                   |
|  +---------------------------+               |                   |
|  |  Infrastructure           |               |                   |
|  |  JwtTokenProvider         |               |                   |
|  |  SecurityConfig           |               |                   |
|  |  JwtAuthenticationFilter  |               |                   |
|  |  GlobalExceptionHandler   |               |                   |
|  |  SseEmitterManager        |               |                   |
|  |  S3Config                 |               |                   |
|  +---------------------------+               |                   |
+==================================================+===============+
                                               |
                              +----------------+----------------+
                              |                                 |
                    +---------+--------+             +----------+---------+
                    |   PostgreSQL     |             |     AWS S3         |
                    |   Database       |             |     Storage        |
                    +------------------+             +--------------------+
```

---

## 3. 프로젝트 디렉토리 구조 (예상)

```
table-order/
+-- src/
|   +-- main/
|   |   +-- java/com/tableorder/
|   |   |   +-- config/           # SecurityConfig, S3Config, WebConfig
|   |   |   +-- security/         # JwtTokenProvider, JwtAuthFilter, RoleBasedAccessControl
|   |   |   +-- controller/
|   |   |   |   +-- customer/     # TableAuthController, CustomerMenuController, OrderController
|   |   |   |   +-- admin/        # AdminAuthController, AdminOrderController, AdminTableController, AdminMenuController
|   |   |   +-- service/          # All Service classes
|   |   |   +-- repository/       # All Repository interfaces
|   |   |   +-- entity/           # JPA Entity classes
|   |   |   +-- dto/              # Request/Response DTOs
|   |   |   +-- exception/        # Custom exceptions, GlobalExceptionHandler
|   |   |   +-- enums/            # OrderStatus, AdminRole enums
|   |   |   +-- TableOrderApplication.java
|   |   +-- resources/
|   |       +-- static/
|   |       |   +-- customer/     # 고객 UI (HTML, JS, CSS)
|   |       |   +-- admin/        # 관리자 UI (HTML, JS, CSS)
|   |       +-- application.yml
|   |       +-- schema.sql        # DB 스키마
|   |       +-- data.sql          # 시드 데이터
|   +-- test/
|       +-- java/com/tableorder/
|           +-- controller/       # Controller 단위 테스트
|           +-- service/          # Service 단위 테스트
|           +-- repository/       # Repository 단위 테스트
+-- pom.xml
```

---

## 4. 핵심 설계 요약

### 컴포넌트 수
- **Controller**: 8개 (고객 3 + 관리자 5)
- **Service**: 8개
- **Repository**: 9개
- **인프라/공통**: 7개
- **프론트엔드 모듈**: 고객 7개 + 관리자 7개

### 주요 통신 패턴
- **고객→서버**: REST API (JWT 인증)
- **관리자→서버**: REST API (JWT + RBAC)
- **서버→관리자**: SSE (실시간 주문 알림)
- **서버→S3**: AWS SDK (이미지 업로드)

### 상세 문서 참조
- 컴포넌트 정의: [components.md](components.md)
- 메서드 시그니처: [component-methods.md](component-methods.md)
- 서비스 설계: [services.md](services.md)
- 의존성 관계: [component-dependency.md](component-dependency.md)

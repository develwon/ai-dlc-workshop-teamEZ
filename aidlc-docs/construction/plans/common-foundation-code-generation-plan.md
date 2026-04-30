# Code Generation Plan - Unit 1: common-foundation

## 유닛 컨텍스트
- **유닛명**: common-foundation
- **유형**: 백엔드 공통 인프라
- **책임**: Spring Boot 프로젝트 초기화, JPA Entity, Security, 공통 컴포넌트, DB 스키마
- **관련 스토리**: 전체 (기반 인프라)
- **코드 위치**: 워크스페이스 루트 (모놀리식 Spring Boot)

## 프로젝트 구조
```
src/main/java/com/tableorder/
src/main/resources/
src/test/java/com/tableorder/
pom.xml
```

---

## 실행 계획

### Step 1: 프로젝트 초기화
- [x] pom.xml 생성 (Spring Boot 3.2.x, 모든 의존성)
- [x] src/main/java/com/tableorder/TableOrderApplication.java 생성
- [x] src/main/resources/application.yml 생성
- [x] src/main/resources/application-test.yml 생성

### Step 2: Enum 클래스 생성
- [x] src/main/java/com/tableorder/enums/OrderStatus.java
- [x] src/main/java/com/tableorder/enums/AdminRole.java

### Step 3: BaseEntity 및 JPA Entity 생성
- [x] src/main/java/com/tableorder/entity/BaseEntity.java (추상 클래스)
- [x] src/main/java/com/tableorder/entity/Store.java
- [x] src/main/java/com/tableorder/entity/Admin.java
- [x] src/main/java/com/tableorder/entity/StoreTable.java
- [x] src/main/java/com/tableorder/entity/TableSession.java
- [x] src/main/java/com/tableorder/entity/Category.java
- [x] src/main/java/com/tableorder/entity/Menu.java
- [x] src/main/java/com/tableorder/entity/Order.java
- [x] src/main/java/com/tableorder/entity/OrderItem.java
- [x] src/main/java/com/tableorder/entity/OrderHistory.java

### Step 4: DTO 클래스 생성
- [x] 공통 DTO: ErrorResponse, ValidationErrorResponse
- [x] 인증 DTO: TableLoginRequest, TableLoginResponse, AdminLoginRequest, AdminLoginResponse
- [x] 메뉴 DTO: CategoryResponse, MenuResponse, MenuDetailResponse, CreateMenuRequest, UpdateMenuRequest, MenuOrderRequest, CreateCategoryRequest, UpdateCategoryRequest
- [x] 주문 DTO: CreateOrderRequest, OrderItemRequest, OrderResponse, AdminOrderResponse, AdminOrderDetailResponse, StatusUpdateRequest, OrderEventData
- [x] 테이블 DTO: CreateTableRequest, TableResponse, TableSummaryResponse, OrderHistoryResponse

### Step 5: 커스텀 예외 클래스 생성
- [x] src/main/java/com/tableorder/exception/NotFoundException.java
- [x] src/main/java/com/tableorder/exception/UnauthorizedException.java
- [x] src/main/java/com/tableorder/exception/ForbiddenException.java
- [x] src/main/java/com/tableorder/exception/ConflictException.java
- [x] src/main/java/com/tableorder/exception/AccountLockedException.java
- [x] src/main/java/com/tableorder/exception/InvalidStateTransitionException.java
- [x] src/main/java/com/tableorder/exception/GlobalExceptionHandler.java

### Step 6: 보안 컴포넌트 생성
- [x] src/main/java/com/tableorder/security/JwtTokenProvider.java
- [x] src/main/java/com/tableorder/security/JwtAuthenticationFilter.java
- [x] src/main/java/com/tableorder/security/StoreIsolationValidator.java
- [x] src/main/java/com/tableorder/config/SecurityConfig.java
- [x] src/main/java/com/tableorder/config/WebConfig.java

### Step 7: 인프라 컴포넌트 생성
- [x] src/main/java/com/tableorder/config/S3Config.java
- [x] src/main/java/com/tableorder/infrastructure/SseEmitterManager.java
- [x] src/main/java/com/tableorder/infrastructure/OrderNumberGenerator.java
- [x] src/main/java/com/tableorder/filter/RequestIdFilter.java

### Step 8: DB 스키마 및 시드 데이터
- [x] src/main/resources/schema.sql
- [x] src/main/resources/data.sql (시드 데이터: 샘플 매장, 관리자, 테이블, 카테고리, 메뉴)

### Step 9: 단위 테스트 - Entity
- [x] src/test/java/com/tableorder/entity/OrderStatusTest.java
- [x] src/test/java/com/tableorder/entity/AdminTest.java

### Step 10: 단위 테스트 - Security
- [x] src/test/java/com/tableorder/security/JwtTokenProviderTest.java

### Step 11: 단위 테스트 - Infrastructure
- [x] src/test/java/com/tableorder/infrastructure/SseEmitterManagerTest.java
- [x] src/test/java/com/tableorder/infrastructure/OrderNumberGeneratorTest.java

### Step 12: 단위 테스트 - Exception
- [x] src/test/java/com/tableorder/exception/GlobalExceptionHandlerTest.java

### Step 13: 코드 생성 요약 문서
- [x] aidlc-docs/construction/common-foundation/code/code-summary.md

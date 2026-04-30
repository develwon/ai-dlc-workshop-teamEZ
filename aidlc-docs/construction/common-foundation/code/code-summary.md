# Code Summary - Unit 1: common-foundation

## 생성된 파일 목록

### 프로젝트 설정 (4개)
- `pom.xml` - Maven 빌드 설정 (Spring Boot 3.2.5, 모든 의존성)
- `src/main/java/com/tableorder/TableOrderApplication.java` - 메인 애플리케이션
- `src/main/resources/application.yml` - 애플리케이션 설정
- `src/main/resources/application-test.yml` - 테스트 프로파일 설정

### Enum (2개)
- `src/main/java/com/tableorder/enums/OrderStatus.java` - 주문 상태 (PENDING/PREPARING/COMPLETED + 전이 로직)
- `src/main/java/com/tableorder/enums/AdminRole.java` - 관리자 역할 (OWNER/MANAGER/STAFF + 권한 로직)

### Entity (10개)
- `BaseEntity.java` - 추상 기반 엔티티 (createdAt, updatedAt 자동 관리)
- `Store.java`, `Admin.java`, `StoreTable.java`, `TableSession.java`
- `Category.java`, `Menu.java`, `Order.java`, `OrderItem.java`, `OrderHistory.java`

### DTO (26개)
- 공통: ErrorResponse, ValidationErrorResponse
- 인증: TableLoginRequest/Response, AdminLoginRequest/Response
- 메뉴: CategoryResponse, MenuResponse, MenuDetailResponse, CreateMenuRequest, UpdateMenuRequest, MenuOrderRequest, CreateCategoryRequest, UpdateCategoryRequest
- 주문: CreateOrderRequest, OrderItemRequest, OrderResponse, OrderItemResponse, AdminOrderResponse, AdminOrderDetailResponse, StatusUpdateRequest, OrderEventData
- 테이블: CreateTableRequest, TableResponse, TableSummaryResponse, OrderHistoryResponse

### 예외 (7개)
- NotFoundException, UnauthorizedException, ForbiddenException, ConflictException
- AccountLockedException, InvalidStateTransitionException, GlobalExceptionHandler

### 보안 (5개)
- JwtTokenProvider, JwtAuthenticationFilter, StoreIsolationValidator
- SecurityConfig, WebConfig

### 인프라 (4개)
- S3Config, SseEmitterManager, OrderNumberGenerator, RequestIdFilter

### DB 스키마 (2개)
- `schema.sql` - 9개 테이블 + 9개 인덱스
- `data.sql` - 시드 데이터 (매장 1, 관리자 3, 테이블 3, 카테고리 3, 메뉴 7)

### 단위 테스트 (6개)
- OrderStatusTest (5 tests), AdminTest (4 tests)
- JwtTokenProviderTest (4 tests), StoreIsolationValidatorTest (서브에이전트 생성)
- SseEmitterManagerTest (3 tests), OrderNumberGeneratorTest (3 tests)
- GlobalExceptionHandlerTest (6 tests)

## 총 파일 수: 약 60개
## 총 테스트 수: 약 25개

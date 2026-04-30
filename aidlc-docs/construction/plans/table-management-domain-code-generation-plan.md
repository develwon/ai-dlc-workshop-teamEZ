# Code Generation Plan - Unit 5: table-management-domain

## 유닛 컨텍스트
- **유닛명**: table-management-domain
- **관련 스토리**: US-A03 (테이블 관리)
- **의존**: Unit 1 (common-foundation) - 이미 구현 완료
- **코드 위치**: `src/main/java/com/tableorder/` (기존 모놀리식 구조)
- **테스트 위치**: `src/test/java/com/tableorder/`

## 기존 코드 상태
- Entity: Order, OrderHistory, StoreTable, TableSession, OrderItem 이미 존재
- DTO: CreateTableRequest, TableResponse, TableSummaryResponse, OrderHistoryResponse 이미 존재
- 스키마: schema.sql 이미 존재
- **수정 필요**: Order 엔티티에 `deleted` 필드 추가, schema.sql에 컬럼/인덱스 추가

---

## 실행 계획

### Step 1: Order 엔티티 수정 (deleted 필드 추가)
- [x] `src/main/java/com/tableorder/entity/Order.java` 수정
- [x] `src/main/java/com/tableorder/entity/StoreTable.java` 수정 (updatePassword 메서드 추가)

### Step 2: DB 스키마 수정
- [x] `src/main/resources/schema.sql` 수정

### Step 3: DTO 수정/추가
- [x] `src/main/java/com/tableorder/dto/TableSummaryResponse.java` 수정
- [x] `src/main/java/com/tableorder/dto/OrderHistoryResponse.java` 수정
- [x] `src/main/java/com/tableorder/dto/OrderItemSnapshot.java` 생성

### Step 4: Repository 생성
- [x] `src/main/java/com/tableorder/repository/StoreTableRepository.java` 생성
- [x] `src/main/java/com/tableorder/repository/TableSessionRepository.java` 생성
- [x] `src/main/java/com/tableorder/repository/OrderRepository.java` 생성
- [x] `src/main/java/com/tableorder/repository/OrderItemRepository.java` 생성
- [x] `src/main/java/com/tableorder/repository/OrderHistoryRepository.java` 생성

### Step 5: Service 생성 - OrderHistoryService
- [x] `src/main/java/com/tableorder/service/OrderHistoryService.java` 생성

### Step 6: Service 생성 - TableSessionService
- [x] `src/main/java/com/tableorder/service/TableSessionService.java` 생성

### Step 7: Service 생성 - AdminTableService
- [x] `src/main/java/com/tableorder/service/AdminTableService.java` 생성

### Step 8: Controller 생성 - AdminTableController
- [x] `src/main/java/com/tableorder/controller/AdminTableController.java` 생성

### Step 9: Service 단위 테스트
- [x] `src/test/java/com/tableorder/service/OrderHistoryServiceTest.java` 생성
- [x] `src/test/java/com/tableorder/service/TableSessionServiceTest.java` 생성
- [x] `src/test/java/com/tableorder/service/AdminTableServiceTest.java` 생성

### Step 10: Controller 단위 테스트
- [x] `src/test/java/com/tableorder/controller/AdminTableControllerTest.java` 생성

### Step 11: Repository 단위 테스트
- [x] `src/test/java/com/tableorder/repository/OrderHistoryRepositoryTest.java` 생성

### Step 12: Code Summary 문서 생성
- [x] `aidlc-docs/construction/table-management-domain/code/code-summary.md` 생성

---

## 스토리 추적성
| 스토리 | 구현 Step | 상태 |
|--------|-----------|------|
| US-A03: 테이블 설정 | Step 7, 8 | [x] |
| US-A03: 이용 완료 | Step 1, 5, 6, 8 | [x] |
| US-A03: 과거 이력 조회 | Step 5, 8 | [x] |
| US-A03: 주문 삭제 (논리적) | Step 1 | [x] |

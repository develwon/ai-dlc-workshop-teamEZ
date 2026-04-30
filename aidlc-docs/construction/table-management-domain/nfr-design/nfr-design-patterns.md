# NFR Design Patterns - Unit 5: table-management-domain

> **상속**: Unit 1 (common-foundation)의 NFR Design Patterns를 전체 상속합니다.
> 이 문서는 Unit 5에 특화된 추가 패턴만 기술합니다.

---

## 1. 트랜잭션 패턴

### 1.1 이용 완료 복합 트랜잭션 패턴
```
AdminTableController.completeTable(tableId)
    |
    v
+------------------------------------------+
| @Transactional (READ_COMMITTED)          |
|                                          |
| 1. 테이블 존재 확인                        |
| 2. 매장 격리 검증                          |
| 3. 활성 세션 조회                          |
| 4. 주문 존재 확인 (deleted=false)          |
| 5. 주문 상태 검증 (모두 COMPLETED)          |
| 6. OrderHistory 배치 INSERT               |
| 7. Order 배치 UPDATE (deleted=true)       |
| 8. TableSession UPDATE (endTime=NOW)     |
|                                          |
| -- 트랜잭션 커밋 --                        |
+------------------------------------------+
    |
    v (트랜잭션 외부)
+------------------------------------------+
| SSE 이벤트 발행 (TABLE_COMPLETED)         |
| - 실패해도 데이터 무결성에 영향 없음        |
+------------------------------------------+
```

**설계 원칙**:
- 데이터 변경 작업은 모두 트랜잭션 내부에서 실행
- SSE 이벤트 발행은 트랜잭션 외부에서 실행 (격리)
- 트랜잭션 실패 시 전체 롤백 (부분 아카이빙 방지)
- `@TransactionalEventListener(phase = AFTER_COMMIT)` 패턴 활용 가능

### 1.2 배치 처리 패턴
```
아카이빙 시:
  - 세션의 모든 Order를 한 번에 조회 (1 SELECT)
  - 각 Order의 OrderItems를 배치 조회 (IN 절 활용)
  - OrderHistory를 배치 INSERT (saveAll)
  - Order를 배치 UPDATE (deleted=true, JPQL UPDATE)
```

**JPA 배치 설정**:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
```

---

## 2. 보안 패턴

### 2.1 RBAC 적용 패턴 (Unit 5 엔드포인트)
```
+------------------------------------------+
| AdminTableController                     |
+------------------------------------------+
| POST /api/admin/tables                   |
|   --> OWNER, MANAGER만 허용              |
|                                          |
| GET  /api/admin/tables                   |
|   --> 전체 관리자 역할 허용               |
|                                          |
| POST /api/admin/tables/{id}/complete     |
|   --> OWNER, MANAGER만 허용              |
|                                          |
| GET  /api/admin/tables/{id}/history      |
|   --> 전체 관리자 역할 허용               |
+------------------------------------------+
```

### 2.2 매장 격리 패턴 (Unit 5 적용)
```
모든 API 요청
    |
    v
[JWT에서 storeId 추출]
    |
    v
[테이블 조회 시 storeId 필터]
  - StoreTableRepository.findAllByStoreIdOrderByTableNumberAsc(storeId)
  - StoreTableRepository.findById(tableId) → storeId 일치 확인
    |
    v
[이력 조회 시 storeId 검증]
  - 테이블의 storeId와 JWT storeId 일치 확인
```

---

## 3. 데이터 무결성 패턴

### 3.1 논리적 삭제 패턴 (Soft Delete)
```
Order 엔티티:
  +-- deleted: Boolean (default: false)
  |
  +-- 조회 시: WHERE deleted = false
  +-- 삭제 시: UPDATE SET deleted = true
  +-- 아카이빙 시: UPDATE SET deleted = true (배치)
```

**구현 방식**:
- JPA `@Where(clause = "deleted = false")` 어노테이션 사용 고려
- 또는 Repository 메서드에 명시적 조건 추가
- 명시적 조건 방식 권장 (투명성, 디버깅 용이)

### 3.2 JSON 직렬화 패턴 (OrderItems 아카이빙)
```
Order + List<OrderItem>
    |
    v
[Jackson ObjectMapper]
    |
    v
OrderHistory.orderItems = JSON String
  [
    {"menuName":"김치찌개","quantity":2,"unitPrice":9000},
    {"menuName":"공기밥","quantity":2,"unitPrice":1000}
  ]
    |
    v (조회 시)
[Jackson ObjectMapper]
    |
    v
List<OrderItemSnapshot> (역직렬화)
```

**OrderItemSnapshot DTO**:
```java
public record OrderItemSnapshot(
    String menuName,
    int quantity,
    int unitPrice
) {}
```

---

## 4. 복원력 패턴

### 4.1 이용 완료 실패 복원 패턴
```
이용 완료 요청
    |
    v
[전제조건 검증] --실패--> 400 에러 (명확한 메시지)
    |                      - "활성 세션이 없습니다"
    |                      - "주문 내역이 없습니다"
    |                      - "미완료 주문이 있습니다"
    |성공
    v
[트랜잭션 실행] --실패--> 500 에러 (자동 롤백)
    |                      - 데이터 변경 없음
    |                      - 재시도 가능
    |성공
    v
[SSE 이벤트 발행] --실패--> 로그 기록 (데이터는 정상)
    |                        - 관리자 대시보드 수동 새로고침 필요
    |성공
    v
200 OK
```

### 4.2 Fail-Closed 패턴
- 검증 실패 시 항상 작업 차단 (fail-closed)
- 미완료 주문 존재 → 이용 완료 차단
- 주문 없음 → 이용 완료 차단
- 매장 불일치 → 접근 차단
- 역할 부족 → 접근 차단

---

## 5. 로깅 패턴 (Unit 5 특화)

### 5.1 이용 완료 감사 로깅
```
[이용 완료 시작] INFO  - Table completion started [requestId=abc] [storeId=1] [tableId=5] [sessionId=42]
[아카이빙]      INFO  - Archiving orders [sessionId=42] [orderCount=3] [totalAmount=45000]
[논리적 삭제]   DEBUG - Soft-deleting orders [sessionId=42] [orderIds=[101,102,103]]
[세션 종료]     INFO  - Session closed [sessionId=42] [endTime=2026-04-30T15:30:00]
[SSE 발행]     DEBUG - SSE event published [storeId=1] [event=TABLE_COMPLETED] [tableId=5]
[이용 완료 완료] INFO  - Table completion finished [requestId=abc] [duration=250ms]
```

### 5.2 주문 삭제 감사 로깅
```
[주문 삭제]     INFO  - Order soft-deleted [requestId=abc] [storeId=1] [orderId=101] [adminId=3]
```

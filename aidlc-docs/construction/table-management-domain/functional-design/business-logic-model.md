# Business Logic Model - Unit 5: table-management-domain

---

## 1. 테이블 설정 (Create/Update Table) 플로우

### 플로우 다이어그램
```
AdminTableController.createTable(CreateTableRequest)
  |
  v
[입력 검증]
  - tableNumber: NOT NULL, 양수
  - password: NOT NULL, 최소 4자
  |
  v
[매장 격리 검증]
  - 요청자의 storeId 확인 (JWT에서 추출)
  - OWNER, MANAGER 역할만 허용
  |
  v
[중복 테이블 확인]
  - StoreTableRepository.findByStoreIdAndTableNumber(storeId, tableNumber)
  |
  +-- 존재함 --> [기존 테이블 업데이트]
  |               - passwordHash = bcrypt(newPassword)
  |               - updatedAt = NOW
  |               - 200 OK + TableResponse 반환
  |
  +-- 미존재 --> [새 테이블 생성]
                  - StoreTable 엔티티 생성
                  - passwordHash = bcrypt(password)
                  - StoreTableRepository.save()
                  - 201 Created + TableResponse 반환
```

---

## 2. 테이블 목록 조회 (Get Tables) 플로우

### 플로우 다이어그램
```
AdminTableController.getTables()
  |
  v
[매장 격리 검증]
  - 요청자의 storeId 확인 (JWT에서 추출)
  |
  v
[테이블 목록 조회]
  - StoreTableRepository.findAllByStoreId(storeId)
  |
  v
[활성 세션 여부 조회]
  - 각 테이블에 대해 TableSessionRepository.findByTableIdAndEndTimeIsNull(tableId)
  - 활성 세션 존재 여부를 hasActiveSession 필드로 매핑
  |
  v
[응답 생성]
  - List<TableSummaryResponse> 반환
  - 각 항목: tableId, tableNumber, hasActiveSession
```

---

## 3. 테이블 이용 완료 (Complete Table) 플로우

### 플로우 다이어그램
```
AdminTableController.completeTable(tableId)
  |
  v
[매장 격리 검증]
  - 요청자의 storeId 확인 (JWT에서 추출)
  - OWNER, MANAGER 역할만 허용
  |
  v
[테이블 존재 확인]
  - StoreTableRepository.findById(tableId)
  - 미존재 시 404 NOT_FOUND
  - 매장 불일치 시 403 FORBIDDEN
  |
  v
[활성 세션 확인]
  - TableSessionRepository.findByTableIdAndEndTimeIsNull(tableId)
  - 활성 세션 없음 --> 400 에러 "활성 세션이 없습니다"
  |
  v
[주문 상태 검증]
  - OrderRepository.findBySessionId(sessionId)
  - 주문 0건 --> 400 에러 "주문 내역이 없습니다"
  - PENDING 또는 PREPARING 주문 존재 --> 400 에러 "미완료 주문이 있습니다"
  |
  v
[주문 이력 아카이빙]
  - OrderHistoryService.archiveSessionOrders(tableId, sessionId)
  - 각 Order + OrderItems를 OrderHistory로 변환
  - OrderHistory 저장
  |
  v
[주문 논리적 삭제]
  - 해당 세션의 모든 Order에 deleted = true 설정
  |
  v
[세션 종료]
  - session.endTime = NOW
  - TableSessionRepository.save(session)
  |
  v
[SSE 이벤트 발행]
  - OrderEventService.publishOrderEvent(storeId, TABLE_COMPLETED)
  |
  v
[응답]
  - 200 OK (void)
```

### 이용 완료 전제조건 검증 순서
```
1. 테이블 존재 확인
2. 매장 소속 확인
3. 활성 세션 존재 확인
4. 주문 존재 확인 (최소 1건)
5. 모든 주문 COMPLETED 상태 확인
```

---

## 4. 주문 삭제 (Delete Order - 직권 수정) 플로우

### 플로우 다이어그램
```
AdminOrderController.deleteOrder(orderId)
  |
  v
[매장 격리 검증]
  - 요청자의 storeId 확인 (JWT에서 추출)
  - OWNER, MANAGER 역할만 허용
  |
  v
[주문 존재 확인]
  - OrderRepository.findById(orderId)
  - 미존재 또는 이미 삭제됨 --> 404 NOT_FOUND
  - 매장 불일치 --> 403 FORBIDDEN
  |
  v
[논리적 삭제 처리]
  - order.deleted = true
  - order.updatedAt = NOW
  - OrderRepository.save(order)
  |
  v
[SSE 이벤트 발행]
  - OrderEventService.publishOrderEvent(storeId, ORDER_DELETED)
  |
  v
[응답]
  - 204 No Content
```

> **Note**: 주문 삭제는 Unit 4 (order-domain)의 AdminOrderController에 이미 정의되어 있으나, Unit 5에서는 논리적 삭제 방식으로 확장합니다. Order 엔티티에 `deleted` 필드가 추가되어야 합니다.

---

## 5. 과거 주문 이력 조회 (Get Table History) 플로우

### 플로우 다이어그램
```
AdminTableController.getTableHistory(tableId, dateType, date)
  |
  v
[매장 격리 검증]
  - 요청자의 storeId 확인 (JWT에서 추출)
  |
  v
[테이블 존재 확인]
  - StoreTableRepository.findById(tableId)
  - 미존재 시 404 NOT_FOUND
  - 매장 불일치 시 403 FORBIDDEN
  |
  v
[이력 조회]
  +-- date 파라미터 없음 --> 전체 이력 조회
  |     OrderHistoryRepository.findByTableIdOrderByCompletedAtDesc(tableId)
  |
  +-- date + dateType=orderedAt --> 주문 시각 기준 필터
  |     OrderHistoryRepository.findByTableIdAndOrderedAtBetween(tableId, startOfDay, endOfDay)
  |
  +-- date + dateType=completedAt --> 이용 완료 시각 기준 필터
        OrderHistoryRepository.findByTableIdAndCompletedAtBetween(tableId, startOfDay, endOfDay)
  |
  v
[응답 생성]
  - List<OrderHistoryResponse> 반환
  - 각 항목: historyId, orderNumber, totalAmount, orderItems(JSON 파싱), orderedAt, completedAt
```

---

## 6. OrderHistory 아카이빙 로직

### 변환 규칙
```
Order + List<OrderItem> --> OrderHistory

매핑:
  OrderHistory.storeId     = Order.storeId
  OrderHistory.tableId     = Order.tableId
  OrderHistory.sessionId   = Order.sessionId
  OrderHistory.orderNumber = Order.orderNumber
  OrderHistory.totalAmount = Order.totalAmount
  OrderHistory.orderItems  = JSON.stringify(OrderItems)
  OrderHistory.orderedAt   = Order.createdAt
  OrderHistory.completedAt = NOW (이용 완료 시각)
```

### OrderItems JSON 형식
```json
[
  {
    "menuName": "김치찌개",
    "quantity": 2,
    "unitPrice": 9000
  },
  {
    "menuName": "공기밥",
    "quantity": 2,
    "unitPrice": 1000
  }
]
```

### 아카이빙 트랜잭션
```
@Transactional
archiveSessionOrders(tableId, sessionId):
  1. 해당 세션의 모든 Order 조회 (deleted = false)
  2. 각 Order에 대해:
     a. OrderItems 조회
     b. OrderHistory 엔티티 생성 (변환 규칙 적용)
     c. OrderHistory 저장
  3. 모든 Order에 deleted = true 설정
```

# Domain Entities - Unit 5: table-management-domain

---

## 1. Unit 5 관련 엔티티 개요

Unit 5는 새로운 엔티티를 생성하지 않으며, Unit 1에서 정의된 기존 엔티티를 활용합니다.
단, Order 엔티티에 논리적 삭제를 위한 `deleted` 필드 추가가 필요합니다.

### 사용 엔티티
| 엔티티 | 역할 | 정의 유닛 |
|--------|------|-----------|
| StoreTable | 테이블 설정 대상 | Unit 1 |
| TableSession | 세션 관리 (시작/종료) | Unit 1 |
| Order | 주문 조회/논리적 삭제 | Unit 1 (수정 필요) |
| OrderItem | 주문 항목 조회 (아카이빙용) | Unit 1 |
| OrderHistory | 과거 주문 이력 저장 | Unit 1 |

---

## 2. Order 엔티티 수정사항

### 추가 필드
| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| deleted | Boolean | NOT NULL, DEFAULT false | 논리적 삭제 플래그 |

### 영향 범위
- Unit 4 (order-domain)의 모든 주문 조회 쿼리에 `deleted = false` 조건 추가 필요
- Unit 5 (table-management-domain)에서 이용 완료 시 `deleted = true` 설정
- schema.sql에 `deleted BOOLEAN NOT NULL DEFAULT false` 컬럼 추가

---

## 3. 엔티티 상호작용 다이어그램

### 테이블 설정 플로우
```
AdminTableController
  |
  v
StoreTable (생성 또는 업데이트)
  - 새 테이블: INSERT
  - 기존 테이블: UPDATE passwordHash
```

### 이용 완료 플로우
```
AdminTableController
  |
  v
StoreTable --> TableSession (활성 세션 조회)
                  |
                  v
              Order (세션별 주문 조회, deleted=false)
                  |
                  v
              OrderItem (주문별 항목 조회)
                  |
                  v
              OrderHistory (아카이빙 저장)
                  |
                  v
              Order.deleted = true (논리적 삭제)
                  |
                  v
              TableSession.endTime = NOW (세션 종료)
```

### 이력 조회 플로우
```
AdminTableController
  |
  v
StoreTable --> OrderHistory (테이블별 이력 조회)
                  |
                  +-- 전체 조회: findByTableId
                  +-- 날짜 필터 (orderedAt): findByTableIdAndOrderedAtBetween
                  +-- 날짜 필터 (completedAt): findByTableIdAndCompletedAtBetween
```

---

## 4. Repository 메서드 상세

### StoreTableRepository (기존 활용)
| 메서드 | 설명 |
|--------|------|
| findByStoreIdAndTableNumber(storeId, tableNumber) | 매장+테이블번호로 조회 |
| findAllByStoreIdOrderByTableNumberAsc(storeId) | 매장별 테이블 목록 (번호순) |
| findById(tableId) | 테이블 ID로 조회 |

### TableSessionRepository (기존 활용)
| 메서드 | 설명 |
|--------|------|
| findByTableIdAndEndTimeIsNull(tableId) | 활성 세션 조회 |

### OrderRepository (기존 활용 + 수정)
| 메서드 | 설명 |
|--------|------|
| findBySessionIdAndDeletedFalse(sessionId) | 세션별 활성 주문 조회 |
| findBySessionIdAndDeletedFalseAndStatusNot(sessionId, COMPLETED) | 미완료 주문 존재 확인 |

### OrderItemRepository (기존 활용)
| 메서드 | 설명 |
|--------|------|
| findByOrderId(orderId) | 주문별 항목 조회 |

### OrderHistoryRepository (기존 활용 + 확장)
| 메서드 | 설명 |
|--------|------|
| findByTableIdOrderByCompletedAtDesc(tableId) | 테이블별 전체 이력 조회 |
| findByTableIdAndOrderedAtBetween(tableId, start, end) | 주문 시각 기준 필터 |
| findByTableIdAndCompletedAtBetween(tableId, start, end) | 이용 완료 시각 기준 필터 |

---

## 5. DTO 매핑

### 요청 DTO
| DTO | 필드 | 검증 |
|-----|------|------|
| CreateTableRequest | tableNumber (Integer), password (String) | tableNumber > 0, password.length >= 4 |

### 응답 DTO
| DTO | 필드 |
|-----|------|
| TableResponse | tableId, tableNumber, storeId, createdAt |
| TableSummaryResponse | tableId, tableNumber, hasActiveSession |
| OrderHistoryResponse | historyId, orderNumber, totalAmount, orderItems (List), orderedAt, completedAt |

### OrderHistoryResponse.orderItems 내부 구조
| 필드 | 타입 | 설명 |
|------|------|------|
| menuName | String | 메뉴명 |
| quantity | Integer | 수량 |
| unitPrice | Integer | 단가 |

# Logical Components - Unit 5: table-management-domain

> **상속**: Unit 1 (common-foundation)의 Logical Components를 전체 상속합니다.
> 이 문서는 Unit 5에서 새로 구현하거나 확장하는 컴포넌트만 기술합니다.

---

## 1. Controller 컴포넌트

### AdminTableController
| 항목 | 내용 |
|------|------|
| **책임** | 테이블 관리 API 엔드포인트 |
| **경로 접두사** | `/api/admin/tables` |
| **인증** | JWT 필수 (관리자) |
| **의존** | TableSessionService, OrderHistoryService, StoreTableRepository, StoreIsolationValidator |

**엔드포인트**:
| 메서드 | 경로 | 역할 제한 | 설명 |
|--------|------|-----------|------|
| POST | /api/admin/tables | OWNER, MANAGER | 테이블 설정 (생성/업데이트) |
| GET | /api/admin/tables | ALL ADMIN | 테이블 목록 조회 |
| POST | /api/admin/tables/{tableId}/complete | OWNER, MANAGER | 이용 완료 |
| GET | /api/admin/tables/{tableId}/history | ALL ADMIN | 과거 이력 조회 |

**입력 검증**:
- CreateTableRequest: tableNumber > 0, password.length >= 4
- tableId: Path Variable, Long 타입
- date: Query Parameter, LocalDate 형식 (optional)
- dateType: Query Parameter, String ("orderedAt" 또는 "completedAt", optional)

---

## 2. Service 컴포넌트

### TableSessionService (확장)
| 항목 | 내용 |
|------|------|
| **책임** | 테이블 세션 라이프사이클 관리 (이용 완료 확장) |
| **정의 유닛** | Unit 4 (order-domain) |
| **확장 유닛** | Unit 5 (table-management-domain) |
| **의존** | TableSessionRepository, OrderRepository, OrderHistoryService, OrderEventService |

**Unit 5 확장 메서드**:
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| completeSession | tableId, storeId | void | 이용 완료 (검증 + 아카이빙 + 세션 종료) |

**completeSession 내부 로직**:
1. 활성 세션 조회 → 없으면 예외
2. 세션 주문 조회 (deleted=false) → 0건이면 예외
3. 미완료 주문 확인 (status != COMPLETED) → 있으면 예외
4. OrderHistoryService.archiveSessionOrders() 호출
5. session.endTime = NOW 설정
6. SSE TABLE_COMPLETED 이벤트 발행 (트랜잭션 외부)

### OrderHistoryService
| 항목 | 내용 |
|------|------|
| **책임** | 과거 주문 이력 아카이빙 및 조회 |
| **의존** | OrderHistoryRepository, OrderRepository, OrderItemRepository, ObjectMapper |

**메서드**:
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| archiveSessionOrders | tableId, sessionId | void | 세션 주문을 이력으로 아카이빙 |
| getTableHistory | tableId, date, dateType | List\<OrderHistory\> | 과거 이력 조회 |

**archiveSessionOrders 내부 로직**:
1. 세션의 모든 Order 조회 (deleted=false)
2. 각 Order의 OrderItems 조회
3. OrderItems → JSON 직렬화 (ObjectMapper)
4. OrderHistory 엔티티 생성 (배치)
5. OrderHistory 배치 저장 (saveAll)
6. Order 배치 논리적 삭제 (deleted=true)

**getTableHistory 내부 로직**:
1. date 없음 → 전체 이력 조회 (completedAt DESC)
2. dateType=orderedAt → orderedAt 범위 필터
3. dateType=completedAt (또는 기본값) → completedAt 범위 필터
4. OrderHistory.orderItems JSON → List\<OrderItemSnapshot\> 역직렬화

---

## 3. Repository 컴포넌트

### OrderHistoryRepository (확장)
| 항목 | 내용 |
|------|------|
| **책임** | OrderHistory 데이터 접근 |
| **상속** | JpaRepository\<OrderHistory, Long\> |

**메서드**:
| 메서드 | 설명 |
|--------|------|
| findByTableIdOrderByCompletedAtDesc(tableId) | 테이블별 전체 이력 (최신순) |
| findByTableIdAndOrderedAtBetween(tableId, start, end) | 주문 시각 기준 필터 |
| findByTableIdAndCompletedAtBetween(tableId, start, end) | 이용 완료 시각 기준 필터 |

### OrderRepository (확장 쿼리)
| 메서드 | 설명 |
|--------|------|
| findBySessionIdAndDeletedFalse(sessionId) | 세션별 활성 주문 조회 |
| existsBySessionIdAndDeletedFalseAndStatusNot(sessionId, status) | 미완료 주문 존재 확인 |
| countBySessionIdAndDeletedFalse(sessionId) | 세션별 활성 주문 수 |

### StoreTableRepository (확장 쿼리)
| 메서드 | 설명 |
|--------|------|
| findAllByStoreIdOrderByTableNumberAsc(storeId) | 매장별 테이블 목록 (번호순) |

### TableSessionRepository (기존 활용)
| 메서드 | 설명 |
|--------|------|
| findByTableIdAndEndTimeIsNull(tableId) | 활성 세션 조회 |

---

## 4. DTO 컴포넌트 (Unit 5 특화)

### 요청 DTO
| DTO | 필드 | 검증 |
|-----|------|------|
| CreateTableRequest | tableNumber (Integer), password (String) | @Min(1), @NotNull, @Size(min=4) |

### 응답 DTO
| DTO | 필드 |
|-----|------|
| TableResponse | tableId, tableNumber, storeId, createdAt |
| TableSummaryResponse | tableId, tableNumber, hasActiveSession |
| OrderHistoryResponse | historyId, orderNumber, totalAmount, orderItems (List\<OrderItemSnapshot\>), orderedAt, completedAt |

### 내부 DTO
| DTO | 필드 | 용도 |
|-----|------|------|
| OrderItemSnapshot | menuName, quantity, unitPrice | OrderHistory JSON 직렬화/역직렬화 |

---

## 5. 컴포넌트 의존성 다이어그램

```
+---------------------------+
| AdminTableController      |
+---------------------------+
    |           |
    v           v
+-----------+ +------------------+
| TableSess | | OrderHistory     |
| ionService| | Service          |
| (확장)     | |                  |
+-----------+ +------------------+
    |    |        |    |    |
    v    v        v    v    v
+------+ +------+ +------+ +------+ +------+
|Table | |Order | |Order | |Order | |Object|
|Sess  | |Repo  | |Item  | |Hist  | |Mapper|
|Repo  | |      | |Repo  | |Repo  | |      |
+------+ +------+ +------+ +------+ +------+
```

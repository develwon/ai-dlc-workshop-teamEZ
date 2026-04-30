# Domain Entities - Unit 1: common-foundation

---

## Entity Relationship Diagram (Text)

```
Store (1) ----< (N) Admin
Store (1) ----< (N) StoreTable
Store (1) ----< (N) Category
StoreTable (1) ----< (N) TableSession
Category (1) ----< (N) Menu
TableSession (1) ----< (N) Order
Order (1) ----< (N) OrderItem
OrderItem (N) >---- (1) Menu
TableSession (1) ----< (N) OrderHistory
```

---

## 1. Store (매장)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | Long | PK, Auto Increment | 매장 고유 ID |
| storeCode | String(50) | UNIQUE, NOT NULL | 매장 식별 코드 |
| storeName | String(100) | NOT NULL | 매장명 |
| createdAt | LocalDateTime | NOT NULL, DEFAULT NOW | 생성 시각 |
| updatedAt | LocalDateTime | NOT NULL, DEFAULT NOW | 수정 시각 |

---

## 2. Admin (관리자)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | Long | PK, Auto Increment | 관리자 고유 ID |
| storeId | Long | FK → Store.id, NOT NULL | 소속 매장 |
| username | String(50) | NOT NULL | 사용자명 |
| passwordHash | String(255) | NOT NULL | bcrypt 해시 비밀번호 |
| role | AdminRole (Enum) | NOT NULL | 역할 (OWNER, MANAGER, STAFF) |
| loginAttempts | Integer | NOT NULL, DEFAULT 0 | 로그인 시도 횟수 |
| lockedUntil | LocalDateTime | NULLABLE | 계정 잠금 해제 시각 |
| createdAt | LocalDateTime | NOT NULL, DEFAULT NOW | 생성 시각 |
| updatedAt | LocalDateTime | NOT NULL, DEFAULT NOW | 수정 시각 |

**UNIQUE 제약**: (storeId, username)

---

## 3. StoreTable (테이블)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | Long | PK, Auto Increment | 테이블 고유 ID |
| storeId | Long | FK → Store.id, NOT NULL | 소속 매장 |
| tableNumber | Integer | NOT NULL | 테이블 번호 |
| passwordHash | String(255) | NOT NULL | bcrypt 해시 비밀번호 |
| createdAt | LocalDateTime | NOT NULL, DEFAULT NOW | 생성 시각 |
| updatedAt | LocalDateTime | NOT NULL, DEFAULT NOW | 수정 시각 |

**UNIQUE 제약**: (storeId, tableNumber)

---

## 4. TableSession (테이블 세션)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | Long | PK, Auto Increment | 세션 고유 ID |
| tableId | Long | FK → StoreTable.id, NOT NULL | 테이블 |
| startTime | LocalDateTime | NOT NULL, DEFAULT NOW | 세션 시작 시각 |
| endTime | LocalDateTime | NULLABLE | 세션 종료 시각 (NULL = 활성) |
| createdAt | LocalDateTime | NOT NULL, DEFAULT NOW | 생성 시각 |

**비즈니스 규칙**: 테이블당 활성 세션(endTime IS NULL)은 최대 1개

---

## 5. Category (메뉴 카테고리)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | Long | PK, Auto Increment | 카테고리 고유 ID |
| storeId | Long | FK → Store.id, NOT NULL | 소속 매장 |
| name | String(50) | NOT NULL | 카테고리명 |
| displayOrder | Integer | NOT NULL, DEFAULT 0 | 노출 순서 |
| createdAt | LocalDateTime | NOT NULL, DEFAULT NOW | 생성 시각 |
| updatedAt | LocalDateTime | NOT NULL, DEFAULT NOW | 수정 시각 |

**UNIQUE 제약**: (storeId, name)

---

## 6. Menu (메뉴)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | Long | PK, Auto Increment | 메뉴 고유 ID |
| storeId | Long | FK → Store.id, NOT NULL | 소속 매장 |
| categoryId | Long | FK → Category.id, NOT NULL | 카테고리 |
| name | String(100) | NOT NULL | 메뉴명 |
| price | Integer | NOT NULL, MIN 0 | 가격 (원) |
| description | String(500) | NULLABLE | 메뉴 설명 |
| imageUrl | String(500) | NULLABLE | 이미지 URL (S3) |
| displayOrder | Integer | NOT NULL, DEFAULT 0 | 노출 순서 |
| createdAt | LocalDateTime | NOT NULL, DEFAULT NOW | 생성 시각 |
| updatedAt | LocalDateTime | NOT NULL, DEFAULT NOW | 수정 시각 |

---

## 7. Order (주문)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | Long | PK, Auto Increment | 주문 고유 ID |
| storeId | Long | FK → Store.id, NOT NULL | 소속 매장 |
| tableId | Long | FK → StoreTable.id, NOT NULL | 테이블 |
| sessionId | Long | FK → TableSession.id, NOT NULL | 테이블 세션 |
| orderNumber | String(20) | UNIQUE, NOT NULL | 주문 번호 (표시용) |
| totalAmount | Integer | NOT NULL, MIN 0 | 총 주문 금액 |
| status | OrderStatus (Enum) | NOT NULL, DEFAULT PENDING | 주문 상태 |
| createdAt | LocalDateTime | NOT NULL, DEFAULT NOW | 주문 시각 |
| updatedAt | LocalDateTime | NOT NULL, DEFAULT NOW | 수정 시각 |

---

## 8. OrderItem (주문 항목)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | Long | PK, Auto Increment | 주문 항목 고유 ID |
| orderId | Long | FK → Order.id, NOT NULL | 주문 |
| menuId | Long | FK → Menu.id, NOT NULL | 메뉴 |
| menuName | String(100) | NOT NULL | 주문 시점 메뉴명 (스냅샷) |
| quantity | Integer | NOT NULL, MIN 1 | 수량 |
| unitPrice | Integer | NOT NULL, MIN 0 | 주문 시점 단가 (스냅샷) |
| createdAt | LocalDateTime | NOT NULL, DEFAULT NOW | 생성 시각 |

---

## 9. OrderHistory (과거 주문 이력)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | Long | PK, Auto Increment | 이력 고유 ID |
| storeId | Long | NOT NULL | 매장 ID |
| tableId | Long | NOT NULL | 테이블 ID |
| sessionId | Long | NOT NULL | 세션 ID |
| orderNumber | String(20) | NOT NULL | 주문 번호 |
| totalAmount | Integer | NOT NULL | 총 금액 |
| orderItems | String (JSON) | NOT NULL | 주문 항목 JSON (스냅샷) |
| orderedAt | LocalDateTime | NOT NULL | 원래 주문 시각 |
| completedAt | LocalDateTime | NOT NULL | 이용 완료 시각 |
| createdAt | LocalDateTime | NOT NULL, DEFAULT NOW | 이력 생성 시각 |

---

## Enum 정의

### OrderStatus
| 값 | 설명 |
|----|------|
| PENDING | 대기중 |
| PREPARING | 준비중 |
| COMPLETED | 완료 |

### AdminRole
| 값 | 설명 | 권한 범위 |
|----|------|-----------|
| OWNER | 매장주 | 전체 기능 |
| MANAGER | 매니저 | 주문 관리 + 테이블 관리 + 메뉴 관리 |
| STAFF | 직원 | 주문 모니터링 + 주문 상태 변경 |

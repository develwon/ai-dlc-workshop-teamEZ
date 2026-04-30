# Business Rules - Unit 5: table-management-domain

---

## 1. 테이블 설정 규칙 (Table Setup Rules)

| 규칙 ID | 규칙 | 설명 | 에러 코드 |
|---------|------|------|-----------|
| TBL-01 | 테이블 번호는 양수 정수 | tableNumber > 0 | VALIDATION_ERROR (400) |
| TBL-02 | 테이블 비밀번호 최소 4자 | password.length >= 4 | VALIDATION_ERROR (400) |
| TBL-03 | 비밀번호는 bcrypt로 해싱 저장 | 평문 저장 금지 | - |
| TBL-04 | 동일 매장 내 테이블 번호 중복 시 업데이트 | 기존 테이블의 비밀번호를 새 값으로 변경 | - |
| TBL-05 | OWNER, MANAGER만 테이블 설정 가능 | STAFF 역할 차단 | FORBIDDEN (403) |
| TBL-06 | 매장 격리: 자기 매장 테이블만 설정 가능 | JWT의 storeId와 일치 확인 | FORBIDDEN (403) |

---

## 2. 테이블 목록 조회 규칙 (Table List Rules)

| 규칙 ID | 규칙 | 설명 | 에러 코드 |
|---------|------|------|-----------|
| TBL-10 | 자기 매장 테이블만 조회 가능 | storeId 기반 필터링 | - |
| TBL-11 | 활성 세션 여부 포함 반환 | hasActiveSession 필드 | - |
| TBL-12 | 테이블 번호 오름차순 정렬 | ORDER BY tableNumber ASC | - |

---

## 3. 이용 완료 규칙 (Table Completion Rules)

| 규칙 ID | 규칙 | 설명 | 에러 코드 |
|---------|------|------|-----------|
| CMP-01 | 활성 세션이 있어야 이용 완료 가능 | endTime IS NULL인 세션 필수 | VALIDATION_ERROR (400) |
| CMP-02 | 해당 세션에 주문이 최소 1건 이상 존재해야 함 | 주문 0건이면 이용 완료 차단 | VALIDATION_ERROR (400) |
| CMP-03 | 모든 주문이 COMPLETED 상태여야 이용 완료 가능 | PENDING/PREPARING 주문 존재 시 차단 | VALIDATION_ERROR (400) |
| CMP-04 | OWNER, MANAGER만 이용 완료 가능 | STAFF 역할 차단 | FORBIDDEN (403) |
| CMP-05 | 매장 격리: 자기 매장 테이블만 이용 완료 가능 | JWT의 storeId와 일치 확인 | FORBIDDEN (403) |
| CMP-06 | 이용 완료 시 주문을 OrderHistory로 아카이빙 | 모든 주문 + 항목을 이력으로 변환 | - |
| CMP-07 | 이용 완료 시 세션 종료 | session.endTime = NOW | - |
| CMP-08 | 이용 완료 시 주문 논리적 삭제 | order.deleted = true | - |
| CMP-09 | 이용 완료 시 SSE TABLE_COMPLETED 이벤트 발행 | 관리자 대시보드 실시간 반영 | - |
| CMP-10 | 아카이빙과 세션 종료는 단일 트랜잭션 | 부분 실패 방지 (@Transactional) | - |

### 이용 완료 검증 순서
```
1. 테이블 존재 확인 (404 if not found)
2. 매장 소속 확인 (403 if mismatch)
3. 역할 확인 (403 if STAFF)
4. 활성 세션 확인 (400 if no active session)
5. 주문 존재 확인 (400 if no orders)
6. 주문 상태 확인 (400 if incomplete orders)
7. 아카이빙 + 논리적 삭제 + 세션 종료 (트랜잭션)
8. SSE 이벤트 발행
```

---

## 4. 주문 삭제 규칙 (Order Deletion Rules)

| 규칙 ID | 규칙 | 설명 | 에러 코드 |
|---------|------|------|-----------|
| DEL-01 | 논리적 삭제 방식 적용 | deleted = true 설정, DB에서 제거하지 않음 | - |
| DEL-02 | 이미 삭제된 주문은 재삭제 불가 | deleted = true인 주문 접근 시 404 | NOT_FOUND (404) |
| DEL-03 | OWNER, MANAGER만 주문 삭제 가능 | STAFF 역할 차단 | FORBIDDEN (403) |
| DEL-04 | 매장 격리: 자기 매장 주문만 삭제 가능 | JWT의 storeId와 일치 확인 | FORBIDDEN (403) |
| DEL-05 | 삭제 시 SSE ORDER_DELETED 이벤트 발행 | 관리자 대시보드 실시간 반영 | - |
| DEL-06 | 삭제된 주문은 조회 목록에서 제외 | 모든 주문 조회 쿼리에 deleted = false 조건 | - |

> **Note**: Order 엔티티에 `deleted` (Boolean, default false) 필드 추가 필요. 이는 Unit 1 (common-foundation)의 Order 엔티티 수정이 필요합니다.

---

## 5. 과거 주문 이력 조회 규칙 (Order History Rules)

| 규칙 ID | 규칙 | 설명 | 에러 코드 |
|---------|------|------|-----------|
| HST-01 | 매장 격리: 자기 매장 테이블 이력만 조회 가능 | JWT의 storeId와 일치 확인 | FORBIDDEN (403) |
| HST-02 | 날짜 필터 미지정 시 전체 이력 반환 | 최신순 정렬 (completedAt DESC) | - |
| HST-03 | dateType=orderedAt 시 주문 시각 기준 필터 | orderedAt BETWEEN startOfDay AND endOfDay | - |
| HST-04 | dateType=completedAt 시 이용 완료 시각 기준 필터 | completedAt BETWEEN startOfDay AND endOfDay | - |
| HST-05 | dateType 미지정 + date 지정 시 기본값은 completedAt | 기본 필터 기준 | - |
| HST-06 | orderItems는 JSON에서 파싱하여 구조화된 형태로 반환 | List 형태로 변환 | - |
| HST-07 | 이력은 수정/삭제 불가 (읽기 전용) | 아카이빙된 데이터 보존 | - |

---

## 6. 아카이빙 규칙 (Archiving Rules)

| 규칙 ID | 규칙 | 설명 |
|---------|------|------|
| ARC-01 | Order → OrderHistory 변환 시 주문 항목은 JSON으로 직렬화 | menuName, quantity, unitPrice 포함 |
| ARC-02 | orderedAt은 원래 Order.createdAt 값 보존 | 주문 시점 기록 유지 |
| ARC-03 | completedAt은 이용 완료 처리 시점 | NOW() 사용 |
| ARC-04 | 아카이빙 후 원본 Order는 논리적 삭제 | deleted = true |
| ARC-05 | 아카이빙은 세션 단위로 일괄 처리 | 세션의 모든 주문을 한 번에 아카이빙 |
| ARC-06 | 아카이빙 트랜잭션 실패 시 전체 롤백 | 부분 아카이빙 방지 |

---

## 7. 공통 규칙 (Cross-Cutting Rules)

| 규칙 ID | 규칙 | 설명 |
|---------|------|------|
| COM-01 | 모든 API는 JWT 인증 필수 | JwtAuthenticationFilter 적용 |
| COM-02 | 매장 격리 (Store Isolation) | 모든 데이터 접근에 storeId 검증 |
| COM-03 | 역할 기반 접근 제어 | OWNER > MANAGER > STAFF 권한 계층 |
| COM-04 | 에러 응답은 통일된 ErrorResponse 형식 | GlobalExceptionHandler 활용 |
| COM-05 | 모든 변경 작업은 감사 로깅 | 구조화된 로그 출력 |

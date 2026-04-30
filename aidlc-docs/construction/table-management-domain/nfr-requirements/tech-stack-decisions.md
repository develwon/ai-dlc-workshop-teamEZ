# Tech Stack Decisions - Unit 5: table-management-domain

> **상속**: Unit 1 (common-foundation)의 기술 스택을 전체 상속합니다.
> 이 문서는 Unit 5에 특화된 추가 기술 결정사항만 기술합니다.

---

## 1. 핵심 기술 스택 (Unit 1 상속)

| 영역 | 기술 | 버전 |
|------|------|------|
| 언어 | Java | 17 (LTS) |
| 프레임워크 | Spring Boot | 3.2.x |
| DB | PostgreSQL | 15.x |
| ORM | Spring Data JPA + Hibernate | Spring Boot 내장 |
| 보안 | Spring Security | Spring Boot 내장 |
| 빌드 | Maven | 3.9.x |

---

## 2. Unit 5 추가 라이브러리

| 라이브러리 | 용도 | 비고 |
|-----------|------|------|
| **Jackson (spring-boot-starter-web 내장)** | OrderItems JSON 직렬화/역직렬화 | ObjectMapper 활용 |

> Unit 5는 추가 외부 라이브러리가 필요하지 않습니다. Spring Boot 내장 라이브러리로 모든 기능 구현 가능합니다.

---

## 3. DB 인덱스 전략 (Unit 5 추가)

| 테이블 | 인덱스 | 용도 |
|--------|--------|------|
| orders | (session_id, deleted) | 세션별 활성 주문 조회 (논리적 삭제 필터) |
| orders | (store_id, deleted, status) | 매장별 활성 주문 조회 (삭제 제외) |
| order_history | (table_id, ordered_at) | 주문 시각 기준 이력 조회 |
| order_history | (table_id, completed_at) | 이용 완료 시각 기준 이력 조회 |

### 기존 인덱스 수정
| 테이블 | 기존 인덱스 | 수정 내용 |
|--------|------------|-----------|
| orders | (session_id, created_at) | (session_id, deleted, created_at)로 확장 |

---

## 4. 스키마 변경사항

### Order 테이블 컬럼 추가
```sql
ALTER TABLE orders ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false;
```

### 인덱스 추가
```sql
CREATE INDEX idx_orders_session_deleted ON orders(session_id, deleted);
CREATE INDEX idx_orders_store_deleted_status ON orders(store_id, deleted, status);
CREATE INDEX idx_order_history_table_ordered ON order_history(table_id, ordered_at);
CREATE INDEX idx_order_history_table_completed ON order_history(table_id, completed_at);
```

---

## 5. 테스트 전략 (Unit 5)

| 테스트 유형 | 도구 | 대상 |
|------------|------|------|
| 단위 테스트 | JUnit 5 + Mockito | AdminTableController, OrderHistoryService, TableSessionService(확장) |
| Repository 테스트 | @DataJpaTest + H2 | OrderHistoryRepository (날짜 필터 쿼리), OrderRepository (deleted 필터) |
| Controller 테스트 | @WebMvcTest + MockMvc | AdminTableController (RBAC, 입력 검증) |
| 통합 테스트 | @SpringBootTest | 이용 완료 전체 플로우 (아카이빙 + 삭제 + 세션 종료) |

### 테스트 시나리오 (핵심)
1. **이용 완료 성공**: 모든 주문 COMPLETED → 아카이빙 → 논리적 삭제 → 세션 종료
2. **이용 완료 실패 - 미완료 주문**: PENDING 주문 존재 시 400 에러
3. **이용 완료 실패 - 주문 없음**: 주문 0건 시 400 에러
4. **이용 완료 실패 - 활성 세션 없음**: 세션 없을 때 400 에러
5. **테이블 설정 - 신규**: 새 테이블 생성
6. **테이블 설정 - 중복**: 기존 테이블 비밀번호 업데이트
7. **이력 조회 - 전체**: 날짜 필터 없이 전체 조회
8. **이력 조회 - orderedAt 필터**: 주문 시각 기준 필터
9. **이력 조회 - completedAt 필터**: 이용 완료 시각 기준 필터
10. **RBAC 검증**: STAFF 역할로 이용 완료/테이블 설정 시도 시 403
11. **매장 격리**: 다른 매장 테이블 접근 시 403

---

## 6. 트랜잭션 설계

### 이용 완료 트랜잭션
```
@Transactional(isolation = Isolation.READ_COMMITTED)
completeSession(tableId):
  1. 활성 세션 조회 (SELECT)
  2. 주문 목록 조회 (SELECT)
  3. 주문 상태 검증
  4. OrderHistory 배치 INSERT
  5. Order 배치 UPDATE (deleted = true)
  6. TableSession UPDATE (endTime = NOW)
  -- 트랜잭션 커밋 --
  7. SSE 이벤트 발행 (트랜잭션 외부)
```

> **SSE 이벤트 발행은 트랜잭션 외부에서 실행**: 이벤트 발행 실패가 데이터 무결성에 영향을 주지 않도록 격리

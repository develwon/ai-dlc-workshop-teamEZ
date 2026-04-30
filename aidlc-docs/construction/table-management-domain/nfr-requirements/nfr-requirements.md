# NFR Requirements - Unit 5: table-management-domain

> **상속**: Unit 1 (common-foundation)의 NFR 요구사항을 전체 상속합니다.
> 이 문서는 Unit 5에 특화된 추가/강화 요구사항만 기술합니다.

---

## 1. 성능 요구사항 (Performance)

### 상속 (Unit 1)
- PERF-01: API 응답 시간 300ms 이내 (p95) - 테이블 목록, 이력 조회에 적용
- PERF-05: DB 커넥션 풀 최소 10, 최대 30

### Unit 5 특화
| ID | 요구사항 | 목표 | 측정 방법 |
|----|----------|------|-----------|
| PERF-U5-01 | 이용 완료 API 응답 시간 | 1000ms 이내 (p95) | 아카이빙 포함 전체 트랜잭션 |
| PERF-U5-02 | 이력 조회 API 응답 시간 | 500ms 이내 (p95) | 날짜 필터 포함 |
| PERF-U5-03 | 테이블 목록 조회 응답 시간 | 300ms 이내 (p95) | 활성 세션 여부 포함 |
| PERF-U5-04 | 아카이빙 처리량 | 세션당 최대 50건 주문 처리 | 단일 트랜잭션 내 |

### 성능 설계 지침
- 이용 완료 시 아카이빙은 단일 트랜잭션으로 처리하되, 배치 INSERT 활용
- 이력 조회 시 날짜 범위 인덱스 활용 (orderedAt, completedAt)
- 테이블 목록 조회 시 활성 세션 여부는 LEFT JOIN으로 한 번에 조회 (N+1 방지)

---

## 2. 보안 요구사항 (Security)

### 상속 (Unit 1)
- SEC-NFR-01 ~ SEC-NFR-12 전체 적용

### Unit 5 특화
| ID | 요구사항 | 구현 방식 |
|----|----------|-----------|
| SEC-U5-01 | 테이블 설정은 OWNER, MANAGER만 가능 | @PreAuthorize 또는 서비스 레이어 역할 검증 |
| SEC-U5-02 | 이용 완료는 OWNER, MANAGER만 가능 | @PreAuthorize 또는 서비스 레이어 역할 검증 |
| SEC-U5-03 | 주문 삭제는 OWNER, MANAGER만 가능 | @PreAuthorize 또는 서비스 레이어 역할 검증 |
| SEC-U5-04 | 매장 격리: 모든 API에서 storeId 검증 | StoreIsolationValidator 활용 |
| SEC-U5-05 | 테이블 비밀번호 bcrypt 해싱 | PasswordEncoder.encode() |
| SEC-U5-06 | 아카이빙 데이터 무결성 | 트랜잭션 보장, 부분 아카이빙 방지 |

### SECURITY Extension 규칙 매핑 (Unit 5 관련)
| SECURITY 규칙 | 적용 여부 | Unit 5 구현 |
|---------------|-----------|-------------|
| SECURITY-05 (입력 검증) | ✅ | CreateTableRequest @Valid, 날짜 파라미터 검증 |
| SECURITY-08 (접근 제어) | ✅ | RBAC (OWNER/MANAGER), 매장 격리, IDOR 방지 |
| SECURITY-11 (보안 설계) | ✅ | 이용 완료 전제조건 검증 (미완료 주문 차단) |
| SECURITY-12 (인증 관리) | ✅ | JWT 검증, 테이블 비밀번호 bcrypt |
| SECURITY-13 (데이터 무결성) | ✅ | 아카이빙 트랜잭션, 감사 로깅 |
| SECURITY-15 (예외 처리) | ✅ | 모든 외부 호출 try/catch, fail-closed |

---

## 3. 데이터 무결성 요구사항 (Data Integrity)

| ID | 요구사항 | 설계 지침 |
|----|----------|-----------|
| INT-U5-01 | 아카이빙 트랜잭션 원자성 | @Transactional로 아카이빙+삭제+세션종료 일괄 처리 |
| INT-U5-02 | 논리적 삭제 일관성 | deleted=true인 주문은 모든 조회에서 제외 |
| INT-U5-03 | OrderHistory 데이터 불변성 | 아카이빙된 이력은 수정/삭제 불가 (읽기 전용) |
| INT-U5-04 | JSON 직렬화 정확성 | OrderItems → JSON 변환 시 데이터 손실 방지 |
| INT-U5-05 | 세션 일관성 | 테이블당 활성 세션 최대 1개 보장 |

---

## 4. 확장성 요구사항 (Scalability)

### 상속 (Unit 1)
- SCAL-01 ~ SCAL-04 전체 적용

### Unit 5 특화
| ID | 요구사항 | 설계 지침 |
|----|----------|-----------|
| SCAL-U5-01 | OrderHistory 데이터 증가 대응 | 날짜 기반 인덱스, 향후 파티셔닝 고려 |
| SCAL-U5-02 | 이력 조회 페이지네이션 | 대량 이력 데이터 대응 (향후 확장) |

---

## 5. 가용성 요구사항 (Availability)

### 상속 (Unit 1)
- AVAIL-01 ~ AVAIL-04 전체 적용

### Unit 5 특화
| ID | 요구사항 | 설계 지침 |
|----|----------|-----------|
| AVAIL-U5-01 | 아카이빙 실패 시 롤백 | 트랜잭션 실패 시 전체 롤백, 재시도 가능 |
| AVAIL-U5-02 | SSE 이벤트 발행 실패 격리 | SSE 실패가 이용 완료 트랜잭션에 영향 없음 |

---

## 6. 유지보수성 요구사항 (Maintainability)

### 상속 (Unit 1)
- MAINT-01 ~ MAINT-05 전체 적용

### Unit 5 특화
| ID | 요구사항 | 설계 지침 |
|----|----------|-----------|
| MAINT-U5-01 | 단위 테스트 커버리지 | AdminTableController, TableSessionService(확장), OrderHistoryService |
| MAINT-U5-02 | 아카이빙 로직 테스트 | Order→OrderHistory 변환 정확성 검증 |
| MAINT-U5-03 | 논리적 삭제 테스트 | deleted 플래그 동작 검증 |

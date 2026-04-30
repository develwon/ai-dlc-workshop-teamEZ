# NFR Requirements - Unit 3: menu-domain

> Unit 1 (common-foundation)에서 정의된 전체 시스템 NFR을 기반으로, menu-domain에 특화된 요구사항을 정의합니다.

---

## 1. 성능 요구사항 (Performance)

| ID | 요구사항 | 목표 | 비고 |
|----|----------|------|------|
| PERF-M01 | 메뉴 목록 조회 응답 시간 | 300ms 이내 (p95) | Unit 1 PERF-01 적용 |
| PERF-M02 | 메뉴 상세 조회 응답 시간 | 300ms 이내 (p95) | Unit 1 PERF-01 적용 |
| PERF-M03 | 카테고리 목록 조회 응답 시간 | 200ms 이내 (p95) | 단순 조회 |
| PERF-M04 | 메뉴 등록/수정 응답 시간 (이미지 제외) | 500ms 이내 (p95) | DB 쓰기 포함 |
| PERF-M05 | 이미지 업로드 응답 시간 | 3초 이내 (p95) | S3 업로드 포함, 5MB 기준 |
| PERF-M06 | 매장당 메뉴 수 | 최대 200개 | 성능 설계 기준 |
| PERF-M07 | 매장당 카테고리 수 | 최대 20개 | 성능 설계 기준 |

### 성능 설계 지침
- 메뉴 조회 시 `(store_id, category_id, display_order)` 복합 인덱스 활용
- 카테고리 조회 시 `(store_id, display_order)` 인덱스 활용
- N+1 문제 방지: 메뉴 목록 조회 시 단일 쿼리로 처리
- 이미지 업로드는 비동기 처리 고려 (현재 MVP에서는 동기 처리)

---

## 2. 보안 요구사항 (Security)

| ID | 요구사항 | 구현 방식 | SECURITY 규칙 |
|----|----------|-----------|---------------|
| SEC-M01 | 관리자 메뉴 CRUD 권한 검증 | RBAC (OWNER, MANAGER만 허용) | SECURITY-08 |
| SEC-M02 | 고객 메뉴 조회 인증 | 테이블 JWT 토큰 검증 | SECURITY-08 |
| SEC-M03 | 매장 데이터 격리 | storeId 기반 필터링 필수 | SECURITY-08 |
| SEC-M04 | 입력 검증 | Bean Validation (@Valid) | SECURITY-05 |
| SEC-M05 | 이미지 파일 검증 | 파일 크기 + MIME 타입 검증 | SECURITY-05 |
| SEC-M06 | S3 접근 제어 | IAM 역할 기반, 최소 권한 | SECURITY-06 |
| SEC-M07 | 에러 응답 보안 | 스택 트레이스 노출 금지 | SECURITY-09 |
| SEC-M08 | SQL Injection 방지 | JPA 파라미터 바인딩 | SECURITY-05 |

---

## 3. 확장성 요구사항 (Scalability)

| ID | 요구사항 | 설계 지침 |
|----|----------|-----------|
| SCAL-M01 | 다중 매장 메뉴 독립 관리 | storeId 기반 완전 격리 |
| SCAL-M02 | 메뉴 수 증가 대응 | 인덱스 기반 조회, 페이징 미적용 (MVP) |
| SCAL-M03 | 이미지 저장소 확장 | S3 무제한 스토리지 |

---

## 4. 가용성 요구사항 (Availability)

| ID | 요구사항 | 설계 지침 |
|----|----------|-----------|
| AVAIL-M01 | S3 업로드 실패 처리 | 이미지 없이 메뉴 등록 가능, 재시도 안내 |
| AVAIL-M02 | S3 삭제 실패 처리 | 로그 기록 후 비즈니스 로직 계속 진행 |
| AVAIL-M03 | DB 연결 실패 처리 | Unit 1 AVAIL-03 적용 (커넥션 풀 재시도) |

---

## 5. 유지보수성 요구사항 (Maintainability)

| ID | 요구사항 | 설계 지침 |
|----|----------|-----------|
| MAINT-M01 | 계층형 아키텍처 준수 | Controller → Service → Repository |
| MAINT-M02 | 단위 테스트 | MenuService, FileStorageService 테스트 필수 |
| MAINT-M03 | Repository 테스트 | CategoryRepository, MenuRepository 테스트 필수 |
| MAINT-M04 | Controller 테스트 | CustomerMenuController, AdminMenuController 테스트 필수 |
| MAINT-M05 | 구조화된 로깅 | SLF4J, 요청 ID 포함 |

---

## 6. SECURITY Extension 규칙 준수 현황

| SECURITY 규칙 | 적용 여부 | Unit 3 구현 위치 |
|---------------|-----------|-----------------|
| SECURITY-01 (암호화) | N/A | Unit 1에서 처리 (TLS, DB 암호화) |
| SECURITY-02 (접근 로깅) | N/A | Unit 1에서 처리 (인프라 레벨) |
| SECURITY-03 (앱 로깅) | ✅ | SLF4J 로거 사용, 민감 정보 로깅 금지 |
| SECURITY-04 (HTTP 헤더) | N/A | Unit 1 SecurityConfig에서 처리 |
| SECURITY-05 (입력 검증) | ✅ | Bean Validation, 이미지 파일 검증 |
| SECURITY-06 (최소 권한) | ✅ | S3 IAM 역할 최소 권한 |
| SECURITY-07 (네트워크) | N/A | 인프라 레벨 |
| SECURITY-08 (접근 제어) | ✅ | RBAC + storeId 격리 |
| SECURITY-09 (보안 강화) | ✅ | 에러 응답 보안 |
| SECURITY-10 (공급망) | N/A | Unit 1 pom.xml에서 처리 |
| SECURITY-11 (보안 설계) | ✅ | 계층 분리, 입력 검증 |
| SECURITY-12 (인증 관리) | N/A | Unit 2 auth-domain에서 처리 |
| SECURITY-13 (무결성) | N/A | 인프라 레벨 |
| SECURITY-14 (모니터링) | N/A | 인프라 레벨 |
| SECURITY-15 (예외 처리) | ✅ | 모든 외부 호출 try-catch, GlobalExceptionHandler |

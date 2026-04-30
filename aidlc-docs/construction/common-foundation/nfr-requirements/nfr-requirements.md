# NFR Requirements - Unit 1: common-foundation

---

## 1. 성능 요구사항 (Performance)

| ID | 요구사항 | 목표 | 측정 방법 |
|----|----------|------|-----------|
| PERF-01 | API 응답 시간 (일반) | 300ms 이내 (p95) | 메뉴 조회, 주문 내역 조회 등 |
| PERF-02 | API 응답 시간 (주문 생성) | 500ms 이내 (p95) | 주문 생성 API |
| PERF-03 | SSE 이벤트 전달 지연 | 2초 이내 | 주문 생성 → 관리자 대시보드 표시 |
| PERF-04 | 동시 주문 처리량 | 분당 10~50건 (피크) | 부하 테스트 |
| PERF-05 | DB 커넥션 풀 | 최소 10, 최대 30 | HikariCP 설정 |
| PERF-06 | SSE 동시 연결 | 매장당 최대 10개 | SseEmitterManager |

### 성능 설계 지침
- JPA N+1 문제 방지: 필요한 경우 fetch join 또는 @EntityGraph 사용
- 메뉴 조회 시 카테고리별 인덱스 활용
- 주문 조회 시 세션 ID 기반 인덱스 활용

---

## 2. 보안 요구사항 (Security)

| ID | 요구사항 | 구현 방식 |
|----|----------|-----------|
| SEC-NFR-01 | 전송 암호화 | HTTPS (TLS 1.2+) 필수 |
| SEC-NFR-02 | 비밀번호 저장 | bcrypt 해싱 (cost factor 10) |
| SEC-NFR-03 | 인증 토큰 | JWT (HS256 또는 RS256), 16시간 만료 |
| SEC-NFR-04 | 입력 검증 | Bean Validation (@Valid) + 커스텀 검증 |
| SEC-NFR-05 | SQL Injection 방지 | JPA/Hibernate 파라미터 바인딩 (네이티브 쿼리 금지) |
| SEC-NFR-06 | XSS 방지 | 입력 값 이스케이프, CSP 헤더 |
| SEC-NFR-07 | CORS 정책 | 허용 오리진 명시적 설정 (와일드카드 금지) |
| SEC-NFR-08 | HTTP 보안 헤더 | CSP, HSTS, X-Content-Type-Options, X-Frame-Options, Referrer-Policy |
| SEC-NFR-09 | 로그인 시도 제한 | 5회 실패 시 30분 잠금 |
| SEC-NFR-10 | 세션 쿠키 속성 | Secure, HttpOnly, SameSite=Strict |
| SEC-NFR-11 | 에러 응답 | 스택 트레이스 노출 금지, 일반적 메시지만 반환 |
| SEC-NFR-12 | 매장 데이터 격리 | storeId 기반 필터링 필수 |

### SECURITY Extension 규칙 매핑
| SECURITY 규칙 | 적용 여부 | 구현 위치 |
|---------------|-----------|-----------|
| SECURITY-01 (암호화) | ✅ | application.yml (TLS), DB 연결 |
| SECURITY-03 (앱 로깅) | ✅ | Logback + SLF4J 설정 |
| SECURITY-04 (HTTP 헤더) | ✅ | SecurityConfig |
| SECURITY-05 (입력 검증) | ✅ | DTO @Valid + 커스텀 Validator |
| SECURITY-08 (접근 제어) | ✅ | JwtAuthFilter + RBAC |
| SECURITY-09 (보안 강화) | ✅ | GlobalExceptionHandler |
| SECURITY-10 (공급망) | ✅ | pom.xml 버전 고정 |
| SECURITY-12 (인증 관리) | ✅ | AdminAuthService, 로그인 잠금 |
| SECURITY-15 (예외 처리) | ✅ | GlobalExceptionHandler |

---

## 3. 확장성 요구사항 (Scalability)

| ID | 요구사항 | 설계 지침 |
|----|----------|-----------|
| SCAL-01 | 다중 매장 지원 | storeId 기반 데이터 격리, 매장 간 독립 운영 |
| SCAL-02 | 매장당 테이블 최대 30개 | 테이블 수 기반 성능 설계 |
| SCAL-03 | 수평 확장 가능 | 상태 비저장(Stateless) 설계, JWT 기반 인증 |
| SCAL-04 | SSE 확장 | 매장별 emitter 관리, 연결 수 제한 |

---

## 4. 가용성 요구사항 (Availability)

| ID | 요구사항 | 설계 지침 |
|----|----------|-----------|
| AVAIL-01 | 서비스 가용성 99.5% | 연간 다운타임 약 44시간 이내 |
| AVAIL-02 | Graceful degradation | SSE 실패 시 폴링 폴백 가능 |
| AVAIL-03 | DB 연결 실패 처리 | 커넥션 풀 재시도, 타임아웃 설정 |
| AVAIL-04 | 헬스 체크 | Spring Boot Actuator /health 엔드포인트 |

---

## 5. 유지보수성 요구사항 (Maintainability)

| ID | 요구사항 | 설계 지침 |
|----|----------|-----------|
| MAINT-01 | 계층형 아키텍처 준수 | Controller → Service → Repository 단방향 의존 |
| MAINT-02 | 단위 테스트 | 모든 Service, Repository 레이어 테스트 |
| MAINT-03 | 구조화된 로깅 | SLF4J + Logback, 요청 ID 포함 |
| MAINT-04 | API 문서화 | Swagger/OpenAPI 자동 생성 (선택) |
| MAINT-05 | 코드 컨벤션 | Java 표준 코딩 컨벤션 준수 |

---

## 6. 사용성 요구사항 (Usability)

| ID | 요구사항 | 설계 지침 |
|----|----------|-----------|
| USAB-01 | 터치 친화적 UI | 최소 44x44px 터치 타겟 |
| USAB-02 | 반응형 디자인 | 태블릿 최적화 (768px~1024px) |
| USAB-03 | 접근성 | WCAG 기본 수준 (시맨틱 HTML, alt 텍스트, 키보드 네비게이션) |
| USAB-04 | 로딩 상태 표시 | API 호출 시 로딩 인디케이터 |
| USAB-05 | 에러 피드백 | 사용자 친화적 에러 메시지 |

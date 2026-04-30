# NFR Design Patterns - Unit 1: common-foundation

---

## 1. 보안 패턴

### 1.1 JWT 인증 필터 체인 패턴
```
HTTP Request
    |
    v
+---------------------------+
| CorsFilter                |  CORS 정책 적용
+---------------------------+
    |
    v
+---------------------------+
| SecurityHeaderFilter      |  HTTP 보안 헤더 추가
| (CSP, HSTS, X-Frame, etc)|
+---------------------------+
    |
    v
+---------------------------+
| JwtAuthenticationFilter   |  JWT 토큰 검증
| - 토큰 추출 (Bearer)      |
| - 토큰 검증 (서명, 만료)   |
| - SecurityContext 설정     |
+---------------------------+
    |
    v
+---------------------------+
| RoleBasedAccessControl    |  역할 기반 접근 제어
| - @PreAuthorize 어노테이션 |
| - 엔드포인트별 역할 매핑    |
+---------------------------+
    |
    v
+---------------------------+
| Controller                |  비즈니스 로직 처리
+---------------------------+
```

**공개 엔드포인트 (인증 불필요)**:
- `POST /api/tables/login` - 테이블 로그인
- `POST /api/admin/login` - 관리자 로그인
- `GET /static/**` - 정적 리소스

**테이블 인증 필요**:
- `GET /api/stores/{storeId}/menus/**` - 메뉴 조회
- `GET /api/stores/{storeId}/categories` - 카테고리 조회
- `POST /api/orders` - 주문 생성
- `GET /api/tables/{tableId}/orders` - 주문 내역 조회

**관리자 인증 필요 (역할별)**:
- `GET /api/admin/orders/**` - 전체 관리자 역할
- `PATCH /api/admin/orders/{id}/status` - 전체 관리자 역할
- `DELETE /api/admin/orders/{id}` - OWNER, MANAGER만
- `POST /api/admin/tables/**` - OWNER, MANAGER만
- `GET/POST/PUT/DELETE /api/admin/menus/**` - OWNER, MANAGER만

### 1.2 매장 격리 패턴 (Tenant Isolation)
```java
// 모든 Service 메서드에서 storeId 검증
// JWT에서 추출한 storeId와 요청 데이터의 storeId 일치 확인
@Component
public class StoreIsolationValidator {
    public void validateStoreAccess(Long jwtStoreId, Long requestStoreId) {
        if (!jwtStoreId.equals(requestStoreId)) {
            throw new ForbiddenException("매장 접근 권한이 없습니다.");
        }
    }
}
```

### 1.3 로그인 시도 제한 패턴 (Rate Limiting)
```
로그인 시도
    |
    v
[계정 잠금 확인] --잠금중--> 423 Account Locked
    |
    |정상
    v
[인증 시도]
    |
    +--성공--> loginAttempts = 0, JWT 발급
    |
    +--실패--> loginAttempts++
                |
                +-- < 5회 --> 401 Unauthorized
                |
                +-- >= 5회 --> lockedUntil = now + 30분, 423 Account Locked
```

---

## 2. 성능 패턴

### 2.1 커넥션 풀 패턴 (HikariCP)
| 설정 | 값 | 근거 |
|------|-----|------|
| minimum-idle | 10 | 기본 유휴 연결 수 |
| maximum-pool-size | 30 | 피크 시 최대 연결 (매장당 30 테이블 기준) |
| connection-timeout | 30000ms | 연결 획득 대기 시간 |
| idle-timeout | 600000ms | 유휴 연결 유지 시간 (10분) |
| max-lifetime | 1800000ms | 연결 최대 수명 (30분) |

### 2.2 JPA 최적화 패턴
- **Batch Fetch**: `default_batch_fetch_size: 100` (N+1 방지)
- **Lazy Loading**: 기본 FetchType.LAZY, 필요 시 @EntityGraph
- **읽기 전용 트랜잭션**: 조회 메서드에 `@Transactional(readOnly = true)`
- **DTO Projection**: 목록 조회 시 엔티티 대신 DTO 직접 조회 고려

### 2.3 SSE 연결 관리 패턴
```
관리자 대시보드 접속
    |
    v
+---------------------------+
| SseEmitterManager         |
| - storeId별 emitter 맵    |
| - 타임아웃: 30분           |
| - 최대 연결: 매장당 10개   |
+---------------------------+
    |
    | 주문 이벤트 발생 시
    v
+---------------------------+
| 매장별 브로드캐스트         |
| - 해당 storeId의 모든      |
|   emitter에 이벤트 전송    |
| - 실패한 emitter 자동 제거 |
+---------------------------+
```

---

## 3. 복원력 패턴

### 3.1 전역 예외 처리 패턴 (Fail-Safe)
```
예외 발생
    |
    v
+---------------------------+
| GlobalExceptionHandler    |
| (@ControllerAdvice)       |
+---------------------------+
    |
    +-- ValidationException     --> 400 + 필드별 에러 목록
    +-- UnauthorizedException   --> 401 + 일반 메시지
    +-- ForbiddenException      --> 403 + 일반 메시지
    +-- NotFoundException       --> 404 + 리소스 정보
    +-- ConflictException       --> 409 + 충돌 정보
    +-- AccountLockedException  --> 423 + 잠금 해제 시각
    +-- InvalidStateException   --> 400 + 상태 전이 정보
    +-- Exception (기타)        --> 500 + 일반 메시지 (스택 트레이스 숨김)
```

### 3.2 SSE 복원력 패턴
- **연결 타임아웃**: 30분 후 자동 종료, 클라이언트 재연결
- **에러 핸들링**: emitter 전송 실패 시 자동 제거
- **하트비트**: 15초 간격 빈 이벤트 전송 (연결 유지)
- **재연결 지원**: 클라이언트에서 EventSource 자동 재연결

### 3.3 트랜잭션 관리 패턴
- **주문 생성**: Order + OrderItem을 하나의 트랜잭션으로 처리
- **이용 완료**: 세션 종료 + 이력 아카이빙을 하나의 트랜잭션으로 처리
- **롤백**: 예외 발생 시 자동 롤백 (Spring @Transactional)

---

## 4. 로깅 패턴

### 4.1 구조화된 로깅
```
[요청 수신] INFO  - Request: POST /api/orders [requestId=abc123] [storeId=1] [tableId=5]
[비즈니스]  DEBUG - Creating order for session: 42, items: 3
[응답 전송] INFO  - Response: 201 Created [requestId=abc123] [duration=120ms]
[에러 발생] ERROR - Order creation failed [requestId=abc123] [error=MenuNotFoundException]
```

### 4.2 로깅 규칙
- 민감 정보 로깅 금지 (비밀번호, JWT 토큰, 개인정보)
- 요청 ID(requestId) 포함하여 요청 추적 가능
- storeId 포함하여 매장별 로그 필터링 가능
- MDC (Mapped Diagnostic Context) 활용

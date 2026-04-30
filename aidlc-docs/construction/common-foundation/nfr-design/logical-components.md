# Logical Components - Unit 1: common-foundation

---

## 1. 보안 컴포넌트

### JwtTokenProvider
| 항목 | 내용 |
|------|------|
| **책임** | JWT 토큰 생성, 검증, 파싱 |
| **알고리즘** | HS256 (HMAC-SHA256) |
| **만료 시간** | 16시간 (57,600,000ms) |
| **클레임** | sub(userId), storeId, role(관리자만), type(TABLE/ADMIN), iat, exp |

**메서드**:
- `generateTableToken(tableId, storeId)` → String
- `generateAdminToken(adminId, storeId, role)` → String
- `validateToken(token)` → boolean
- `getClaimsFromToken(token)` → Claims
- `getStoreIdFromToken(token)` → Long
- `getRoleFromToken(token)` → AdminRole (관리자 토큰만)

### JwtAuthenticationFilter (OncePerRequestFilter)
| 항목 | 내용 |
|------|------|
| **책임** | HTTP 요청에서 JWT 추출 및 인증 처리 |
| **토큰 위치** | Authorization: Bearer {token} |
| **처리 흐름** | 토큰 추출 → 검증 → SecurityContext 설정 |

### SecurityConfig
| 항목 | 내용 |
|------|------|
| **책임** | Spring Security 전체 설정 |
| **CORS** | 허용 오리진 명시적 설정 |
| **CSRF** | 비활성화 (JWT 기반 Stateless) |
| **세션** | STATELESS |
| **HTTP 보안 헤더** | CSP, HSTS, X-Content-Type-Options, X-Frame-Options, Referrer-Policy |

### RoleBasedAccessControl
| 항목 | 내용 |
|------|------|
| **책임** | 역할 기반 엔드포인트 접근 제어 |
| **구현** | @PreAuthorize + SpEL 또는 SecurityConfig URL 패턴 매칭 |

### StoreIsolationValidator
| 항목 | 내용 |
|------|------|
| **책임** | 매장 간 데이터 격리 검증 |
| **구현** | JWT storeId와 요청 storeId 일치 확인 |

---

## 2. 인프라 컴포넌트

### SseEmitterManager
| 항목 | 내용 |
|------|------|
| **책임** | SSE 연결 관리 (등록, 제거, 브로드캐스트) |
| **데이터 구조** | `ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>>` (storeId → emitters) |
| **타임아웃** | 30분 (1,800,000ms) |
| **하트비트** | 15초 간격 |
| **최대 연결** | 매장당 10개 |

**메서드**:
- `createEmitter(storeId)` → SseEmitter
- `sendEvent(storeId, eventName, data)` → void
- `removeEmitter(storeId, emitter)` → void
- `getEmitterCount(storeId)` → int

### S3FileStorageService
| 항목 | 내용 |
|------|------|
| **책임** | AWS S3 이미지 업로드/삭제 |
| **버킷** | 환경 변수로 설정 (${S3_BUCKET_NAME}) |
| **키 형식** | `menus/{storeId}/{UUID}.{extension}` |
| **허용 형식** | jpg, jpeg, png, gif, webp |
| **최대 크기** | 10MB |

**메서드**:
- `uploadImage(storeId, file)` → String (imageUrl)
- `deleteImage(imageUrl)` → void
- `validateImageFile(file)` → void (형식/크기 검증)

### GlobalExceptionHandler (@ControllerAdvice)
| 항목 | 내용 |
|------|------|
| **책임** | 전역 예외 처리, 통일된 에러 응답 |
| **응답 형식** | `{error, message, timestamp, path}` |
| **프로덕션** | 스택 트레이스 숨김 |

### RequestIdFilter (OncePerRequestFilter)
| 항목 | 내용 |
|------|------|
| **책임** | 요청별 고유 ID 생성 및 MDC 설정 |
| **ID 형식** | UUID (축약) |
| **MDC 키** | requestId, storeId |

---

## 3. 공통 엔티티 컴포넌트

### BaseEntity (추상 클래스)
| 필드 | 타입 | 설명 |
|------|------|------|
| createdAt | LocalDateTime | @PrePersist 자동 설정 |
| updatedAt | LocalDateTime | @PreUpdate 자동 갱신 |

모든 엔티티가 BaseEntity를 상속하여 타임스탬프 자동 관리.

### OrderNumberGenerator
| 항목 | 내용 |
|------|------|
| **책임** | 고유 주문 번호 생성 |
| **형식** | `yyyyMMdd-HHmmss-XXXX` (X=랜덤 영숫자 4자리) |
| **고유성** | DB UNIQUE 제약 + 충돌 시 재생성 |

---

## 4. DTO 컴포넌트

### 공통 응답 DTO
| DTO | 용도 |
|-----|------|
| ErrorResponse | 통일된 에러 응답 (error, message, timestamp, path) |
| ValidationErrorResponse | 검증 에러 응답 (ErrorResponse + fieldErrors[]) |

### 인증 DTO
| DTO | 용도 |
|-----|------|
| TableLoginRequest | 테이블 로그인 요청 (storeCode, tableNumber, password) |
| TableLoginResponse | 테이블 로그인 응답 (token, tableId, storeId, sessionId) |
| AdminLoginRequest | 관리자 로그인 요청 (storeCode, username, password) |
| AdminLoginResponse | 관리자 로그인 응답 (token, role, storeId, storeName) |

### 메뉴 DTO
| DTO | 용도 |
|-----|------|
| CategoryResponse | 카테고리 응답 |
| MenuResponse | 메뉴 목록 응답 |
| MenuDetailResponse | 메뉴 상세 응답 |
| CreateMenuRequest | 메뉴 등록 요청 (@Valid) |
| UpdateMenuRequest | 메뉴 수정 요청 (@Valid) |
| MenuOrderRequest | 메뉴 순서 변경 요청 |

### 주문 DTO
| DTO | 용도 |
|-----|------|
| CreateOrderRequest | 주문 생성 요청 (items[]) |
| OrderItemRequest | 주문 항목 요청 (menuId, quantity) |
| OrderResponse | 주문 응답 |
| AdminOrderResponse | 관리자 주문 응답 (테이블 정보 포함) |
| AdminOrderDetailResponse | 관리자 주문 상세 응답 |
| StatusUpdateRequest | 주문 상태 변경 요청 |
| OrderEventData | SSE 이벤트 데이터 |

### 테이블 DTO
| DTO | 용도 |
|-----|------|
| CreateTableRequest | 테이블 설정 요청 |
| TableResponse | 테이블 응답 |
| TableSummaryResponse | 테이블 요약 응답 (총 주문액 포함) |
| OrderHistoryResponse | 과거 주문 이력 응답 |

# NFR Design Patterns - Unit 3: menu-domain

> Unit 1의 공통 패턴(JWT 인증 필터 체인, 매장 격리, 전역 예외 처리, 구조화된 로깅)을 그대로 적용합니다.
> 여기서는 menu-domain에 특화된 패턴만 정의합니다.

---

## 1. 보안 패턴 (Unit 1 적용)

### 1.1 인증/권한 체인 (Unit 1 패턴 그대로 적용)
```
HTTP Request → CorsFilter → SecurityHeaderFilter → JwtAuthenticationFilter → RoleBasedAccessControl → Controller
```

**menu-domain 엔드포인트 권한 매핑**:

| 엔드포인트 | 인증 유형 | 역할 |
|-----------|-----------|------|
| GET /api/stores/{storeId}/categories | 테이블 JWT | 고객 |
| GET /api/stores/{storeId}/menus | 테이블 JWT | 고객 |
| GET /api/stores/{storeId}/menus/{menuId} | 테이블 JWT | 고객 |
| GET /api/admin/menus | 관리자 JWT | OWNER, MANAGER, STAFF |
| POST /api/admin/menus | 관리자 JWT | OWNER, MANAGER |
| PUT /api/admin/menus/{menuId} | 관리자 JWT | OWNER, MANAGER |
| DELETE /api/admin/menus/{menuId} | 관리자 JWT | OWNER, MANAGER |
| PATCH /api/admin/menus/order | 관리자 JWT | OWNER, MANAGER |
| GET /api/admin/categories | 관리자 JWT | OWNER, MANAGER, STAFF |
| POST /api/admin/categories | 관리자 JWT | OWNER, MANAGER |
| PUT /api/admin/categories/{categoryId} | 관리자 JWT | OWNER, MANAGER |
| DELETE /api/admin/categories/{categoryId} | 관리자 JWT | OWNER, MANAGER |

### 1.2 매장 격리 패턴 (Unit 1 StoreIsolationValidator 활용)
- 모든 Service 메서드에서 storeId 검증
- Controller에서 JWT의 storeId 추출 후 Service에 전달
- Repository 쿼리에 storeId 조건 필수 포함

---

## 2. 이미지 업로드 복원력 패턴

### 2.1 이미지 업로드 플로우 (Fail-Safe)
```
이미지 업로드 요청
    |
    v
[파일 검증]
    |
    +-- 크기 초과 (>5MB) --> 400 Bad Request
    +-- 형식 오류 --> 400 Bad Request
    |
    |정상
    v
[S3 업로드]
    |
    +-- 성공 --> imageUrl 반환
    |
    +-- 실패 --> 500 Internal Server Error
              (메뉴 등록/수정 전체 롤백)
```

### 2.2 이미지 삭제 플로우 (Best-Effort)
```
이미지 삭제 요청 (메뉴 삭제/수정 시)
    |
    v
[S3 삭제]
    |
    +-- 성공 --> 정상 진행
    |
    +-- 실패 --> WARN 로그 기록
              (비즈니스 로직 중단하지 않음)
              (고아 이미지는 별도 정리 배치로 처리 가능)
```

**설계 근거**: 이미지 삭제 실패가 메뉴 삭제/수정을 막으면 안 됨. 고아 이미지는 S3 라이프사이클 정책이나 별도 배치로 정리.

### 2.3 이미지 파일 검증 패턴
```java
// FileStorageService 내부
validateImageFile(MultipartFile file):
  1. file == null || file.isEmpty() → IllegalArgumentException
  2. file.getSize() > 5MB → IllegalArgumentException("이미지 파일 크기는 5MB를 초과할 수 없습니다")
  3. contentType NOT IN [image/jpeg, image/png, image/gif, image/webp]
     → IllegalArgumentException("허용되지 않는 파일 형식입니다")
  4. 파일 확장자와 contentType 일치 확인 (MIME 스니핑 방지)
```

---

## 3. 트랜잭션 관리 패턴

### 3.1 메뉴 CRUD 트랜잭션
| 작업 | 트랜잭션 범위 | readOnly |
|------|-------------|----------|
| 메뉴 목록 조회 | @Transactional(readOnly=true) | ✅ |
| 메뉴 상세 조회 | @Transactional(readOnly=true) | ✅ |
| 카테고리 목록 조회 | @Transactional(readOnly=true) | ✅ |
| 메뉴 등록 | @Transactional | ❌ |
| 메뉴 수정 | @Transactional | ❌ |
| 메뉴 삭제 | @Transactional | ❌ |
| 메뉴 순서 변경 | @Transactional | ❌ |
| 카테고리 등록 | @Transactional | ❌ |
| 카테고리 수정 | @Transactional | ❌ |
| 카테고리 삭제 | @Transactional | ❌ |

### 3.2 이미지 + DB 트랜잭션 순서
```
메뉴 등록 (이미지 포함):
  1. 이미지 S3 업로드 (트랜잭션 외부)
  2. DB 저장 (트랜잭션 내부)
  3. DB 실패 시 → S3 이미지 삭제 시도 (보상 트랜잭션)

메뉴 수정 (이미지 변경):
  1. 새 이미지 S3 업로드 (트랜잭션 외부)
  2. DB 수정 (트랜잭션 내부)
  3. DB 성공 시 → 기존 이미지 S3 삭제 (best-effort)
  4. DB 실패 시 → 새 이미지 S3 삭제 시도 (보상 트랜잭션)

메뉴 삭제:
  1. DB 삭제 (트랜잭션 내부)
  2. DB 성공 시 → 이미지 S3 삭제 (best-effort)
```

---

## 4. 성능 패턴

### 4.1 JPA 최적화 (Unit 1 패턴 적용)
- **읽기 전용 트랜잭션**: 조회 메서드에 `@Transactional(readOnly = true)`
- **인덱스 활용**: `(store_id, category_id, display_order)` 복합 인덱스
- **Batch Fetch**: `default_batch_fetch_size: 100` (Unit 1 설정)
- **단일 쿼리**: 메뉴 목록 조회 시 JOIN 없이 단일 테이블 쿼리

### 4.2 메뉴 상세 조회 최적화
```
MenuService.getMenuDetail(storeId, menuId):
  1. Menu 조회 (단일 쿼리)
  2. Category 조회 (categoryName 포함)
  → 2개 쿼리로 처리 (JOIN 대신 개별 조회 - 단순성 우선)
```

---

## 5. 로깅 패턴 (Unit 1 적용)

### 5.1 menu-domain 로깅 예시
```
[메뉴 등록] INFO  - Creating menu [requestId=abc123] [storeId=1] [categoryId=3] [menuName=김치찌개]
[메뉴 수정] INFO  - Updating menu [requestId=abc123] [storeId=1] [menuId=15]
[메뉴 삭제] INFO  - Deleting menu [requestId=abc123] [storeId=1] [menuId=15]
[이미지]   INFO  - Uploading image to S3 [requestId=abc123] [storeId=1] [fileSize=2.3MB]
[이미지]   WARN  - Failed to delete S3 image [requestId=abc123] [imageUrl=...] [error=...]
[카테고리] INFO  - Creating category [requestId=abc123] [storeId=1] [categoryName=메인메뉴]
```

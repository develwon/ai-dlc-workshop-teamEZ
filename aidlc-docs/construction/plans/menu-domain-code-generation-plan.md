# Code Generation Plan - Unit 3: menu-domain

## 유닛 컨텍스트
- **유닛명**: menu-domain
- **프로젝트 유형**: Greenfield 모놀리식 (기존 Unit 1 코드 위에 추가)
- **코드 위치**: `src/main/java/com/tableorder/` (기존 패키지 구조 활용)
- **테스트 위치**: `src/test/java/com/tableorder/`
- **관련 스토리**: US-C02 (고객 메뉴 조회/탐색), US-A04 (관리자 메뉴 관리)
- **의존**: Unit 1 (Entity, DTO, Config, Security, Exception 모두 구현 완료)

## 기존 코드 현황 (Unit 1에서 구현 완료)
- Entity: Category, Menu, BaseEntity
- DTO: CreateMenuRequest, UpdateMenuRequest, MenuResponse, MenuDetailResponse, CategoryResponse, CreateCategoryRequest, UpdateCategoryRequest, MenuOrderRequest
- Config: S3Config, SecurityConfig, WebConfig
- Security: JwtTokenProvider, JwtAuthenticationFilter, StoreIsolationValidator
- Exception: GlobalExceptionHandler, NotFoundException, ConflictException, ForbiddenException 등

## 실행 계획

### Step 1: Repository Layer 생성
- [x] CategoryRepository 생성 (`src/main/java/com/tableorder/repository/CategoryRepository.java`)
- [x] MenuRepository 생성 (`src/main/java/com/tableorder/repository/MenuRepository.java`)

### Step 2: Repository Layer 단위 테스트
- [x] CategoryRepositoryTest 생성 (`src/test/java/com/tableorder/repository/CategoryRepositoryTest.java`)
- [x] MenuRepositoryTest 생성 (`src/test/java/com/tableorder/repository/MenuRepositoryTest.java`)

### Step 3: Service Layer - FileStorageService 생성
- [x] FileStorageService 생성 (`src/main/java/com/tableorder/service/FileStorageService.java`)

### Step 4: Service Layer - FileStorageService 단위 테스트
- [x] FileStorageServiceTest 생성 (`src/test/java/com/tableorder/service/FileStorageServiceTest.java`)

### Step 5: Service Layer - MenuService 생성
- [x] MenuService 생성 (`src/main/java/com/tableorder/service/MenuService.java`)

### Step 6: Service Layer - MenuService 단위 테스트
- [x] MenuServiceTest 생성 (`src/test/java/com/tableorder/service/MenuServiceTest.java`)

### Step 7: Controller Layer - CustomerMenuController 생성
- [x] CustomerMenuController 생성 (`src/main/java/com/tableorder/controller/CustomerMenuController.java`)

### Step 8: Controller Layer - AdminMenuController 생성
- [x] AdminMenuController 생성 (`src/main/java/com/tableorder/controller/AdminMenuController.java`)

### Step 9: Controller Layer 단위 테스트
- [x] CustomerMenuControllerTest 생성 (`src/test/java/com/tableorder/controller/CustomerMenuControllerTest.java`)
- [x] AdminMenuControllerTest 생성 (`src/test/java/com/tableorder/controller/AdminMenuControllerTest.java`)

### Step 10: Code Summary 문서 생성
- [x] code-summary.md 생성 (`aidlc-docs/construction/menu-domain/code/code-summary.md`)

## 스토리 추적
- [x] US-C02: 고객 메뉴 조회/탐색 (Step 1, 5, 7)
- [x] US-A04: 관리자 메뉴 관리 (Step 1, 3, 5, 8)

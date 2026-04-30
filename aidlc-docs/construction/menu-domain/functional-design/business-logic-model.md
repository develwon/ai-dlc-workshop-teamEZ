# Business Logic Model - Unit 3: menu-domain

---

## 1. 메뉴 CRUD 비즈니스 플로우

### 1.1 메뉴 등록 (createMenu)
```
AdminMenuController.createMenu(storeId, CreateMenuRequest, image)
  → [권한 검증] RBAC-07: OWNER 또는 MANAGER만 허용
  → [매장 격리] ISO-02: JWT storeId == 요청 storeId 검증
  → [이미지 처리] image != null → FileStorageService.uploadImage(image) → imageUrl
  → MenuService.createMenu(storeId, CreateMenuRequest, imageUrl)
    → [검증] BR-M01: name 비어있지 않음 (1~100자)
    → [검증] BR-M02: price >= 0
    → [검증] BR-M03: price <= 10,000,000
    → [검증] BR-M04: categoryId가 해당 매장의 유효한 카테고리인지 확인
    → [검증] BR-M05: description 최대 500자
    → Menu 엔티티 생성 및 저장
    → MenuResponse 반환
```

### 1.2 메뉴 수정 (updateMenu)
```
AdminMenuController.updateMenu(storeId, menuId, UpdateMenuRequest, image)
  → [권한 검증] RBAC-07: OWNER 또는 MANAGER만 허용
  → [매장 격리] ISO-02: JWT storeId == 요청 storeId 검증
  → MenuService.updateMenu(storeId, menuId, UpdateMenuRequest, imageUrl)
    → [조회] Menu 조회 (menuId + storeId)
    → [검증] 404 Not Found: 메뉴 미존재
    → [검증] BR-M01~M06 동일 적용
    → [이미지 처리] 새 이미지 있으면:
      → 기존 imageUrl != null → FileStorageService.deleteImage(기존 imageUrl)
      → FileStorageService.uploadImage(새 이미지) → 새 imageUrl
    → Menu.update() 호출
    → MenuResponse 반환
```

### 1.3 메뉴 삭제 (deleteMenu)
```
AdminMenuController.deleteMenu(storeId, menuId)
  → [권한 검증] RBAC-07: OWNER 또는 MANAGER만 허용
  → [매장 격리] ISO-02: JWT storeId == 요청 storeId 검증
  → MenuService.deleteMenu(storeId, menuId)
    → [조회] Menu 조회 (menuId + storeId)
    → [검증] 404 Not Found: 메뉴 미존재
    → [이미지 정리] imageUrl != null → FileStorageService.deleteImage(imageUrl)
    → Menu 삭제
```

### 1.4 메뉴 목록 조회 - 관리자 (getMenus)
```
AdminMenuController.getMenus(storeId, categoryId)
  → [권한 검증] OWNER, MANAGER, STAFF 모두 허용 (조회)
  → [매장 격리] ISO-02: JWT storeId 기반 필터링
  → MenuService.getMenusByStore(storeId, categoryId)
    → categoryId != null → MenuRepository.findByStoreIdAndCategoryIdOrderByDisplayOrder()
    → categoryId == null → MenuRepository.findByStoreIdOrderByDisplayOrder()
    → List<MenuResponse> 반환
```

### 1.5 메뉴 노출 순서 변경 (updateMenuOrder)
```
AdminMenuController.updateMenuOrder(storeId, List<MenuOrderRequest>)
  → [권한 검증] RBAC-08: OWNER 또는 MANAGER만 허용
  → [매장 격리] ISO-02: JWT storeId == 요청 storeId 검증
  → MenuService.updateMenuDisplayOrder(storeId, List<MenuOrderRequest>)
    → 각 MenuOrderRequest에 대해:
      → Menu 조회 (menuId + storeId)
      → [검증] 404 Not Found: 메뉴 미존재
      → Menu.updateDisplayOrder() 호출
```

---

## 2. 카테고리 CRUD 비즈니스 플로우

### 2.1 카테고리 등록 (createCategory)
```
AdminMenuController.createCategory(storeId, CreateCategoryRequest)
  → [권한 검증] RBAC-07: OWNER 또는 MANAGER만 허용
  → [매장 격리] ISO-02: JWT storeId == 요청 storeId 검증
  → MenuService.createCategory(storeId, CreateCategoryRequest)
    → [검증] BR-CT01: name 비어있지 않음 (1~50자)
    → [검증] BR-CT02: 동일 매장 내 카테고리명 중복 검사
    → [검증] BR-CT03: displayOrder >= 0
    → Category 엔티티 생성 및 저장
    → CategoryResponse 반환
```

### 2.2 카테고리 수정 (updateCategory)
```
AdminMenuController.updateCategory(storeId, categoryId, UpdateCategoryRequest)
  → [권한 검증] RBAC-07: OWNER 또는 MANAGER만 허용
  → [매장 격리] ISO-02: JWT storeId == 요청 storeId 검증
  → MenuService.updateCategory(storeId, categoryId, UpdateCategoryRequest)
    → [조회] Category 조회 (categoryId + storeId)
    → [검증] 404 Not Found: 카테고리 미존재
    → [검증] BR-CT01, BR-CT02 동일 적용
    → [검증] BR-CT02: 이름 변경 시 동일 매장 내 중복 검사 (자기 자신 제외)
    → Category.updateName(), Category.updateDisplayOrder() 호출
    → CategoryResponse 반환
```

### 2.3 카테고리 삭제 (deleteCategory)
```
AdminMenuController.deleteCategory(storeId, categoryId)
  → [권한 검증] RBAC-07: OWNER 또는 MANAGER만 허용
  → [매장 격리] ISO-02: JWT storeId == 요청 storeId 검증
  → MenuService.deleteCategory(storeId, categoryId)
    → [조회] Category 조회 (categoryId + storeId)
    → [검증] 404 Not Found: 카테고리 미존재
    → [검증] BR-CT-DEL01: 해당 카테고리에 메뉴가 존재하면 삭제 불가 (409 Conflict)
    → Category 삭제
```

### 2.4 카테고리 목록 조회 - 관리자 (getCategories)
```
AdminMenuController.getCategories(storeId)
  → [권한 검증] OWNER, MANAGER, STAFF 모두 허용 (조회)
  → [매장 격리] ISO-02: JWT storeId 기반 필터링
  → MenuService.getCategoriesByStore(storeId)
    → CategoryRepository.findByStoreIdOrderByDisplayOrder(storeId)
    → List<CategoryResponse> 반환
```

---

## 3. 고객 메뉴 조회 플로우

### 3.1 카테고리 목록 조회 - 고객 (getCategories)
```
CustomerMenuController.getCategories(storeId)
  → [인증] 테이블 JWT 토큰 검증
  → [매장 격리] JWT storeId == Path storeId 검증
  → MenuService.getCategoriesByStore(storeId)
    → CategoryRepository.findByStoreIdOrderByDisplayOrder(storeId)
    → List<CategoryResponse> 반환
```

### 3.2 메뉴 목록 조회 - 고객 (getMenusByCategory)
```
CustomerMenuController.getMenusByCategory(storeId, categoryId)
  → [인증] 테이블 JWT 토큰 검증
  → [매장 격리] JWT storeId == Path storeId 검증
  → MenuService.getMenusByStore(storeId, categoryId)
    → categoryId != null → 카테고리별 메뉴 조회
    → categoryId == null → 전체 메뉴 조회
    → List<MenuResponse> 반환 (displayOrder 순)
```

### 3.3 메뉴 상세 조회 - 고객 (getMenuDetail)
```
CustomerMenuController.getMenuDetail(storeId, menuId)
  → [인증] 테이블 JWT 토큰 검증
  → [매장 격리] JWT storeId == Path storeId 검증
  → MenuService.getMenuDetail(storeId, menuId)
    → Menu 조회 (menuId + storeId)
    → [검증] 404 Not Found: 메뉴 미존재
    → Category 조회 (categoryId) → categoryName 포함
    → MenuDetailResponse 반환
```

---

## 4. 이미지 업로드/삭제 플로우

### 4.1 FileStorageService.uploadImage
```
uploadImage(MultipartFile image)
  → [검증] 파일 크기 제한 (최대 5MB)
  → [검증] 허용 파일 형식 (JPEG, PNG, GIF, WebP)
  → 고유 파일명 생성 (UUID + 원본 확장자)
  → S3 업로드 (menus/ 프리픽스)
  → S3 URL 반환
```

### 4.2 FileStorageService.deleteImage
```
deleteImage(String imageUrl)
  → imageUrl에서 S3 키 추출
  → S3 객체 삭제
  → 삭제 실패 시 로그 기록 (비즈니스 로직 중단하지 않음)
```

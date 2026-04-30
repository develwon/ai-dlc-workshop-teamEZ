# Logical Components - Unit 3: menu-domain

> Unit 1의 공통 컴포넌트(JwtTokenProvider, JwtAuthenticationFilter, SecurityConfig, StoreIsolationValidator, GlobalExceptionHandler, RequestIdFilter, BaseEntity)를 그대로 사용합니다.
> 여기서는 Unit 3에서 새로 구현하는 컴포넌트만 정의합니다.

---

## 1. Controller Layer

### CustomerMenuController
| 항목 | 내용 |
|------|------|
| **책임** | 고객용 메뉴/카테고리 조회 API |
| **인증** | 테이블 JWT 토큰 |
| **매장 격리** | Path의 storeId와 JWT storeId 일치 검증 |

**엔드포인트**:
| 메서드 | HTTP | 경로 | 설명 |
|--------|------|------|------|
| getCategories | GET | /api/stores/{storeId}/categories | 카테고리 목록 조회 |
| getMenusByCategory | GET | /api/stores/{storeId}/menus | 메뉴 목록 조회 (카테고리 필터) |
| getMenuDetail | GET | /api/stores/{storeId}/menus/{menuId} | 메뉴 상세 조회 |

### AdminMenuController
| 항목 | 내용 |
|------|------|
| **책임** | 관리자용 메뉴/카테고리 CRUD API |
| **인증** | 관리자 JWT 토큰 |
| **권한** | 조회: 전체 역할, CUD: OWNER/MANAGER |
| **매장 격리** | JWT storeId 기반 자동 필터링 |

**엔드포인트**:
| 메서드 | HTTP | 경로 | 권한 | 설명 |
|--------|------|------|------|------|
| getMenus | GET | /api/admin/menus | ALL | 메뉴 목록 조회 |
| createMenu | POST | /api/admin/menus | OWNER, MANAGER | 메뉴 등록 |
| updateMenu | PUT | /api/admin/menus/{menuId} | OWNER, MANAGER | 메뉴 수정 |
| deleteMenu | DELETE | /api/admin/menus/{menuId} | OWNER, MANAGER | 메뉴 삭제 |
| updateMenuOrder | PATCH | /api/admin/menus/order | OWNER, MANAGER | 메뉴 순서 변경 |
| getCategories | GET | /api/admin/categories | ALL | 카테고리 목록 조회 |
| createCategory | POST | /api/admin/categories | OWNER, MANAGER | 카테고리 등록 |
| updateCategory | PUT | /api/admin/categories/{categoryId} | OWNER, MANAGER | 카테고리 수정 |
| deleteCategory | DELETE | /api/admin/categories/{categoryId} | OWNER, MANAGER | 카테고리 삭제 |

---

## 2. Service Layer

### MenuService
| 항목 | 내용 |
|------|------|
| **책임** | 메뉴/카테고리 비즈니스 로직 |
| **의존** | MenuRepository, CategoryRepository, FileStorageService |
| **트랜잭션** | 조회: readOnly=true, CUD: 기본 트랜잭션 |

**메서드**:
| 메서드 | 입력 | 출력 | 트랜잭션 |
|--------|------|------|----------|
| getCategoriesByStore | storeId | List\<CategoryResponse\> | readOnly |
| getMenusByStore | storeId, categoryId? | List\<MenuResponse\> | readOnly |
| getMenuDetail | storeId, menuId | MenuDetailResponse | readOnly |
| createMenu | storeId, CreateMenuRequest, imageUrl? | MenuResponse | write |
| updateMenu | storeId, menuId, UpdateMenuRequest, imageUrl? | MenuResponse | write |
| deleteMenu | storeId, menuId | void | write |
| updateMenuDisplayOrder | storeId, List\<MenuOrderRequest\> | void | write |
| createCategory | storeId, CreateCategoryRequest | CategoryResponse | write |
| updateCategory | storeId, categoryId, UpdateCategoryRequest | CategoryResponse | write |
| deleteCategory | storeId, categoryId | void | write |

### FileStorageService
| 항목 | 내용 |
|------|------|
| **책임** | S3 이미지 업로드/삭제 |
| **의존** | S3Client (Unit 1 S3Config에서 Bean 제공) |
| **키 형식** | `menus/{storeId}/{UUID}.{extension}` |

**메서드**:
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| uploadImage | storeId, MultipartFile | String (imageUrl) | S3 업로드, URL 반환 |
| deleteImage | imageUrl | void | S3 삭제 (best-effort) |
| validateImageFile | MultipartFile | void | 크기/형식 검증 |

---

## 3. Repository Layer

### CategoryRepository (extends JpaRepository\<Category, Long\>)
| 메서드 | 반환 타입 | 설명 |
|--------|-----------|------|
| findByStoreIdOrderByDisplayOrder(storeId) | List\<Category\> | 매장별 카테고리 조회 |
| findByIdAndStoreId(id, storeId) | Optional\<Category\> | 매장 격리 단건 조회 |
| existsByStoreIdAndName(storeId, name) | boolean | 카테고리명 중복 검사 |
| existsByStoreIdAndNameAndIdNot(storeId, name, id) | boolean | 중복 검사 (자기 제외) |

### MenuRepository (extends JpaRepository\<Menu, Long\>)
| 메서드 | 반환 타입 | 설명 |
|--------|-----------|------|
| findByStoreIdOrderByDisplayOrder(storeId) | List\<Menu\> | 매장별 전체 메뉴 조회 |
| findByStoreIdAndCategoryIdOrderByDisplayOrder(storeId, categoryId) | List\<Menu\> | 카테고리별 메뉴 조회 |
| findByIdAndStoreId(id, storeId) | Optional\<Menu\> | 매장 격리 단건 조회 |
| existsByCategoryId(categoryId) | boolean | 카테고리 내 메뉴 존재 확인 |

---

## 4. 컴포넌트 의존성 요약

```
CustomerMenuController
    └── MenuService
         ├── CategoryRepository
         └── MenuRepository

AdminMenuController
    ├── MenuService
    │    ├── CategoryRepository
    │    ├── MenuRepository
    │    └── FileStorageService
    └── FileStorageService (이미지 업로드 직접 호출)
         └── S3Client (Unit 1 S3Config Bean)
```

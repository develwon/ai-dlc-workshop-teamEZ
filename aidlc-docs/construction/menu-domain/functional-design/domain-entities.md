# Domain Entities - Unit 3: menu-domain

---

## 1. 엔티티 관계

```
Store (1) ----< (N) Category
Category (1) ----< (N) Menu
```

> Unit 3에서 직접 관리하는 엔티티는 Category와 Menu입니다.
> Store는 Unit 1에서 정의된 엔티티로, storeId를 통해 참조합니다.

---

## 2. Category 엔티티 (Unit 1에서 정의 완료)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | Long | PK, Auto Increment | 카테고리 고유 ID |
| storeId | Long | FK → Store.id, NOT NULL | 소속 매장 |
| name | String(50) | NOT NULL | 카테고리명 |
| displayOrder | Integer | NOT NULL, DEFAULT 0 | 노출 순서 |
| createdAt | LocalDateTime | NOT NULL | 생성 시각 |
| updatedAt | LocalDateTime | NOT NULL | 수정 시각 |

**UNIQUE 제약**: (storeId, name)

**도메인 메서드**:
- `updateName(String name)` — 카테고리명 변경
- `updateDisplayOrder(Integer displayOrder)` — 노출 순서 변경

---

## 3. Menu 엔티티 (Unit 1에서 정의 완료)

| 필드 | 타입 | 제약조건 | 설명 |
|------|------|----------|------|
| id | Long | PK, Auto Increment | 메뉴 고유 ID |
| storeId | Long | FK → Store.id, NOT NULL | 소속 매장 |
| categoryId | Long | FK → Category.id, NOT NULL | 카테고리 |
| name | String(100) | NOT NULL | 메뉴명 |
| price | Integer | NOT NULL, MIN 0 | 가격 (원) |
| description | String(500) | NULLABLE | 메뉴 설명 |
| imageUrl | String(500) | NULLABLE | 이미지 URL (S3) |
| displayOrder | Integer | NOT NULL, DEFAULT 0 | 노출 순서 |
| createdAt | LocalDateTime | NOT NULL | 생성 시각 |
| updatedAt | LocalDateTime | NOT NULL | 수정 시각 |

**도메인 메서드**:
- `update(name, price, description, categoryId, imageUrl)` — 메뉴 정보 수정
- `updateDisplayOrder(Integer displayOrder)` — 노출 순서 변경

---

## 4. Repository 쿼리 메서드

### CategoryRepository
| 메서드 | 반환 타입 | 설명 |
|--------|-----------|------|
| `findByStoreIdOrderByDisplayOrder(Long storeId)` | `List<Category>` | 매장별 카테고리 조회 (순서) |
| `findByIdAndStoreId(Long id, Long storeId)` | `Optional<Category>` | 매장 격리된 카테고리 단건 조회 |
| `existsByStoreIdAndName(Long storeId, String name)` | `boolean` | 카테고리명 중복 검사 |
| `existsByStoreIdAndNameAndIdNot(Long storeId, String name, Long id)` | `boolean` | 카테고리명 중복 검사 (자기 자신 제외) |

### MenuRepository
| 메서드 | 반환 타입 | 설명 |
|--------|-----------|------|
| `findByStoreIdOrderByDisplayOrder(Long storeId)` | `List<Menu>` | 매장별 전체 메뉴 조회 |
| `findByStoreIdAndCategoryIdOrderByDisplayOrder(Long storeId, Long categoryId)` | `List<Menu>` | 카테고리별 메뉴 조회 |
| `findByIdAndStoreId(Long id, Long storeId)` | `Optional<Menu>` | 매장 격리된 메뉴 단건 조회 |
| `existsByCategoryId(Long categoryId)` | `boolean` | 카테고리에 메뉴 존재 여부 확인 |

---

## 5. DTO 매핑

### 요청 DTO (Unit 1에서 정의 완료)
| DTO | 용도 | 주요 필드 |
|-----|------|-----------|
| CreateMenuRequest | 메뉴 등록 | name, price, description, categoryId, imageUrl |
| UpdateMenuRequest | 메뉴 수정 | name, price, description, categoryId, imageUrl |
| MenuOrderRequest | 순서 변경 | menuId, displayOrder |
| CreateCategoryRequest | 카테고리 등록 | name, displayOrder |
| UpdateCategoryRequest | 카테고리 수정 | name, displayOrder |

### 응답 DTO (Unit 1에서 정의 완료)
| DTO | 용도 | 주요 필드 |
|-----|------|-----------|
| MenuResponse | 메뉴 목록 응답 | id, name, price, description, imageUrl, categoryId, displayOrder |
| MenuDetailResponse | 메뉴 상세 응답 | id, name, price, description, imageUrl, categoryId, categoryName, displayOrder |
| CategoryResponse | 카테고리 응답 | id, name, displayOrder |

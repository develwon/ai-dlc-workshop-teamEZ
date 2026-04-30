# Code Summary - Unit 3: menu-domain

## 생성된 파일 목록

### Repository Layer (2 files)
| 파일 | 경로 | 설명 |
|------|------|------|
| CategoryRepository | `src/main/java/com/tableorder/repository/CategoryRepository.java` | 카테고리 데이터 접근 (JpaRepository) |
| MenuRepository | `src/main/java/com/tableorder/repository/MenuRepository.java` | 메뉴 데이터 접근 (JpaRepository) |

### Service Layer (2 files)
| 파일 | 경로 | 설명 |
|------|------|------|
| FileStorageService | `src/main/java/com/tableorder/service/FileStorageService.java` | S3 이미지 업로드/삭제 |
| MenuService | `src/main/java/com/tableorder/service/MenuService.java` | 메뉴/카테고리 비즈니스 로직 |

### Controller Layer (2 files)
| 파일 | 경로 | 설명 |
|------|------|------|
| CustomerMenuController | `src/main/java/com/tableorder/controller/CustomerMenuController.java` | 고객용 메뉴 조회 API |
| AdminMenuController | `src/main/java/com/tableorder/controller/AdminMenuController.java` | 관리자용 메뉴/카테고리 CRUD API |

### Unit Tests (6 files)
| 파일 | 경로 | 테스트 수 |
|------|------|-----------|
| CategoryRepositoryTest | `src/test/java/com/tableorder/repository/CategoryRepositoryTest.java` | 6 |
| MenuRepositoryTest | `src/test/java/com/tableorder/repository/MenuRepositoryTest.java` | 6 |
| FileStorageServiceTest | `src/test/java/com/tableorder/service/FileStorageServiceTest.java` | 8 |
| MenuServiceTest | `src/test/java/com/tableorder/service/MenuServiceTest.java` | 14 |
| CustomerMenuControllerTest | `src/test/java/com/tableorder/controller/CustomerMenuControllerTest.java` | 5 |
| AdminMenuControllerTest | `src/test/java/com/tableorder/controller/AdminMenuControllerTest.java` | 10 |

**총 단위 테스트: 49개**

---

## API 엔드포인트 요약

### 고객 API (CustomerMenuController)
| HTTP | 경로 | 설명 |
|------|------|------|
| GET | /api/stores/{storeId}/categories | 카테고리 목록 조회 |
| GET | /api/stores/{storeId}/menus | 메뉴 목록 조회 (카테고리 필터) |
| GET | /api/stores/{storeId}/menus/{menuId} | 메뉴 상세 조회 |

### 관리자 API (AdminMenuController)
| HTTP | 경로 | 권한 | 설명 |
|------|------|------|------|
| GET | /api/admin/menus | ALL | 메뉴 목록 조회 |
| POST | /api/admin/menus | OWNER, MANAGER | 메뉴 등록 (multipart) |
| PUT | /api/admin/menus/{menuId} | OWNER, MANAGER | 메뉴 수정 (multipart) |
| DELETE | /api/admin/menus/{menuId} | OWNER, MANAGER | 메뉴 삭제 |
| PATCH | /api/admin/menus/order | OWNER, MANAGER | 메뉴 순서 변경 |
| GET | /api/admin/categories | ALL | 카테고리 목록 조회 |
| POST | /api/admin/categories | OWNER, MANAGER | 카테고리 등록 |
| PUT | /api/admin/categories/{categoryId} | OWNER, MANAGER | 카테고리 수정 |
| DELETE | /api/admin/categories/{categoryId} | OWNER, MANAGER | 카테고리 삭제 |

---

## 스토리 구현 현황
- [x] US-C02: 고객 메뉴 조회/탐색 (CustomerMenuController + MenuService)
- [x] US-A04: 관리자 메뉴 관리 (AdminMenuController + MenuService + FileStorageService)

## 빌드 참고
- Maven이 로컬에 설치되어 있지 않아 IDE 진단 도구로 검증 완료
- 모든 소스 파일 및 테스트 파일에서 컴파일 오류 없음
- 테스트 실행은 Maven 설치 후 `mvn test -pl . -Dtest="com.tableorder.repository.*,com.tableorder.service.*,com.tableorder.controller.*"` 로 실행 가능

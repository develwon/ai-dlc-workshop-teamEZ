# Tech Stack Decisions - Unit 3: menu-domain

> Unit 1 (common-foundation)에서 결정된 기술 스택을 그대로 사용합니다.
> Unit 3에서 추가로 사용하는 기술만 기술합니다.

---

## 1. Unit 1 기술 스택 (그대로 적용)

| 영역 | 기술 | 비고 |
|------|------|------|
| 언어 | Java 17 | Unit 1 결정 |
| 프레임워크 | Spring Boot 3.2.x | Unit 1 결정 |
| ORM | Spring Data JPA | Unit 1 결정 |
| 보안 | Spring Security + JWT | Unit 1 결정 |
| 검증 | Bean Validation | Unit 1 결정 |
| 테스트 | JUnit 5 + Mockito + H2 | Unit 1 결정 |
| 로깅 | SLF4J + Logback | Unit 1 결정 |

---

## 2. Unit 3 추가 기술

| 영역 | 기술 | 용도 | 비고 |
|------|------|------|------|
| 이미지 저장 | AWS S3 (aws-sdk-java-v2) | 메뉴 이미지 업로드/삭제 | Unit 1 S3Config 활용 |
| 파일 업로드 | Spring Multipart | 이미지 파일 수신 | spring-boot-starter-web 내장 |

---

## 3. Unit 3 DB 인덱스

| 테이블 | 인덱스 | 용도 |
|--------|--------|------|
| category | (store_id, name) UNIQUE | 카테고리명 중복 검사 |
| category | (store_id, display_order) | 카테고리 순서 조회 |
| menu | (store_id, category_id, display_order) | 메뉴 순서 조회 |
| menu | (store_id, display_order) | 매장별 전체 메뉴 조회 |

---

## 4. Unit 3 테스트 전략

| 테스트 유형 | 도구 | 대상 |
|------------|------|------|
| Service 단위 테스트 | JUnit 5 + Mockito | MenuService, FileStorageService |
| Repository 테스트 | @DataJpaTest + H2 | CategoryRepository, MenuRepository |
| Controller 테스트 | @WebMvcTest + MockMvc | CustomerMenuController, AdminMenuController |

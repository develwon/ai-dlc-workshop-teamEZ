# Functional Design Plan - Unit 3: menu-domain

## 유닛 컨텍스트
- **유닛명**: menu-domain
- **책임**: 메뉴/카테고리 CRUD, 조회, 이미지 업로드, 순서 관리
- **관련 스토리**: US-C02 (고객 메뉴 조회/탐색), US-A04 (관리자 메뉴 관리)
- **의존**: Unit 1 (common-foundation) - Entity, DTO, Config, Security 기반

## 질문 평가
이 유닛의 비즈니스 로직은 Application Design과 Unit 1 Business Rules에서 이미 상세하게 정의되었습니다:
- 컴포넌트 메서드 시그니처 (component-methods.md)
- 서비스 레이어 설계 (services.md)
- 엔티티 검증 규칙 (BR-CT01~03, BR-M01~06)
- RBAC 권한 규칙 (RBAC-07, RBAC-08)
- 매장 격리 규칙 (ISO-01, ISO-02)

추가 질문 없이 기존 설계를 기반으로 Functional Design을 진행합니다.

## 실행 계획

### Phase A: 비즈니스 로직 모델
- [x] Step A1: 메뉴 CRUD 비즈니스 플로우 정의 (생성, 수정, 삭제, 조회)
- [x] Step A2: 카테고리 CRUD 비즈니스 플로우 정의
- [x] Step A3: 메뉴/카테고리 노출 순서 관리 로직 정의
- [x] Step A4: 이미지 업로드/삭제 플로우 정의
- [x] Step A5: 고객 메뉴 조회 플로우 정의 (카테고리 필터링 포함)
- [x] Step A6: business-logic-model.md 생성

### Phase B: 비즈니스 규칙 정의
- [x] Step B1: 메뉴 도메인 검증 규칙 상세화 (Unit 1 BR-M, BR-CT 확장)
- [x] Step B2: 카테고리 삭제 시 메뉴 존재 여부 검증 규칙
- [x] Step B3: 매장 격리 규칙 적용 (storeId 기반 필터링)
- [x] Step B4: 권한 규칙 적용 (RBAC-07, RBAC-08)
- [x] Step B5: business-rules.md 생성

### Phase C: 도메인 엔티티 상세
- [x] Step C1: Category, Menu 엔티티 관계 및 제약조건 상세화
- [x] Step C2: Repository 쿼리 메서드 정의
- [x] Step C3: domain-entities.md 생성

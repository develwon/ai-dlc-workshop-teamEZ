# Code Summary - Unit 2: auth-domain

## 생성된 파일

### Repository (4개)
- `src/main/java/com/tableorder/repository/StoreRepository.java`
- `src/main/java/com/tableorder/repository/AdminRepository.java`
- `src/main/java/com/tableorder/repository/StoreTableRepository.java`
- `src/main/java/com/tableorder/repository/TableSessionRepository.java`

### Service (2개)
- `src/main/java/com/tableorder/service/TableAuthService.java` - 테이블 인증
- `src/main/java/com/tableorder/service/AdminAuthService.java` - 관리자 인증 (잠금 포함)

### Controller (2개)
- `src/main/java/com/tableorder/controller/customer/TableAuthController.java` - POST /api/tables/login
- `src/main/java/com/tableorder/controller/admin/AdminAuthController.java` - POST /api/admin/login

### 단위 테스트 (4개, 14 test methods)
- `TableAuthServiceTest.java` (5 tests)
- `AdminAuthServiceTest.java` (5 tests)
- `TableAuthControllerTest.java` (2 tests)
- `AdminAuthControllerTest.java` (2 tests)

### 설계 문서 (3개)
- `business-logic-model.md` - 인증 플로우
- `nfr-requirements.md` - 보안/성능 요구사항
- `nfr-design-patterns.md` - 인증 실패 응답, 계정 잠금 패턴

## 관련 스토리
- US-C01: 테이블 태블릿 자동 인증 ✅
- US-A01: 관리자 매장 인증 ✅

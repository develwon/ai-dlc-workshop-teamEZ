# Code Generation Plan - Unit 2: auth-domain

## 유닛 컨텍스트
- **유닛명**: auth-domain
- **책임**: 테이블 인증, 관리자 인증, JWT 발급
- **관련 스토리**: US-C01 (테이블 자동 인증), US-A01 (관리자 인증)

## 실행 계획

### Step 1: Repository 생성
- [x] StoreRepository.java
- [x] AdminRepository.java
- [x] StoreTableRepository.java
- [x] TableSessionRepository.java

### Step 2: Service 생성
- [x] TableAuthService.java
- [x] AdminAuthService.java

### Step 3: Controller 생성
- [x] TableAuthController.java
- [x] AdminAuthController.java

### Step 4: 단위 테스트
- [x] TableAuthServiceTest.java
- [x] AdminAuthServiceTest.java
- [x] TableAuthControllerTest.java
- [x] AdminAuthControllerTest.java

### Step 5: 코드 요약 문서
- [x] code-summary.md

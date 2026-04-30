# 테이블오더 서비스 - 서비스 레이어 설계

---

## 1. 서비스 정의 및 책임

### TableAuthService
- **책임**: 테이블 태블릿 인증 처리
- **주요 로직**: 매장 코드 + 테이블 번호 + 비밀번호 검증, 테이블 전용 토큰 발급
- **의존**: StoreRepository, StoreTableRepository, JwtTokenProvider

### AdminAuthService
- **책임**: 관리자 인증 및 권한 관리
- **주요 로직**: 매장 코드 + 사용자명 + 비밀번호 검증, JWT 발급 (역할 포함), 로그인 시도 제한
- **의존**: StoreRepository, AdminRepository, JwtTokenProvider, PasswordEncoder

### MenuService
- **책임**: 메뉴 및 카테고리 CRUD, 조회
- **주요 로직**: 메뉴 등록/수정/삭제, 카테고리 관리, 노출 순서 관리, 입력 검증
- **의존**: MenuRepository, CategoryRepository, FileStorageService

### OrderService
- **책임**: 주문 생성, 조회, 상태 관리, 삭제
- **주요 로직**: 주문 생성 (메뉴 유효성 검증, 금액 계산), 상태 전이 (대기중→준비중→완료), 주문 삭제
- **의존**: OrderRepository, OrderItemRepository, MenuRepository, TableSessionService, OrderEventService

### TableSessionService
- **책임**: 테이블 세션 라이프사이클 관리
- **주요 로직**: 세션 시작 (첫 주문 시), 세션 종료 (이용 완료), 활성 세션 조회
- **의존**: TableSessionRepository, OrderHistoryService

### OrderEventService
- **책임**: SSE 기반 실시간 주문 이벤트 관리
- **주요 로직**: SSE emitter 등록/제거, 매장별 이벤트 브로드캐스트, 연결 타임아웃 관리
- **의존**: (독립적 - 인메모리 emitter 관리)

### OrderHistoryService
- **책임**: 과거 주문 이력 관리
- **주요 로직**: 세션 종료 시 주문 이력 아카이빙, 과거 이력 조회 (날짜 필터)
- **의존**: OrderHistoryRepository, OrderRepository, OrderItemRepository

### FileStorageService
- **책임**: 클라우드 스토리지 파일 관리
- **주요 로직**: S3 이미지 업로드, 이미지 삭제, URL 생성
- **의존**: S3Client (AWS SDK)

---

## 2. 서비스 간 상호작용 패턴

### 주문 생성 플로우
```
OrderController.createOrder()
  → OrderService.createOrder()
    → TableSessionService.getOrCreateSession()  // 활성 세션 확인/생성
    → MenuRepository.findById()                  // 메뉴 유효성 검증
    → OrderRepository.save()                     // 주문 저장
    → OrderItemRepository.saveAll()              // 주문 항목 저장
    → OrderEventService.publishOrderEvent()      // SSE 이벤트 발행
```

### 테이블 이용 완료 플로우
```
AdminTableController.completeTable()
  → TableSessionService.completeSession()
    → TableSessionRepository.findByTableIdAndEndTimeIsNull()  // 활성 세션 조회
    → OrderHistoryService.archiveSessionOrders()               // 주문 이력 아카이빙
    → TableSessionRepository.save()                            // 세션 종료 시각 기록
```

### 실시간 주문 모니터링 플로우
```
AdminOrderController.streamOrders()
  → OrderEventService.subscribe()  // SSE emitter 등록

[주문 생성 시]
OrderService.createOrder()
  → OrderEventService.publishOrderEvent()  // 모든 구독자에게 브로드캐스트
```

### 메뉴 등록 (이미지 포함) 플로우
```
AdminMenuController.createMenu()
  → FileStorageService.uploadImage()  // S3 이미지 업로드
  → MenuService.createMenu()          // 메뉴 저장 (이미지 URL 포함)
```

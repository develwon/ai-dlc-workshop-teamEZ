# 테이블오더 서비스 - Unit of Work 의존성

---

## 1. 의존성 매트릭스

| 유닛 | 의존 대상 | 의존 유형 |
|------|-----------|-----------|
| **Unit 1: common-foundation** | (없음) | 기반 유닛 |
| **Unit 2: auth-domain** | Unit 1 | Entity, Security, JWT, Repository |
| **Unit 3: menu-domain** | Unit 1 | Entity, Repository, S3Config |
| **Unit 4: order-domain** | Unit 1, Unit 2 | Entity, Repository, Security, SSE, TableSession |
| **Unit 5: table-management-domain** | Unit 1, Unit 4 | Entity, Repository, OrderHistory, TableSession |
| **Unit 6: customer-frontend** | Unit 2, Unit 3, Unit 4 | REST API (인증, 메뉴, 주문) |
| **Unit 7: admin-frontend** | Unit 2, Unit 3, Unit 4, Unit 5 | REST API (인증, 메뉴, 주문, 테이블 관리) |

---

## 2. 의존성 다이어그램

```
+---------------------+
| Unit 1:             |
| common-foundation   |
+---------------------+
    |       |       |
    v       v       v
+-------+ +-------+ +-------+
|Unit 2:| |Unit 3:| |       |
| auth  | | menu  | |       |
+-------+ +-------+ |       |
    |       |        |       |
    v       v        v       |
    +-------+--------+       |
    |                         |
    v                         |
+---------------------+      |
| Unit 4:             |      |
| order-domain        |------+
+---------------------+
    |
    v
+---------------------+
| Unit 5:             |
| table-management    |
+---------------------+
    |           |
    v           v
+----------+ +----------+
| Unit 6:  | | Unit 7:  |
| customer | | admin    |
| frontend | | frontend |
+----------+ +----------+
```

---

## 3. 공유 컴포넌트

| 공유 컴포넌트 | 정의 유닛 | 사용 유닛 |
|---------------|-----------|-----------|
| JPA Entities | Unit 1 | Unit 2, 3, 4, 5 |
| DTOs | Unit 1 | Unit 2, 3, 4, 5 |
| JwtTokenProvider | Unit 1 | Unit 2 |
| SecurityConfig | Unit 1 | Unit 2, 3, 4, 5 |
| SseEmitterManager | Unit 1 | Unit 4 |
| S3Config | Unit 1 | Unit 3 |
| GlobalExceptionHandler | Unit 1 | Unit 2, 3, 4, 5 |
| TableSessionService | Unit 4 | Unit 5 |

---

## 4. 구현 순서 제약 (4명 병렬 최적화)

| Sprint | 유닛 | 담당자 | 선행 조건 | 병렬 |
|--------|------|--------|-----------|------|
| Sprint 1 | common-foundation | 개발자 A (단독) | 없음 | B,C,D는 설계 리뷰/API 문서/테스트 설계 |
| Sprint 2 | auth-domain | 개발자 A | Sprint 1 완료 | 4명 병렬 |
| Sprint 2 | menu-domain | 개발자 B | Sprint 1 완료 | 4명 병렬 |
| Sprint 2 | order-domain | 개발자 C | Sprint 1 완료 | 4명 병렬 |
| Sprint 2 | table-management-domain | 개발자 D | Sprint 1 완료 | 4명 병렬 |
| Sprint 3 | customer-frontend | 개발자 A | Sprint 2 완료 | 4명 병렬 |
| Sprint 3 | admin-frontend | 개발자 B | Sprint 2 완료 | 4명 병렬 |
| Sprint 3 | 백엔드 통합 테스트 | 개발자 C,D | Sprint 2 완료 | 4명 병렬 |

**병렬화 핵심 전략**:
- Sprint 2에서 Unit 4(주문)와 Unit 5(테이블 관리)는 공유 인터페이스(TableSessionService)를 Sprint 2 초기에 확정
- 각 도메인 유닛은 인증 통합 없이 독립 개발 (Security 설정은 Unit 1에서 완료)
- Sprint 3에서 크로스 도메인 통합 테스트 수행

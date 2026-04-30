# 테이블오더 서비스 - Unit of Work ↔ Story 매핑

---

## 1. 유닛별 스토리 매핑

### Unit 1: common-foundation
| 스토리 | 관련도 | 설명 |
|--------|--------|------|
| (전체) | 기반 | 모든 스토리의 기반 인프라 (Entity, Security, Config) |

### Unit 2: auth-domain
| 스토리 | 관련도 | 설명 |
|--------|--------|------|
| US-C01: 테이블 자동 인증 | 직접 | 테이블 태블릿 로그인 API |
| US-A01: 관리자 인증 | 직접 | 관리자 로그인/로그아웃 API, RBAC |

### Unit 3: menu-domain
| 스토리 | 관련도 | 설명 |
|--------|--------|------|
| US-C02: 메뉴 조회/탐색 | 직접 | 고객용 메뉴/카테고리 조회 API |
| US-A04: 메뉴 관리 | 직접 | 관리자 메뉴 CRUD, 이미지 업로드, 순서 관리 API |

### Unit 4: order-domain
| 스토리 | 관련도 | 설명 |
|--------|--------|------|
| US-C04: 주문 생성 | 직접 | 주문 생성 API, 세션 관리 |
| US-C05: 주문 내역 조회 | 직접 | 현재 세션 주문 조회 API |
| US-A02: 실시간 주문 모니터링 | 직접 | SSE 스트림, 주문 상태 변경, 주문 조회 API |

### Unit 5: table-management-domain
| 스토리 | 관련도 | 설명 |
|--------|--------|------|
| US-A03: 테이블 관리 | 직접 | 테이블 설정, 주문 삭제, 이용 완료, 과거 내역 API |

### Unit 6: customer-frontend
| 스토리 | 관련도 | 설명 |
|--------|--------|------|
| US-C01: 테이블 자동 인증 | 직접 | 자동 로그인 UI, localStorage 관리 |
| US-C02: 메뉴 조회/탐색 | 직접 | 메뉴 카드 UI, 카테고리 네비게이션 |
| US-C03: 장바구니 관리 | 직접 | 장바구니 UI (클라이언트 전용, localStorage) |
| US-C04: 주문 생성 | 직접 | 주문 확정 UI, 성공/실패 처리 |
| US-C05: 주문 내역 조회 | 직접 | 주문 내역 목록 UI |

### Unit 7: admin-frontend
| 스토리 | 관련도 | 설명 |
|--------|--------|------|
| US-A01: 관리자 인증 | 직접 | 로그인 UI, JWT 관리, 역할 기반 UI 분기 |
| US-A02: 실시간 주문 모니터링 | 직접 | SSE 대시보드 UI, 그리드 레이아웃, 주문 상태 변경 |
| US-A03: 테이블 관리 | 직접 | 테이블 설정/이용 완료/과거 내역 UI |
| US-A04: 메뉴 관리 | 직접 | 메뉴 CRUD UI, 이미지 업로드, 순서 변경 |

---

## 2. 스토리별 유닛 매핑

| 스토리 | 백엔드 유닛 | 프론트엔드 유닛 |
|--------|-------------|-----------------|
| US-C01: 테이블 자동 인증 | Unit 2 (auth-domain) | Unit 6 (customer-frontend) |
| US-C02: 메뉴 조회/탐색 | Unit 3 (menu-domain) | Unit 6 (customer-frontend) |
| US-C03: 장바구니 관리 | (없음 - 클라이언트 전용) | Unit 6 (customer-frontend) |
| US-C04: 주문 생성 | Unit 4 (order-domain) | Unit 6 (customer-frontend) |
| US-C05: 주문 내역 조회 | Unit 4 (order-domain) | Unit 6 (customer-frontend) |
| US-A01: 관리자 인증 | Unit 2 (auth-domain) | Unit 7 (admin-frontend) |
| US-A02: 실시간 주문 모니터링 | Unit 4 (order-domain) | Unit 7 (admin-frontend) |
| US-A03: 테이블 관리 | Unit 5 (table-management) | Unit 7 (admin-frontend) |
| US-A04: 메뉴 관리 | Unit 3 (menu-domain) | Unit 7 (admin-frontend) |

---

## 3. 커버리지 검증

| 검증 항목 | 결과 |
|-----------|------|
| 모든 스토리가 유닛에 할당됨 | ✅ 9/9 스토리 할당 완료 |
| 모든 유닛에 최소 1개 스토리 매핑 | ✅ (Unit 1은 기반 유닛으로 전체 지원) |
| 프론트엔드 유닛이 모든 UI 스토리 커버 | ✅ 고객 5개 + 관리자 4개 |
| 백엔드 유닛이 모든 API 스토리 커버 | ✅ 8개 (US-C03 제외 - 클라이언트 전용) |

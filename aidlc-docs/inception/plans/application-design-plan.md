# Application Design Plan - 테이블오더 서비스

## 개요
테이블오더 서비스의 고수준 컴포넌트 식별, 서비스 레이어 설계, 컴포넌트 간 의존성을 정의합니다.

---

## Part 1: 설계 결정 질문

### Question 1
Spring Boot 백엔드의 아키텍처 패턴을 어떻게 구성하시겠습니까?

A) 전통적 계층형 아키텍처 (Controller → Service → Repository) - 단순하고 직관적
B) 헥사고날 아키텍처 (Ports & Adapters) - 도메인 중심, 테스트 용이
C) DDD (Domain-Driven Design) 기반 - 도메인 모델 중심, 복잡한 비즈니스 로직에 적합
D) Other (please describe after [Answer]: tag below)

[Answer]: A) 전통적 계층형 아키텍처 (Controller → Service → Repository) - 단순하고 직관적

### Question 2
프론트엔드(바닐라 JS)의 코드 구조를 어떻게 구성하시겠습니까?

A) 페이지 기반 구조 (각 페이지별 HTML/JS/CSS 파일 분리) - 단순하고 직관적
B) 모듈 기반 구조 (기능별 JS 모듈로 분리, ES Modules 사용) - 재사용성 높음
C) MVC 패턴 적용 (Model-View-Controller를 바닐라 JS로 구현) - 구조적이지만 복잡
D) Other (please describe after [Answer]: tag below)

[Answer]: B) 모듈 기반 구조 (기능별 JS 모듈로 분리, ES Modules 사용) - 재사용성 높음

### Question 3
고객용 UI와 관리자용 UI를 어떻게 분리하시겠습니까?

A) 완전 분리: 별도의 HTML 진입점 (customer/index.html, admin/index.html)으로 독립 운영
B) 단일 진입점: 하나의 SPA에서 라우팅으로 분리 (역할에 따라 다른 화면 표시)
C) 별도 서브도메인/경로: Spring Boot에서 /customer/**, /admin/** 경로로 분리
D) Other (please describe after [Answer]: tag below)

[Answer]: A) 완전 분리: 별도의 HTML 진입점 (customer/index.html, admin/index.html)으로 독립 운영

### Question 4
Spring Boot에서 정적 리소스(프론트엔드 파일)를 어떻게 서빙하시겠습니까?

A) Spring Boot 내장 정적 리소스 서빙 (src/main/resources/static/) - 단일 배포
B) 별도 웹 서버 (Nginx 등)에서 프론트엔드 서빙, Spring Boot는 API만 담당 - 분리 배포
C) Other (please describe after [Answer]: tag below)

[Answer]: A) Spring Boot 내장 정적 리소스 서빙 (src/main/resources/static/) - 단일 배포

---

## Part 2: 설계 산출물 생성 계획

### Phase A: 컴포넌트 식별
- [x] Step A1: 백엔드 컴포넌트 식별 (Controller, Service, Repository 레이어)
- [x] Step A2: 프론트엔드 컴포넌트 식별 (고객 UI, 관리자 UI)
- [x] Step A3: 공통/인프라 컴포넌트 식별 (인증, 보안, SSE, S3)
- [x] Step A4: components.md 생성

### Phase B: 컴포넌트 메서드 정의
- [x] Step B1: Controller 메서드 시그니처 정의
- [x] Step B2: Service 메서드 시그니처 정의
- [x] Step B3: Repository 메서드 시그니처 정의
- [x] Step B4: component-methods.md 생성

### Phase C: 서비스 레이어 설계
- [x] Step C1: 서비스 정의 및 책임 할당
- [x] Step C2: 서비스 간 상호작용 패턴 정의
- [x] Step C3: services.md 생성

### Phase D: 의존성 관계 정의
- [x] Step D1: 컴포넌트 간 의존성 매트릭스 작성
- [x] Step D2: 데이터 흐름 다이어그램 작성
- [x] Step D3: component-dependency.md 생성

### Phase E: 통합 문서
- [x] Step E1: application-design.md 통합 문서 생성

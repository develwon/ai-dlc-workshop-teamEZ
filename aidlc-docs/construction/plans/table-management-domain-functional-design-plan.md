# Functional Design Plan - Unit 5: table-management-domain

## 유닛 개요
- **유닛명**: table-management-domain
- **유형**: 백엔드 - 기능 도메인
- **책임**: 테이블 설정, 이용 완료, 과거 주문 이력 관리
- **관련 스토리**: US-A03 (테이블 관리)
- **의존**: Unit 1 (common-foundation), Unit 4 (order-domain - TableSessionService 인터페이스)

---

## 실행 계획

### Part 1: 비즈니스 로직 분석
- [x] Step 1.1: AdminTableController 엔드포인트 비즈니스 로직 상세화
- [x] Step 1.2: TableSessionService 확장 로직 (이용 완료) 상세화
- [x] Step 1.3: OrderHistoryService 아카이빙 로직 상세화

### Part 2: 비즈니스 규칙 정의
- [x] Step 2.1: 테이블 설정 비즈니스 규칙
- [x] Step 2.2: 이용 완료 처리 비즈니스 규칙
- [x] Step 2.3: 과거 주문 이력 조회 비즈니스 규칙
- [x] Step 2.4: 주문 삭제 비즈니스 규칙

### Part 3: 도메인 엔티티 상세화
- [x] Step 3.1: Unit 5 관련 엔티티 상호작용 정의
- [x] Step 3.2: OrderHistory 아카이빙 데이터 변환 규칙

### Part 4: 산출물 생성
- [x] Step 4.1: business-logic-model.md 생성
- [x] Step 4.2: business-rules.md 생성
- [x] Step 4.3: domain-entities.md 생성

---

## 질문

아래 질문에 답변해주세요. 각 질문의 `[Answer]:` 태그 뒤에 선택지 문자를 입력해주세요.

### Question 1
테이블 이용 완료 시, 해당 세션의 모든 주문 상태가 COMPLETED가 아닌 경우(예: PENDING, PREPARING) 어떻게 처리해야 하나요?

A) 이용 완료를 차단하고 "미완료 주문이 있습니다" 에러 반환
B) 미완료 주문을 자동으로 COMPLETED로 변경 후 이용 완료 처리
C) 주문 상태와 관계없이 이용 완료 처리 (현재 상태 그대로 아카이빙)
D) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 2
테이블 이용 완료 시, 해당 세션에 주문이 하나도 없는 경우 어떻게 처리해야 하나요?

A) 이용 완료를 차단하고 "주문 내역이 없습니다" 에러 반환
B) 주문 없이도 세션만 종료 (OrderHistory 아카이빙 없이 세션 endTime만 설정)
C) 활성 세션이 없으므로 이용 완료 자체가 불가 (세션은 첫 주문 시 생성되므로)
D) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 3
주문 삭제(직권 수정) 시, 삭제된 주문의 처리 방식은 어떻게 해야 하나요?

A) 물리적 삭제 (DB에서 완전 제거)
B) 논리적 삭제 (삭제 플래그 설정, 조회에서 제외)
C) 삭제 후 별도 삭제 이력 테이블에 기록
D) Other (please describe after [Answer]: tag below)

[Answer]: B

### Question 4
과거 주문 이력 조회 시, 날짜 필터링의 기준은 무엇으로 해야 하나요?

A) 원래 주문 시각 (orderedAt) 기준
B) 이용 완료 시각 (completedAt) 기준
C) 두 가지 모두 지원 (파라미터로 선택)
D) Other (please describe after [Answer]: tag below)

[Answer]: C

### Question 5
테이블 설정(createTable) 시, 이미 존재하는 테이블 번호로 요청이 오면 어떻게 처리해야 하나요?

A) 409 Conflict 에러 반환
B) 기존 테이블 정보를 업데이트 (비밀번호 변경)
C) 기존 테이블을 삭제하고 새로 생성
D) Other (please describe after [Answer]: tag below)

[Answer]: B

### Question 6
테이블 목록 조회(getTables) 시, 각 테이블의 현재 상태 정보(활성 세션 여부, 현재 주문 수, 총 주문액)를 함께 반환해야 하나요?

A) 테이블 기본 정보만 반환 (번호, ID)
B) 활성 세션 여부만 추가로 반환
C) 활성 세션 여부 + 현재 주문 수 + 총 주문액 모두 반환
D) Other (please describe after [Answer]: tag below)

[Answer]: B

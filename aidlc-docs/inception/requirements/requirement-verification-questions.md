# 테이블오더 서비스 - 요구사항 명확화 질문

요구사항 문서를 분석한 결과, 아래 질문들에 대한 답변이 필요합니다.
각 질문의 `[Answer]:` 태그 뒤에 선택한 옵션의 알파벳을 입력해 주세요.
제공된 옵션 중 해당하는 것이 없으면 마지막 옵션(Other)을 선택하고 설명을 추가해 주세요.

---

## Question 1
백엔드 기술 스택으로 어떤 언어/프레임워크를 사용하시겠습니까?

A) Java + Spring Boot
B) TypeScript + Node.js (Express/NestJS)
C) Python + FastAPI/Django
D) Go + Gin/Echo
E) Other (please describe after [Answer]: tag below)

[Answer]: A) Java + Spring Boot (당사 프레임워크와 동일한 환경)

## Question 2
프론트엔드 기술 스택으로 어떤 프레임워크를 사용하시겠습니까?

A) React (TypeScript)
B) Vue.js (TypeScript)
C) Next.js (React 기반 풀스택)
D) Angular
E) Other (please describe after [Answer]: tag below)

[Answer]: E) Other (HTML, JAVASCRIPT, CSS)

## Question 3
데이터베이스로 어떤 기술을 사용하시겠습니까?

A) PostgreSQL (관계형)
B) MySQL/MariaDB (관계형)
C) MongoDB (NoSQL Document)
D) SQLite (경량 관계형, 개발/소규모 매장용)
E) Other (please describe after [Answer]: tag below)

[Answer]: A) PostgreSQL (관계형)

## Question 4
프로젝트 구조를 어떻게 구성하시겠습니까?

A) 모노레포 (프론트엔드 + 백엔드를 하나의 저장소에서 관리)
B) 분리된 저장소 (프론트엔드와 백엔드를 별도 프로젝트로 관리)
C) Other (please describe after [Answer]: tag below)

[Answer]: A) 모노레포 (프론트엔드 + 백엔드를 하나의 저장소에서 관리)

## Question 5
매장(Store) 관리 범위는 어떻게 되나요? 이 시스템은 단일 매장용인가요, 다중 매장용인가요?

A) 단일 매장 전용 (하나의 매장만 관리)
B) 다중 매장 지원 (여러 매장을 하나의 시스템에서 관리, 각 매장은 독립적으로 운영)
C) Other (please describe after [Answer]: tag below)

[Answer]: B) 다중 매장 지원 (여러 매장을 하나의 시스템에서 관리, 각 매장은 독립적으로 운영)

## Question 6
관리자 계정 관리 방식은 어떻게 되나요?

A) 매장당 1개의 관리자 계정 (매장 식별자 + 비밀번호)
B) 매장당 다수의 관리자 계정 (사용자명 + 비밀번호, 역할 구분 없음)
C) 매장당 다수의 관리자 계정 (역할 기반: 매장주, 매니저, 직원 등)
D) Other (please describe after [Answer]: tag below)

[Answer]: C) 매장당 다수의 관리자 계정 (역할 기반: 매장주, 매니저, 직원 등)

## Question 7
메뉴 이미지 관리 방식은 어떻게 하시겠습니까?

A) 외부 URL 입력만 지원 (이미지 호스팅은 별도 서비스 사용)
B) 서버에 직접 이미지 업로드 지원 (로컬 파일 시스템 저장)
C) 클라우드 스토리지 업로드 지원 (AWS S3, GCS 등)
D) Other (please describe after [Answer]: tag below)

[Answer]: C) 클라우드 스토리지 업로드 지원 (AWS S3, GCS 등)

## Question 8
테이블 수는 매장당 최대 몇 개 정도를 예상하시나요? (성능 설계 기준)

A) 소규모: 1~10개
B) 중규모: 11~30개
C) 대규모: 31~50개
D) 초대규모: 50개 이상
E) Other (please describe after [Answer]: tag below)

[Answer]: B) 중규모: 11~30개

## Question 9
동시 주문 처리량은 어느 정도를 예상하시나요? (피크 시간 기준)

A) 낮음: 분당 10건 이하
B) 보통: 분당 10~50건
C) 높음: 분당 50~100건
D) 매우 높음: 분당 100건 이상
E) Other (please describe after [Answer]: tag below)

[Answer]: B) 보통: 분당 10~50건

## Question 10
배포 환경은 어디를 대상으로 하시나요?

A) 로컬 서버 (매장 내 서버에서 직접 운영)
B) 클라우드 (AWS, GCP, Azure 등)
C) 컨테이너 기반 (Docker/Kubernetes)
D) 현재는 로컬 개발 환경만 고려 (배포는 추후 결정)
E) Other (please describe after [Answer]: tag below)

[Answer]: B) 클라우드 (AWS, GCP, Azure 등)

## Question 11
메뉴 관리 기능은 MVP에 포함하시겠습니까? (요구사항 문서의 3.2.4에 정의되어 있으나 MVP 범위(섹션 4)에는 명시되지 않았습니다)

A) MVP에 포함 (메뉴 CRUD 기능 구현)
B) MVP에서 제외 (초기 데이터는 시드 데이터로 제공, 추후 구현)
C) Other (please describe after [Answer]: tag below)

[Answer]: A) MVP에 포함 (메뉴 CRUD 기능 구현)

## Question 12: Security Extensions
이 프로젝트에 보안 확장 규칙(SECURITY rules)을 적용하시겠습니까?

A) Yes — 모든 SECURITY 규칙을 blocking constraint로 적용 (프로덕션 수준 애플리케이션에 권장)
B) No — 모든 SECURITY 규칙 건너뛰기 (PoC, 프로토타입, 실험적 프로젝트에 적합)
C) Other (please describe after [Answer]: tag below)

[Answer]: A) Yes — 모든 SECURITY 규칙을 blocking constraint로 적용 (프로덕션 수준 애플리케이션에 권장)

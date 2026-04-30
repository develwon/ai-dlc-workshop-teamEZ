# Tech Stack Decisions - Unit 1: common-foundation

---

## 1. 핵심 기술 스택

| 영역 | 기술 | 버전 | 근거 |
|------|------|------|------|
| **언어** | Java | 17 (LTS) | 장기 지원, 당사 표준 |
| **프레임워크** | Spring Boot | 3.2.x | 최신 안정 버전, Spring 6 기반 |
| **빌드 도구** | Maven | 3.9.x | 당사 표준, 의존성 관리 |
| **DB** | PostgreSQL | 15.x | 관계형, 안정성, JSON 지원 |
| **ORM** | Spring Data JPA + Hibernate | Spring Boot 내장 | 표준 JPA 구현 |
| **보안** | Spring Security | Spring Boot 내장 | JWT 인증, RBAC |
| **프론트엔드** | HTML5 + JavaScript (ES Modules) + CSS3 | - | 바닐라, 프레임워크 미사용 |
| **이미지 저장** | AWS S3 | - | 클라우드 스토리지 |

---

## 2. 주요 라이브러리

| 라이브러리 | 용도 | 버전 |
|-----------|------|------|
| **spring-boot-starter-web** | REST API, 내장 Tomcat | 3.2.x |
| **spring-boot-starter-data-jpa** | JPA/Hibernate | 3.2.x |
| **spring-boot-starter-security** | Spring Security | 3.2.x |
| **spring-boot-starter-validation** | Bean Validation | 3.2.x |
| **spring-boot-starter-actuator** | 헬스 체크, 모니터링 | 3.2.x |
| **postgresql** | PostgreSQL JDBC 드라이버 | 42.7.x |
| **jjwt (io.jsonwebtoken)** | JWT 생성/검증 | 0.12.x |
| **spring-cloud-starter-aws** 또는 **aws-sdk-java-v2 (s3)** | AWS S3 연동 | 최신 안정 |
| **lombok** | 보일러플레이트 코드 감소 | 1.18.x |
| **spring-boot-starter-test** | 테스트 (JUnit 5, Mockito) | 3.2.x |
| **h2** | 테스트용 인메모리 DB | 2.2.x (test scope) |

---

## 3. 프로젝트 설정

### application.yml 주요 설정
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tableorder
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      minimum-idle: 10
      maximum-pool-size: 30
      connection-timeout: 30000
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        default_batch_fetch_size: 100
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

jwt:
  secret: ${JWT_SECRET}
  expiration: 57600000  # 16시간 (밀리초)

aws:
  s3:
    bucket: ${S3_BUCKET_NAME}
    region: ${AWS_REGION}

logging:
  level:
    root: INFO
    com.tableorder: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] [%X{requestId}] %-5level %logger{36} - %msg%n"
```

### 프로파일 전략
| 프로파일 | 용도 | DB | 특이사항 |
|----------|------|-----|----------|
| **default/local** | 로컬 개발 | PostgreSQL (로컬) | ddl-auto: update |
| **test** | 테스트 | H2 (인메모리) | ddl-auto: create-drop |
| **prod** | 프로덕션 | PostgreSQL (클라우드) | ddl-auto: validate |

---

## 4. DB 인덱스 전략

| 테이블 | 인덱스 | 용도 |
|--------|--------|------|
| admin | (store_id, username) UNIQUE | 관리자 로그인 조회 |
| store_table | (store_id, table_number) UNIQUE | 테이블 조회 |
| table_session | (table_id, end_time) | 활성 세션 조회 |
| category | (store_id, display_order) | 카테고리 순서 조회 |
| menu | (store_id, category_id, display_order) | 메뉴 순서 조회 |
| orders | (session_id, created_at) | 세션별 주문 조회 |
| orders | (store_id, status, created_at) | 매장별 활성 주문 조회 |
| order_item | (order_id) | 주문별 항목 조회 |
| order_history | (table_id, completed_at) | 테이블별 이력 조회 |

---

## 5. 테스트 전략

| 테스트 유형 | 도구 | 대상 |
|------------|------|------|
| 단위 테스트 | JUnit 5 + Mockito | Service 레이어 |
| Repository 테스트 | @DataJpaTest + H2 | Repository 레이어 |
| Controller 테스트 | @WebMvcTest + MockMvc | Controller 레이어 |
| 통합 테스트 | @SpringBootTest | 전체 플로우 |

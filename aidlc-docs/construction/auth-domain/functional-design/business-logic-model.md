# Business Logic Model - Unit 2: auth-domain

---

## 1. 테이블 태블릿 인증 플로우

```
테이블 로그인 요청 (storeCode, tableNumber, password)
    |
    v
[매장 코드로 Store 조회] --없음--> 401 Unauthorized
    |
    |있음
    v
[storeId + tableNumber로 StoreTable 조회] --없음--> 401 Unauthorized
    |
    |있음
    v
[bcrypt 비밀번호 검증] --불일치--> 401 Unauthorized
    |
    |일치
    v
[활성 세션 조회 (endTime IS NULL)]
    |
    +-- 있음 --> sessionId 포함
    +-- 없음 --> sessionId = null
    |
    v
[JWT 테이블 토큰 발급 (tableId, storeId, type=TABLE)]
    |
    v
TableLoginResponse (token, tableId, storeId, sessionId)
```

---

## 2. 관리자 인증 플로우

```
관리자 로그인 요청 (storeCode, username, password)
    |
    v
[매장 코드로 Store 조회] --없음--> 401 Unauthorized
    |
    |있음
    v
[storeId + username으로 Admin 조회] --없음--> 401 Unauthorized
    |
    |있음
    v
[계정 잠금 확인 (isLocked())] --잠금중--> 423 Account Locked
    |
    |정상
    v
[bcrypt 비밀번호 검증]
    |
    +--불일치--> loginAttempts++
    |            |
    |            +-- >= 5회 --> lockAccount(30분), 423 Account Locked
    |            +-- < 5회 --> 401 Unauthorized
    |
    +--일치--> resetLoginAttempts()
              |
              v
    [JWT 관리자 토큰 발급 (adminId, storeId, role, type=ADMIN)]
              |
              v
    AdminLoginResponse (token, role, storeId, storeName)
```

---

## 3. 비즈니스 규칙 요약

| 규칙 | 설명 |
|------|------|
| 테이블 인증은 storeCode + tableNumber + password 조합 | 3가지 모두 일치해야 인증 성공 |
| 관리자 인증은 storeCode + username + password 조합 | 3가지 모두 일치해야 인증 성공 |
| 로그인 실패 5회 시 30분 계정 잠금 | Admin 엔티티의 loginAttempts, lockedUntil 필드 사용 |
| 잠금 시간 경과 후 자동 해제 | isLocked() 메서드에서 현재 시각과 비교 |
| 인증 성공 시 loginAttempts 초기화 | resetLoginAttempts() 호출 |
| JWT에 역할(role) 포함 | 관리자 토큰에만 role 클레임 포함 |
| JWT에 매장 ID(storeId) 포함 | 매장 격리를 위한 필수 정보 |

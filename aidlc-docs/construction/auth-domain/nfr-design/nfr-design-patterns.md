# NFR Design Patterns - Unit 2: auth-domain

## 인증 실패 응답 패턴
- 매장 코드 오류, 사용자명 오류, 비밀번호 오류 모두 동일한 메시지 반환
- "인증에 실패했습니다." (공격자에게 유효한 계정 정보 노출 방지)

## 계정 잠금 패턴
- 5회 실패 → 30분 잠금 (application.yml에서 설정 가능)
- 잠금 중 로그인 시도 시 423 응답 + 잠금 해제 시각 안내
- 잠금 시간 경과 후 다음 로그인 시도 시 자동 해제

## Repository 패턴
- StoreRepository: findByStoreCode(storeCode)
- AdminRepository: findByStoreIdAndUsername(storeId, username)
- StoreTableRepository: findByStoreIdAndTableNumber(storeId, tableNumber)
- TableSessionRepository: findByTableIdAndEndTimeIsNull(tableId)

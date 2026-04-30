# NFR Requirements - Unit 2: auth-domain

## 보안
- bcrypt 비밀번호 해싱 (cost factor 10)
- JWT 토큰 16시간 만료
- 로그인 시도 5회 제한, 30분 잠금
- 인증 실패 시 구체적 실패 원인 미노출 ("인증에 실패했습니다." 일반 메시지)

## 성능
- 로그인 API 응답 시간: 500ms 이내 (bcrypt 해싱 포함)

## SECURITY 규칙 적용
- SECURITY-05: 입력 검증 (@Valid)
- SECURITY-08: 인증 엔드포인트 접근 제어
- SECURITY-12: bcrypt 해싱, 로그인 시도 제한
- SECURITY-15: 예외 처리 (fail-closed)

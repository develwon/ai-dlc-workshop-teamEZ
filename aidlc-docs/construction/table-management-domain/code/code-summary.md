# Code Summary - Unit 5: table-management-domain

## 생성/수정 파일 목록

### 수정된 파일 (3개)
| 파일 | 변경 내용 |
|------|-----------|
| `src/main/java/com/tableorder/entity/Order.java` | `deleted` 필드 추가, `softDelete()`, `isDeleted()` 메서드 추가 |
| `src/main/java/com/tableorder/entity/StoreTable.java` | `updatePassword()` 메서드 추가 |
| `src/main/resources/schema.sql` | orders 테이블 `deleted` 컬럼 추가, 인덱스 3개 추가 |

### 수정된 DTO (2개)
| 파일 | 변경 내용 |
|------|-----------|
| `src/main/java/com/tableorder/dto/TableSummaryResponse.java` | `hasActiveSession` 필드로 변경 |
| `src/main/java/com/tableorder/dto/OrderHistoryResponse.java` | `orderItems`를 `List<OrderItemSnapshot>`으로 변경 |

### 생성된 파일 (14개)

#### DTO (1개)
| 파일 | 설명 |
|------|------|
| `src/main/java/com/tableorder/dto/OrderItemSnapshot.java` | 아카이빙용 주문 항목 스냅샷 |

#### Repository (5개)
| 파일 | 설명 |
|------|------|
| `src/main/java/com/tableorder/repository/StoreTableRepository.java` | 테이블 데이터 접근 |
| `src/main/java/com/tableorder/repository/TableSessionRepository.java` | 세션 데이터 접근 |
| `src/main/java/com/tableorder/repository/OrderRepository.java` | 주문 데이터 접근 (논리적 삭제 포함) |
| `src/main/java/com/tableorder/repository/OrderItemRepository.java` | 주문 항목 데이터 접근 |
| `src/main/java/com/tableorder/repository/OrderHistoryRepository.java` | 이력 데이터 접근 (날짜 필터) |

#### Service (3개)
| 파일 | 설명 |
|------|------|
| `src/main/java/com/tableorder/service/OrderHistoryService.java` | 아카이빙 + 이력 조회 |
| `src/main/java/com/tableorder/service/TableSessionService.java` | 세션 관리 + 이용 완료 |
| `src/main/java/com/tableorder/service/AdminTableService.java` | 테이블 설정 + 목록 조회 |

#### Controller (1개)
| 파일 | 설명 |
|------|------|
| `src/main/java/com/tableorder/controller/AdminTableController.java` | 테이블 관리 REST API |

#### 테스트 (5개)
| 파일 | 설명 |
|------|------|
| `src/test/java/com/tableorder/service/OrderHistoryServiceTest.java` | 아카이빙/이력 조회 테스트 |
| `src/test/java/com/tableorder/service/TableSessionServiceTest.java` | 이용 완료 성공/실패 테스트 |
| `src/test/java/com/tableorder/service/AdminTableServiceTest.java` | 테이블 설정/목록 조회 테스트 |
| `src/test/java/com/tableorder/controller/AdminTableControllerTest.java` | Controller 엔드포인트 테스트 |
| `src/test/java/com/tableorder/repository/OrderHistoryRepositoryTest.java` | Repository 쿼리 테스트 |

## API 엔드포인트
| Method | Path | 역할 | 설명 |
|--------|------|------|------|
| POST | /api/admin/tables | OWNER, MANAGER | 테이블 설정 (생성/업데이트) |
| GET | /api/admin/tables | ALL ADMIN | 테이블 목록 조회 |
| POST | /api/admin/tables/{tableId}/complete | OWNER, MANAGER | 이용 완료 |
| GET | /api/admin/tables/{tableId}/history | ALL ADMIN | 과거 이력 조회 |

## 스토리 커버리지
| 스토리 | 구현 상태 |
|--------|-----------|
| US-A03: 테이블 설정 | ✅ |
| US-A03: 이용 완료 | ✅ |
| US-A03: 과거 이력 조회 | ✅ |
| US-A03: 주문 삭제 (논리적) | ✅ |

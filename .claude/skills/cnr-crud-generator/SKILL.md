---
name: cnr-crud-generator
description: >
  CNR 프로젝트에서 새로운 도메인의 CRUD 보일러플레이트 코드를 헥사고날 아키텍처에 맞게 자동 생성하는 스킬.
  사용자가 "도메인 추가", "CRUD 생성", "새 엔티티 만들어줘" 등의 요청을 하면 이 스킬을 사용한다.
  도메인명과 필드 정의를 입력받아 Domain / Port / Application / API / RDS 5개 레이어에 걸쳐 총 17개 파일을 생성한다.
---

# CNR CRUD Generator

## 생성되는 파일 (총 17개)

| 레이어 | 파일 |
|---|---|
| Domain | `{Domain}.java`, `{Domain}CreateCommand.java`, `{Domain}UpdateCommand.java` |
| Port | `{Domain}Repository.java`, `{Domain}Dto.java`, `{Domain}CreateDto.java`, `{Domain}UpdateDto.java` |
| Application | `{Domain}Mapper.java`, `{Domain}QueryService.java` |
| Inbound API | `{Domain}Api.java`, `{Domain}UseCase.java`, `{Domain}CreateRequest.java`, `{Domain}UpdateRequest.java`, `{Domain}Response.java` |
| Outbound RDS | `{Domain}Entity.java`, `{Domain}JpaRepository.java`, `{Domain}RepositoryImpl.java` |

## 워크플로우

### Step 1: 사용자에게 정보 수집

다음 두 가지를 반드시 확인한다:

**① 도메인명** — PascalCase, 예: `Product`, `OrderItem`, `UserProfile`

**② 필드 정의** — 쉼표 구분, 형식: `fieldName:JavaType[!]`
- `!` 접미사 = DB `nullable = false` 제약
- `id` 필드는 자동 생성되므로 제외

```
예시: name:String!,price:Long!,description:String,createdAt:LocalDateTime!
```

**지원 Java 타입:**
- 별도 import 불필요: `String`, `Long`, `Integer`, `Boolean`, `Double`
- import 자동 추가: `BigDecimal`, `LocalDate`, `LocalDateTime`, `LocalTime`, `UUID`

### Step 2: 스크립트 실행

```bash
python3 .claude/skills/cnr-crud-generator/scripts/generate_crud.py \
  <DomainName> "<field1:Type[!],field2:Type[!],...>" \
  --base-dir <프로젝트 루트 경로>
```

**예시:**
```bash
python3 .claude/skills/cnr-crud-generator/scripts/generate_crud.py \
  Product "name:String!,price:Long!,description:String,createdAt:LocalDateTime!" \
  --base-dir /Users/nzzi/IdeaProjects/cnr
```

### Step 3: 생성 결과 확인 및 안내

스크립트 실행 후 사용자에게:
1. 생성된 17개 파일 목록 요약
2. 테이블명 확인 (PascalCase → snake_case 자동 변환, 예: `OrderItem` → `order_item`)
3. 비즈니스 로직이 필요한 경우 `{Domain}QueryService`에 메서드를 추가하도록 안내
4. 도메인 고유 검증 로직이 필요한 경우 `{Domain}` record에 메서드를 추가하도록 안내

## 아키텍처 규칙 (생성 코드에 반영됨)

- **의존성 방향**: `api` → `application` → `domain` ← `port` ← `rds`
- **데이터 흐름**: `Request → Command → Dto → Entity → Dto → Domain → Response`
- **에러 처리**: `RepositoryResult` (rds→port), `CommandResult` (application→api), 예외 throw 금지
- **URL 경로**: PascalCase → kebab-case (예: `OrderItem` → `/v1/order-item`)
- **테이블명**: PascalCase → snake_case (예: `OrderItem` → `order_item`)
- **생성자 주입만 사용** (필드 주입 금지)

# module-adaptor

**어댑터(Adapter) 계층**의 상위 모듈입니다. 헥사고날 아키텍처에서 외부 시스템과 내부 비즈니스 로직을 연결하는 모든 구현체가 이 모듈 하위에 위치합니다.

## 역할

- 외부 세계와 내부 비즈니스 로직의 연결점
- 인바운드(외부 → 내부) 및 아웃바운드(내부 → 외부) 어댑터 관리

## 하위 모듈 구조

```
module-adaptor/
├── inbound/              ← 외부에서 들어오는 요청 처리
│   ├── api/              ← REST API 컨트롤러
│   ├── event/            ← 이벤트 리스너 (스켈레톤)
│   └── batch/            ← 배치 작업 (스켈레톤)
└── outbound/             ← 내부에서 외부로 나가는 요청 처리
    ├── rds/              ← RDS 저장소 구현
    └── external/         ← 외부 API 클라이언트
        └── foo-client/   ← Foo 외부 서비스 연동 (스켈레톤)
```

## 인바운드 vs 아웃바운드

| 구분 | 인바운드 (Inbound) | 아웃바운드 (Outbound) |
|---|---|---|
| 방향 | 외부 → 내부 | 내부 → 외부 |
| 역할 | 외부 요청을 받아서 내부 서비스 호출 | 내부에서 외부 시스템에 데이터 저장/조회 |
| 예시 | REST API, 이벤트 리스너, 배치 | DB 접근, 외부 API 호출 |
| 의존 대상 | `module-core:application`, `module-core:domain` | `module-core:port` |

## 상세 README

| 모듈       | 설명            | 링크                                                         |
|----------|---------------|------------------------------------------------------------|
| api      | REST API 컨트롤러 | [inbound/api/README.md](inbound/api/README.md)             |
| event    | 이벤트 리스너       | [inbound/event/README.md](inbound/event/README.md)         |
| batch    | 배치 작업         | [inbound/batch/README.md](inbound/batch/README.md)         |
| rds      | RDS 저장소 구현    | [outbound/rds/README.md](outbound/rds/README.md)           |
| external | 외부 API 클라이언트  | [outbound/external/README.md](outbound/external/README.md) |

## 어댑터 추가 가이드

새로운 어댑터를 추가할 때:

1. 적절한 위치에 모듈 생성 (`inbound/` 또는 `outbound/`)
2. `settings.gradle`에 모듈 등록
3. `build.gradle` 작성 (Spring Boot 플러그인 + `bootJar.enabled = false`)
4. `module-bootstrap/build.gradle`에 의존성 추가

# module-adaptor:outbound

**아웃바운드 어댑터**의 상위 모듈입니다. 내부 비즈니스 로직에서 외부 시스템으로 나가는 모든 구현체가 이 하위에 위치합니다.

## 역할

`module-core:port`에 정의된 인터페이스의 **실제 구현체**를 제공합니다. 데이터베이스 접근, 외부 API 호출 등 인프라 기술에 종속적인 코드가 이 계층에 위치합니다.

## 하위 모듈

| 모듈                  | 설명                         | 상태 | 링크                                                             |
|---------------------|----------------------------|---|----------------------------------------------------------------|
| rds                 | RDS 저장소 구현 (JPA, QueryDSL) | 구현됨 | [rds/README.md](rds/README.md)                                 |
| external            | 외부 API 클라이언트               | 상위 모듈 | [external/README.md](external/README.md)                       |
| external:foo-client | Foo 외부 서비스 연동              | 스켈레톤 | [external/foo-client/README.md](external/foo-client/README.md) |

## 공통 패턴

모든 아웃바운드 어댑터는 다음 패턴을 따릅니다:

1. `module-core:port`의 인터페이스를 구현 (`implements FooRepository`)
2. Port DTO를 내부 엔티티/요청 객체로 변환
3. 외부 시스템(DB, API)과 통신
4. 결과를 다시 Port DTO로 변환하여 반환

```
[Port 인터페이스 호출] → 어댑터 구현체 → [내부 엔티티 변환] → [외부 시스템] → [Port DTO 반환]
```

## 의존성 방향

```
outbound 어댑터 → module-core:port (인터페이스 구현)
```

> **주의**: 아웃바운드 어댑터는 `module-core:domain`이나 `module-core:application`에 의존하지 않습니다. 오직 `port` 인터페이스만 구현합니다.

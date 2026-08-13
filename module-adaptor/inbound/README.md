# module-adaptor:inbound

**인바운드 어댑터**의 상위 모듈입니다. 외부에서 들어오는 요청을 받아 내부 비즈니스 로직을 호출하는 모든 어댑터가 이 하위에 위치합니다.

## 역할

외부 세계의 다양한 요청 형태(HTTP, 이벤트, 스케줄)를 수신하여 내부 `application` 서비스를 호출합니다.

## 하위 모듈

| 모듈 | 설명 | 상태 | 링크 |
|---|---|---|---|
| api | REST API 엔드포인트 | 구현됨 | [api/README.md](api/README.md) |
| event | 이벤트 리스너 | 스켈레톤 | [event/README.md](event/README.md) |
| batch | 배치 작업 | 스켈레톤 | [batch/README.md](batch/README.md) |

## 공통 패턴

모든 인바운드 어댑터는 다음 패턴을 따릅니다:

1. 외부 요청을 수신 (HTTP, 이벤트, 스케줄 등)
2. Request 객체를 도메인 Command로 변환
3. `application` 서비스 또는 UseCase를 호출
4. 도메인 결과를 Response로 변환하여 반환

```
[외부 요청] → Request → Command → Service → Domain → Response → [외부 응답]
```

## 의존성 방향

```
inbound 어댑터 → module-core:application (서비스 호출)
inbound 어댑터 → module-core:domain (Command, 도메인 모델 참조)
```

> **주의**: 인바운드 어댑터는 아웃바운드 어댑터에 직접 의존하지 않습니다. 반드시 `application` 서비스를 통해 간접적으로 접근합니다.

# module-core

애플리케이션의 **핵심 비즈니스 로직**을 담당하는 상위 모듈입니다. 헥사고날 아키텍처의 내부(inner) 영역에 해당합니다.

## 역할

비즈니스 규칙과 애플리케이션 로직을 외부 인프라로부터 격리합니다. 이 모듈 내부의 코드는 DB, HTTP, 메시지 큐 등의 구체적인 인프라 기술에 의존하지 않습니다.

## 하위 모듈 구조

```
module-core/
├── domain/       ← 순수 도메인 모델 및 비즈니스 규칙
├── application/  ← 애플리케이션 서비스 (유즈케이스 구현)
└── port/         ← 외부 시스템과의 인터페이스(계약) 정의
```

## 하위 모듈 간 의존 관계

```
application
├── domain       (도메인 모델 사용)
└── port         (외부 시스템 인터페이스 사용)

domain           (의존성 없음 - 순수 Java)
port             (의존성 없음 - 순수 Java)
```

## 데이터 흐름

```
[외부 요청] → application → domain (비즈니스 로직) 
                  │
                  └──→ port (인터페이스) → [외부 시스템 어댑터]
```

## 상세 README

| 모듈 | 설명 | 링크 |
|---|---|---|
| domain | 도메인 모델, 비즈니스 규칙 | [domain/README.md](domain/README.md) |
| application | 애플리케이션 서비스, 매퍼 | [application/README.md](application/README.md) |
| port | 외부 시스템 인터페이스 | [port/README.md](port/README.md) |

## 새로운 도메인 추가 시 작업 순서

1. `domain/`에 도메인 record 및 Command 클래스 생성
2. `port/`에 Repository 인터페이스 및 DTO record 생성
3. `application/`에 Mapper와 Service 클래스 생성
4. `module-adaptor`에서 실제 구현체 작성

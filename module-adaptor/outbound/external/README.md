# module-adaptor:outbound:external

**외부 API 아웃바운드 어댑터**의 상위 모듈입니다. 외부 서비스와의 HTTP 통신을 담당하는 클라이언트 모듈들이 이 하위에 위치합니다.

## 역할

- 외부 REST API 호출 클라이언트 관리
- Spring Cloud OpenFeign 기반 선언적 HTTP 클라이언트

## 하위 모듈

| 모듈 | 설명 | 상태 | 링크 |
|---|---|---|---|
| foo-client | Foo 외부 서비스 연동 | 스켈레톤 | [foo-client/README.md](foo-client/README.md) |
| kakao-oauth-client | Kakao OAuth 연동 | 구현 | `kakao-oauth-client/` |
| naver-map-client | Naver Map Open API 연동 | 스켈레톤 | [naver-map-client/README.md](naver-map-client/README.md) |

## 새로운 외부 API 클라이언트 추가 가이드

1. `external/` 하위에 새 모듈 디렉토리 생성 (예: `bar-client/`)
2. `settings.gradle`에 모듈 등록
3. `build.gradle` 작성 (OpenFeign 의존성 포함)
4. Feign Client 인터페이스 정의
5. Port Repository 인터페이스 구현
6. `module-bootstrap/build.gradle`에 의존성 추가

### 예시 구조

```
external/
└── bar-client/
    ├── build.gradle
    └── src/main/java/com/toy/cnr/external/bar/
        ├── BarFeignClient.java        ← Feign 클라이언트 인터페이스
        └── BarExternalRepository.java ← Port 구현체
```

# module-adaptor:inbound:security

Spring Security 기반 인증(Authentication) / 인가(Authorization) 모듈.
JWT(Access Token + Refresh Token) 방식의 Stateless 세션 관리를 구현하며, 이메일/패스워드 로그인과 OAuth 로그인을 모두 지원한다.

## 패키지 구조

```
com.toy.cnr.security
├── configuration/       # Spring Security, BCryptPasswordEncoder 설정
├── filter/              # Security Filter Chain 에 등록되는 필터
├── converter/           # HttpServletRequest → Authentication 변환
├── provider/            # AuthenticationProvider 구현체
├── handler/             # 인증/인가 성공·실패 핸들러
├── model/
│   ├── authentication/  # Authentication 토큰 구현체 (AccessToken, RefreshToken, OAuthToken 등)
│   ├── detail/          # UserDetails 확장 (AuthenticatedUser)
│   ├── jwt/             # JwtType enum
│   └── response/        # Security 전용 응답 모델
├── port/                # 외부 구현 필요 인터페이스 (SPI)
├── wrapper/             # HttpServletRequestWrapper
├── exception/           # Security 전용 예외
└── util/                # JWT, Email, HttpServletRequest, Mapper, UserPrincipal 유틸
```

## Security Filter Chain

`SecurityConfiguration`에서 `SecurityFilterChain`을 구성하며, 세션은 **STATELESS**로 동작한다.

### 필터 실행 순서

```
Request
  │
  ├─ UserAuthenticationFilter    POST /v1/auth/token   (이메일/패스워드 로그인)
  ├─ OAuthAuthenticationFilter   POST /v1/auth/oauth   (OAuth 로그인)
  ├─ RefreshAuthenticationFilter POST /v1/auth/refresh  (토큰 갱신)
  │       ↑ LogoutFilter 이전에 등록
  │
  └─ UserAuthorizationFilter     모든 인증 필요 요청    (Bearer 토큰 검증)
          ↑ UsernamePasswordAuthenticationFilter 이전에 등록
```

- `PermitMatcherProvider`에 등록된 경로는 인가 필터를 건너뛴다.
- 각 필터는 `OncePerRequestFilter`를 상속하며, 경로 매칭(`AntPathRequestMatcher`)으로 자신이 처리할 요청만 가로챈다.

## 인증 흐름별 상세

### 1. 이메일/패스워드 로그인 (`POST /v1/auth/token`)

| 단계 | 클래스 | 역할 |
|------|--------|------|
| Convert | `UserAuthenticationConverter` | `email`, `password` 파라미터 추출 → `UsernamePasswordAuthenticationToken` |
| Authenticate | `UserAuthenticationProvider` | `UserDetailsService`로 사용자 조회 → BCrypt 패스워드 검증 → JWT 발급 |
| Success | `UserAuthenticationSuccessHandler` | Access Token JSON 응답 + Refresh Token HttpOnly 쿠키 설정 + 마지막 로그인 시간 갱신 |
| Failure | `UserAuthenticationFailureHandler` | 에러 상태 코드 + 메시지 JSON 응답 |

### 2. OAuth 로그인 (`POST /v1/auth/oauth`)

| 단계 | 클래스 | 역할 |
|------|--------|------|
| Convert | `OAuthAuthenticationConverter` | 요청 Body에서 `provider`, `code` 추출 → `OAuthToken` |
| Authenticate | `OAuthAuthenticationProvider` | `OAuthUserLoaderService`로 사용자 조회/생성 → JWT 발급 |
| Success/Failure | 이메일/패스워드 로그인과 동일 핸들러 공유 | |

### 3. 토큰 갱신 (`POST /v1/auth/refresh`)

| 단계 | 클래스 | 역할 |
|------|--------|------|
| Convert | `RefreshAuthenticationConverter` | 쿠키에서 `refreshToken` 추출 → `RefreshToken` |
| Authenticate | `RefreshAuthenticationProvider` | Refresh Token 유효성 검증 → 새 Access Token 발급 (Refresh Token 재사용) |
| Success/Failure | 이메일/패스워드 로그인과 동일 핸들러 공유 | |

### 4. 인가 (모든 인증 필요 요청)

| 단계 | 클래스 | 역할 |
|------|--------|------|
| Convert | `UserAuthorizationConverter` | `Authorization: Bearer <token>` 헤더에서 토큰 추출 → `AccessToken` |
| Authenticate | `UserAuthorizationProvider` | JWT 파싱 → `UserDetailsService`로 사용자 조회 → `UserPrincipal` 생성 |
| Success | `UserAuthorizationSuccessHandler` | `SecurityContextHolder`에 인증 정보 설정 → 다음 필터 진행 |
| Failure | `UserAuthorizationFailureHandler` | 계정 관련 예외 → 400, 그 외 → 401 응답 |

## Authentication 토큰 모델

| 클래스 | 용도 |
|--------|------|
| `UnAuthentication` | 미인증 상태 기본 `Authentication` 구현 (메서드 호출 시 예외) |
| `AccessToken` | 인가 필터에서 사용하는 미검증 Bearer 토큰 |
| `RefreshToken` | 갱신 필터에서 사용하는 미검증 Refresh 토큰 |
| `OAuthToken` | OAuth 필터에서 사용하는 provider + code 쌍 |
| `BearerAuthenticationToken` | 인증 성공 후 Access Token + Refresh Token + 만료 시간을 담는 결과 객체 |
| `UserPrincipal` | 인가 성공 후 `SecurityContextHolder`에 저장되는 인증된 사용자 |
| `EmptyPrincipal` | 권한 없는 빈 인증 객체 (싱글턴) |

## JWT 설정

`JwtUtil`이 HMAC-SHA 기반으로 토큰을 발급/검증한다.

| 설정 키 | 설명 | 기본값 |
|---------|------|--------|
| `jwt.private` | HMAC 서명 키 | 프로필별 설정 |
| `jwt.accessToken.ttl` | Access Token TTL (초) | `3600` (1시간) |
| `jwt.refreshToken.ttl` | Refresh Token TTL (초) | `43200` (12시간) |

토큰 Claims에 `type` 필드(`ACCESS_TOKEN` / `REFRESH_TOKEN`)를 포함하여 토큰 종류를 구분한다.

## 인증 제외 경로 (Permit Matcher)

`application-security.yml`의 `permitMatcher.path`에 method + pattern 쌍으로 정의한다.

```yaml
permitMatcher:
  path:
    - method: "GET"
      pattern: "/elb-health"
    - method: "*"
      pattern: "/swagger-ui/**"
    # ...
```

`method: "*"`은 모든 HTTP 메서드를 허용한다.

## SPI (외부 모듈 구현 필수)

이 모듈을 사용하려면 아래 인터페이스를 구현하는 빈을 등록해야 한다.

| 인터페이스 | 역할 |
|-----------|------|
| `SecurityUserLoaderService` | `UserDetailsService` + `updateLastLoginAt()` — 이메일/패스워드 로그인과 인가에 사용 |
| `OAuthUserLoaderService` | OAuth code로 사용자 조회/생성 — OAuth 로그인에 사용 |

`DefaultSecurityUserLoaderService`는 no-op 폴백으로만 존재하며, 실제 서비스에서 반드시 오버라이드해야 한다.

## Mock 프로필

`@Profile("mock")` 활성화 시 `MockUserAuthorizationProvider`가 `UserAuthorizationProvider` 대신 등록된다.
JWT 검증 없이 `UserDetailsService`에서 빈 문자열로 사용자를 조회하여 인가를 우회한다.

## 응답 모델

모든 Security 응답은 `SecurityResponse`를 상속한다.

```json
// 성공 (SuccessResponse)
{ "success": true, "code": 200, "message": "OK", "content": { "accessToken": "...", "accessTokenExpiresIn": "..." } }

// 실패 (FailResponse)
{ "success": false, "code": 401, "message": "비정상 요청입니다." }
```

Refresh Token은 `@JsonIgnore`로 JSON 응답에 포함되지 않으며, HttpOnly 쿠키로만 전달된다.

## 의존성

```groovy
api "org.springframework.boot:spring-boot-starter-security"
implementation "org.springframework.boot:spring-boot-starter-web"
implementation "io.jsonwebtoken:jjwt-api"
runtimeOnly "io.jsonwebtoken:jjwt-impl"
runtimeOnly "io.jsonwebtoken:jjwt-jackson"
```

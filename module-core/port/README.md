# module-core:port

**포트(Port) 인터페이스**를 정의하는 모듈입니다. 헥사고날 아키텍처에서 내부와 외부를 연결하는 **계약(contract)**을 정의합니다.

## 역할

- 외부 시스템(DB, 외부 API 등)과의 인터페이스 정의
- 데이터 전달 객체(DTO) 정의
- 구현체 없이 인터페이스만 정의하여 의존성 역전 원칙(DIP) 실현

## 핵심 원칙

- **구현 없음**: 인터페이스와 DTO만 정의 (구현은 `module-adaptor`에서)
- **프레임워크 의존성 없음**: 순수 Java만 사용
- **외부 표현**: 외부 시스템과 주고받는 데이터의 형태를 DTO로 정의

## 구조

```
port/
├── build.gradle
└── src/main/java/com/toy/cnr/port/
    ├── common/
    │   └── RepositoryResult.java       ← Repository 결과 sealed 타입
    └── foo/
        ├── FooRepository.java          ← 저장소 인터페이스
        └── model/
            ├── FooDto.java             ← 조회 응답 DTO
            ├── FooCreateDto.java       ← 생성 요청 DTO
            └── FooUpdateDto.java       ← 수정 요청 DTO
```

## 주요 클래스

### RepositoryResult.java (sealed 결과 타입)

```java
public sealed interface RepositoryResult<T>
    permits RepositoryResult.Found, RepositoryResult.NotFound, RepositoryResult.Error {

    record Found<T>(T data) implements RepositoryResult<T> {}
    record NotFound<T>(String message) implements RepositoryResult<T> {}
    record Error<T>(String message) implements RepositoryResult<T> {}
}
```

- `Optional` 대신 **sealed 타입**으로 Repository 결과를 표현
- `Found` / `NotFound` / `Error` 세 가지 케이스를 타입으로 구분
- 호출자가 switch 패턴 매칭으로 모든 케이스를 처리하도록 강제

### FooRepository.java (포트 인터페이스)

```java
public interface FooRepository {
    List<FooDto> findAll();
    RepositoryResult<FooDto> findById(Long id);
    FooDto save(FooCreateDto dto);
    RepositoryResult<FooDto> update(Long id, FooUpdateDto dto);
    void deleteById(Long id);
}
```

- 이 인터페이스의 구현체는 `module-adaptor:outbound:rds`에 위치
- `application` 모듈에서는 이 인터페이스에만 의존하여, 실제 저장소 기술과 무관하게 동작
- `findById`, `update`는 `RepositoryResult`를 반환하여 성공/실패를 타입으로 표현

### DTO 클래스 (record)

```java
public record FooDto(Long id, String name, String description) {}
public record FooCreateDto(String name, String description) {}
public record FooUpdateDto(String name, String description) {}
```

- Port 계층 전용 DTO: 도메인 모델(`Foo`)과 분리
- 외부 시스템(DB)의 데이터 표현에 맞춘 구조

## Port vs Domain 분리 이유

| 구분 | Domain (`Foo`) | Port DTO (`FooDto`) |
|---|---|---|
| 위치 | `module-core:domain` | `module-core:port` |
| 용도 | 비즈니스 로직 수행 | 외부 시스템과 데이터 전달 |
| 특징 | 비즈니스 메서드 포함 | 순수 데이터 운반 |
| 변환 | `FooMapper`가 상호 변환 | `FooMapper`가 상호 변환 |

## 새로운 포트 추가 예시

`Bar` 도메인에 대한 포트를 추가하는 경우:

```
port/src/main/java/com/toy/cnr/port/
└── bar/
    ├── BarRepository.java
    └── model/
        ├── BarDto.java
        ├── BarCreateDto.java
        └── BarUpdateDto.java
```

```java
package com.toy.cnr.port.bar;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.bar.model.*;
import java.util.List;

public interface BarRepository {
    List<BarDto> findAll();
    RepositoryResult<BarDto> findById(Long id);
    BarDto save(BarCreateDto dto);
}
```

## 의존성

```groovy
// Port 인터페이스 모듈 - 프레임워크 의존성 없음
dependencies {
}
```

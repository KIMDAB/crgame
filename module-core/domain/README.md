# module-core:domain

**순수 도메인 모델과 비즈니스 규칙**을 정의하는 모듈입니다. 헥사고날 아키텍처에서 가장 안쪽(innermost) 계층에 해당합니다.

## 역할

- 도메인 엔티티 정의 (Java record 활용)
- 비즈니스 규칙 및 로직 구현
- Command 객체 정의 (생성/수정 명령)

## 핵심 원칙

- **외부 의존성 없음**: Spring, JPA 등 어떤 프레임워크에도 의존하지 않습니다
- **순수 Java**: JDK만 사용하여 테스트와 유지보수가 용이합니다
- **불변 객체**: Java record를 사용하여 불변성을 보장합니다

## 구조

```
domain/
├── build.gradle
└── src/main/java/com/toy/cnr/domain/
    ├── common/
    │   └── CommandResult.java     ← 명령 실행 결과 sealed 타입
    └── foo/
        ├── Foo.java               ← 도메인 모델 + 비즈니스 로직
        ├── FooCreateCommand.java  ← 생성 명령 객체
        └── FooUpdateCommand.java  ← 수정 명령 객체
```

## 주요 클래스

### Foo.java (도메인 모델)

```java
public record Foo(Long id, String name, String description) {
    // 비즈니스 로직은 도메인 모델 내부에 정의
    public Foo fooBusinessLogic(Number arg) {
        return new Foo(this.id, this.name + "_processed" + arg, this.description);
    }
}
```

- `record`를 사용하여 불변 도메인 객체 생성
- 비즈니스 로직(`fooBusinessLogic`)을 도메인 모델 내부에 포함 (Rich Domain Model)
- 새로운 상태를 반환하는 방식(불변성 유지)

### FooCreateCommand.java / FooUpdateCommand.java (명령 객체)

```java
public record FooCreateCommand(String name, String description) {}
public record FooUpdateCommand(String name, String description) {}
```

- 외부에서 들어오는 요청을 도메인 레벨의 명령으로 표현
- API 레이어의 Request 객체와 분리하여 도메인 독립성 유지

## 새로운 도메인 추가 예시

`Bar`라는 새 도메인을 추가하는 경우:

```
domain/src/main/java/com/toy/cnr/domain/
└── bar/
    ├── Bar.java
    ├── BarCreateCommand.java
    └── BarUpdateCommand.java
```

```java
// Bar.java
package com.toy.cnr.domain.bar;

public record Bar(Long id, String title, int priority) {
    public Bar escalate() {
        return new Bar(this.id, this.title, this.priority + 1);
    }
}

// BarCreateCommand.java
package com.toy.cnr.domain.bar;

public record BarCreateCommand(String title, int priority) {}
```

## CommandResult (sealed 결과 타입)

서비스/애플리케이션 계층의 명령 실행 결과를 표현하는 sealed 인터페이스입니다.

```java
public sealed interface CommandResult<T>
    permits CommandResult.Success, CommandResult.ValidationError, CommandResult.BusinessError {

    record Success<T>(T data, String message) implements CommandResult<T> {}
    record ValidationError<T>(List<String> errors) implements CommandResult<T> {}
    record BusinessError<T>(String reason) implements CommandResult<T> {}
}
```

- `Success`: 명령이 성공적으로 수행된 경우
- `ValidationError`: 입력 데이터 검증에 실패한 경우
- `BusinessError`: 비즈니스 규칙 위반 또는 리소스를 찾지 못한 경우

### 사용 예시

```java
public CommandResult<Foo> findById(Long id) {
    return switch (fooRepository.findById(id)) {
        case RepositoryResult.Found(var data) ->
            new CommandResult.Success<>(FooMapper.toDomain(data), null);
        case RepositoryResult.NotFound(var msg) ->
            new CommandResult.BusinessError<>(msg);
        case RepositoryResult.Error(var msg) ->
            new CommandResult.BusinessError<>(msg);
    };
}
```

## 의존성

```groovy
// 순수 Java / JDK only - 프레임워크 의존성 없음
dependencies {
}
```

# module-core:application

**애플리케이션 서비스 계층**으로, 도메인 로직을 오케스트레이션하고 외부 시스템(포트)과 연결하는 역할을 합니다.

## 역할

- 도메인 모델과 포트를 연결하는 서비스 클래스 제공
- 데이터 변환(매핑) 로직 관리
- 트랜잭션 경계 설정

## 구조

```
application/
├── build.gradle
└── src/main/java/com/toy/cnr/application/
    └── foo/
        ├── mapper/
        │   └── FooMapper.java         ← 데이터 변환 유틸리티
        └── service/
            └── FooQueryService.java   ← 애플리케이션 서비스
```

## 의존성

```groovy
dependencies {
    implementation project(':module-core:domain')   // 도메인 모델 사용
    implementation project(':module-core:port')     // 포트 인터페이스 사용
    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

## 주요 클래스

### FooMapper.java (데이터 변환)

```java
@UtilityClass
public class FooMapper {
    // Port DTO → Domain 변환
    public static Foo toDomain(FooDto dto) { ... }

    // Domain Command → Port DTO 변환
    public static FooCreateDto toExternal(FooCreateCommand command) { ... }
    public static FooUpdateDto toExternal(FooUpdateCommand command) { ... }
}
```

- `static` 메서드만 포함하는 유틸리티 클래스
- 계층 간 데이터 변환을 중앙화하여 관리

### FooQueryService.java (애플리케이션 서비스)

```java
@Service
public class FooQueryService {
    private final FooRepository fooRepository;

    public List<Foo> findAll() { ... }
    public Foo doBusinessLogic(Foo foo) { ... }
    public void delete(Long id) { ... }

    // RepositoryResult → CommandResult 변환 (switch 패턴 매칭)
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

    public CommandResult<Foo> create(FooCreateCommand command) { ... }
    public CommandResult<Foo> update(Long id, FooUpdateCommand command) { ... }
}
```

- 포트 인터페이스(`FooRepository`)에만 의존 (구현체 모름)
- `RepositoryResult` sealed 타입을 switch 패턴 매칭으로 처리하여 `CommandResult`로 변환
- 예외를 던지지 않고 타입으로 성공/실패를 표현

## 데이터 흐름

```
[인바운드 어댑터] ──요청──▶ Service
                              │
                    ┌─────────┴──────────┐
                    ▼                    ▼
            Domain (로직 처리)   Port (데이터 조회/저장)
                    │                    │
                    ▼                    ▼
            Mapper 변환          [아웃바운드 어댑터]
```

## 새로운 서비스 추가 예시

`Bar` 도메인에 대한 서비스를 추가하는 경우:

```
application/src/main/java/com/toy/cnr/application/
└── bar/
    ├── mapper/
    │   └── BarMapper.java
    └── service/
        └── BarQueryService.java
```

```java
@Service
public class BarQueryService {
    private final BarRepository barRepository;

    public BarQueryService(BarRepository barRepository) {
        this.barRepository = barRepository;
    }

    public CommandResult<Bar> findById(Long id) {
        return switch (barRepository.findById(id)) {
            case RepositoryResult.Found(var data) ->
                new CommandResult.Success<>(BarMapper.toDomain(data), null);
            case RepositoryResult.NotFound(var msg) ->
                new CommandResult.BusinessError<>(msg);
            case RepositoryResult.Error(var msg) ->
                new CommandResult.BusinessError<>(msg);
        };
    }
}
```

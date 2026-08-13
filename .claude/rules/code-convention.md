---
paths: "**/*.java"
---

# 코드 스타일 가이드

이 문서는 CNR 프로젝트의 Java 코드 작성 규칙을 정의합니다.

## 들여쓰기

- **4 spaces**를 사용합니다 (탭 사용 금지)
- 모든 중괄호 내부 코드는 한 단계 들여쓰기합니다

```java
public class Example {
    private String field;  // 4 spaces
    
    public void method() {
        if (condition) {
            doSomething();  // 8 spaces
        }
    }
}
```

## 메서드 선언 및 호출

### 짧은 매개변수 (에디터 최대 줄 길이 이내)

매개변수가 에디터 최대 줄 길이를 넘지 않으면 한 줄에 작성합니다.

```java
// 메서드 선언
public void doSomething(Long id1, Long id2) {
    // ...
}

// 메서드 호출
doSomething(id1, id2);

// 생성자
return new MyClass(this.id1, this.id2);
```

### 긴 매개변수 (에디터 최대 줄 길이 초과)

매개변수가 에디터 최대 줄 길이를 초과하면 **각 매개변수를 새 줄에 작성**하고 **세로로 정렬**합니다.

#### 메서드 선언

```java
public ResponseEntity<FooResponse> update(
    @PathVariable Long id,
    @RequestBody FooUpdateRequest request
) {
    return ResponseEntity.ok(fooUseCase.update(id, request));
}

public void doSomething(
    Long id1,
    Long id2,
    Long id3,
    Long id4,
    Long id5,
    Long id6,
    Long id7
) {
    // ...
}
```

#### 메서드 호출

```java
doSomething(
    id1,
    id2,
    id3,
    id4,
    id5,
    id6,
    id7
);
```

#### 생성자 호출

```java
public MyClass findMyClass() {
    return new MyClass(
        this.id1,
        this.id2,
        this.id3,
        this.id4,
        this.id5,
        this.id6
    );
}
```

#### Record 선언

```java
// 짧은 경우
public record FooDto(Long id, String name) {}

// 긴 경우
public record ComplexDto(
    Long id,
    String name,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy
) {}
```

## 중괄호 스타일

### 여는 중괄호 `{`

- 클래스, 메서드, 제어문의 여는 중괄호는 **같은 줄**에 위치합니다

```java
public class Example {
    public void method() {
        if (condition) {
            // ...
        }
    }
}
```

### 닫는 중괄호 `}`

- 매개변수가 여러 줄일 경우, 닫는 괄호 `)`는 **별도의 줄**에 위치합니다
- 닫는 괄호는 여는 괄호와 같은 들여쓰기 레벨을 유지합니다

```java
public void method(
    Long param1,
    String param2
) {  // 닫는 괄호와 여는 중괄호는 같은 줄
    // ...
}
```

## 어노테이션

### 단일 어노테이션

```java
@Service
public class FooService {
    // ...
}
```

### 다중 어노테이션

각 어노테이션은 별도의 줄에 작성합니다.

```java
@RestController
@RequestMapping("/v1/foo")
public class FooApi {
    // ...
}
```

### 매개변수 어노테이션

```java
// 짧은 경우 - 한 줄
public void method(@PathVariable Long id) {
    // ...
}

// 긴 경우 - 매개변수와 함께 세로 정렬
public ResponseEntity<FooResponse> update(
    @PathVariable Long id,
    @RequestBody FooUpdateRequest request,
    @RequestHeader("Authorization") String token
) {
    // ...
}
```

## 공백

### 연산자 주변

이항 연산자 앞뒤에 공백을 추가합니다.

```java
int sum = a + b;
boolean result = (x > 0) && (y < 10);
String text = "Hello" + " World";
```

### 콤마 `,` 뒤

콤마 뒤에는 공백을 추가합니다.

```java
method(arg1, arg2, arg3);
List<String> list = List.of("a", "b", "c");
```

### 중괄호 내부

여는 중괄호 뒤와 닫는 중괄호 앞에는 공백을 넣지 않습니다.

```java
if (condition) {
    doSomething();
}
```

## 줄 길이

- 권장 최대 줄 길이: **120자**
- 줄이 너무 길면 위 규칙에 따라 줄바꿈합니다

## 메서드 체이닝

긴 메서드 체이닝은 각 메서드를 새 줄에 작성합니다.

```java
// 짧은 경우
List<String> result = list.stream().filter(s -> s.length() > 5).toList();

// 긴 경우
List<FooResponse> result = fooList.stream()
    .filter(foo -> foo.id() != null)
    .map(FooResponse::from)
    .sorted(Comparator.comparing(FooResponse::id))
    .toList();
```

## Lambda 표현식

### 짧은 람다

```java
list.forEach(item -> System.out.println(item));
```

### 긴 람다

```java
list.forEach(item -> {
    System.out.println(item);
    doSomething(item);
});
```

### 여러 매개변수를 가진 람다

```java
map.forEach((key, value) -> {
    System.out.println(key + ": " + value);
});
```

## 예시: 실제 코드 적용

### Controller 예시

```java
@RestController
@RequestMapping("/v1/foo")
public class FooApi {
    private final FooUseCase fooUseCase;

    public FooApi(FooUseCase fooUseCase) {
        this.fooUseCase = fooUseCase;
    }

    @GetMapping
    public ResponseEntity<List<FooResponse>> findAll() {
        return ResponseEntity.ok(fooUseCase.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FooResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(fooUseCase.findFooWithBusinessLogic(id));
    }

    @PostMapping
    public ResponseEntity<FooResponse> create(
        @RequestBody FooCreateRequest request
    ) {
        return ResponseEntity.ok(fooUseCase.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FooResponse> update(
        @PathVariable Long id,
        @RequestBody FooUpdateRequest request
    ) {
        return ResponseEntity.ok(fooUseCase.update(id, request));
    }
}
```

### Service 예시

```java
@Service
public class FooQueryService {
    private final FooRepository fooRepository;

    public FooQueryService(FooRepository fooRepository) {
        this.fooRepository = fooRepository;
    }

    public Foo findById(Long id) {
        var externalFoo = fooRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(
                "Foo not found with id: " + id
            ));
        return FooMapper.toDomain(externalFoo);
    }

    public Foo create(FooCreateCommand command) {
        var externalRequest = FooMapper.toExternal(command);
        var externalFoo = fooRepository.save(externalRequest);
        return FooMapper.toDomain(externalFoo);
    }
}
```

## Lombok 어노테이션

### lombok.config : 프로젝트 루트 디렉토리에 `lombok.config` 파일을 읽어 롬복 스타일을 일관되게 유지합니다.

## IntelliJ IDEA 설정

프로젝트 전체에 이 스타일을 적용하려면 IntelliJ IDEA에서:

1. `Preferences` → `Editor` → `Code Style` → `Java`
2. `Tabs and Indents` → `Tab size: 4`, `Indent: 4`, `Use tab character: 체크 해제`
3. `Wrapping and Braces` → `Hard wrap at: 120`
4. `Method declaration parameters` → `Wrap if long` 선택
5. `Align when multiline` 체크

## Package & Naming Conventions

Base package: `com.toy.cnr`

| Module | Package | Key class patterns |
|--------|---------|-------------------|
| `module-core:domain` | `.domain.<name>` | `<Name>` (record), `<Name>CreateCommand`, `<Name>UpdateCommand` |
| `module-core:application` | `.application.<name>` | `<Name>QueryService` (`@Service`), `<Name>Mapper` (`@UtilityClass`) |
| `module-core:port` | `.port.<name>` | `<Name>Repository` (interface), `<Name>Dto`, `<Name>CreateDto`, `<Name>UpdateDto` |
| `module-adaptor:inbound:api` | `.api.<name>` | `<Name>Api` (`@RestController`), `<Name>UseCase` (`@Component`), `<Name>CreateRequest`, `<Name>Response` |
| `module-adaptor:outbound:rds` | `.rds.<name>` | `<Name>Entity`, `<Name>RepositoryImpl` (`@Repository`) |
| `module-adaptor:outbound:external` | `.external.<name>` | `<Name>FeignClient`, `<Name>ExternalRepository` |

**Responsibility split (api layer):**
- `UseCase` — business orchestration, returns `CommandResult<*Response>` (no HTTP concerns)
- `*Api` — HTTP concerns only, converts `CommandResult` → `ResponseEntity` via switch pattern matching

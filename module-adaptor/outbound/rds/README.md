# module-adaptor:outbound:rds

**RDS 아웃바운드 어댑터** 모듈입니다. `module-core:port`에 정의된 Repository 인터페이스를 JPA로 구현합니다.

## 역할

- Port Repository 인터페이스의 구현체 제공
- JPA Entity 정의 및 관리
- 데이터베이스 접근 로직 (JPA, QueryDSL)

## 구조

```
rds/
├── build.gradle
└── src/main/java/com/toy/cnr/rds/
    └── {domain}/
        ├── entity/
        │   └── {Domain}Entity.java           ← JPA 엔티티
        ├── {Domain}JpaRepository.java        ← Spring Data JPA 인터페이스
        └── {Domain}RepositoryImpl.java       ← Port Repository 구현체
```

## 의존성

```groovy
dependencies {
    implementation project(':module-core:port')

    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'
    runtimeOnly 'com.mysql:mysql-connector-j'
    runtimeOnly 'com.h2database:h2'
}
```

## 주요 클래스

### {Domain}Entity.java (JPA 엔티티)

```java
@Entity
@Table(name = "{table_name}")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FooEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    // Port DTO로부터 엔티티 생성
    public static FooEntity create(FooCreateDto from) { ... }

    // 수정 메서드
    public void update(FooUpdateDto from) { ... }

    // Port DTO로 변환
    public FooDto toDto() { ... }
}
```

- Port DTO(`{Domain}CreateDto`, `{Domain}UpdateDto`)로부터 엔티티를 생성하는 정적 팩토리 메서드 제공
- 엔티티에서 Port DTO(`{Domain}Dto`)로 변환하는 `toDto()` 메서드 제공

### {Domain}RepositoryImpl.java (Repository 구현체)

`RepositoryResult`의 static 헬퍼 메서드를 사용해 보일러플레이트를 제거합니다.

| 메서드 | 설명 |
|--------|------|
| `RepositoryResult.wrap(Supplier)` | try-catch 래핑. 예외 발생 시 `Error` 반환 |
| `RepositoryResult.ofOptional(Supplier<Optional>, msg)` | Optional → `Found` / `NotFound` 변환 + try-catch 내장 |

**Optional을 반환하는 JPA 메서드 (findById, findByXxx, update):**

```java
@Override
public RepositoryResult<FooDto> findById(Long id) {
    return RepositoryResult.ofOptional(
        () -> fooJpaRepository.findById(id).map(FooEntity::toDto),
        "Foo not found with id: " + id
    );
}

@Override
public RepositoryResult<FooDto> update(Long id, FooUpdateDto dto) {
    return RepositoryResult.ofOptional(
        () -> fooJpaRepository.findById(id).map(entity -> {
            entity.update(dto);
            return fooJpaRepository.save(entity).toDto();
        }),
        "Foo not found with id: " + id
    );
}
```

**Optional이 없는 JPA 메서드 (findAll, save, deleteById):**

```java
@Override
public RepositoryResult<List<FooDto>> findAll() {
    return RepositoryResult.wrap(() -> {
        var list = fooJpaRepository.findAll().stream()
            .map(FooEntity::toDto)
            .toList();
        return new RepositoryResult.Found<>(list);
    });
}

@Override
public RepositoryResult<Void> deleteById(Long id) {
    return RepositoryResult.wrap(() -> {
        fooJpaRepository.deleteById(id);
        return new RepositoryResult.Found<>(null);
    });
}
```

## 데이터 변환 흐름

```
[Port DTO] → {Domain}Entity.create(dto) → [엔티티 저장]
[엔티티 조회] → entity.toDto() → [Port DTO 반환]
```

## 새로운 도메인의 RDS 구현 추가

`Bar` 도메인을 추가하는 경우:

```
rds/src/main/java/com/toy/cnr/rds/
└── bar/
    ├── entity/
    │   └── BarEntity.java
    ├── BarJpaRepository.java
    └── BarRepositoryImpl.java
```

> CRUD 보일러플레이트는 `cnr-crud-generator` 스킬로 자동 생성할 수 있습니다.

# module-adaptor:outbound:external:foo-client

**Foo 외부 서비스 연동** 모듈입니다. Spring Cloud OpenFeign을 사용하여 외부 Foo 서비스와 HTTP로 통신합니다.

> 현재 **스켈레톤** 상태입니다. 외부 Foo 서비스 연동이 필요할 때 이 모듈에 구현을 추가하세요.

## 역할

- 외부 Foo 서비스에 대한 HTTP 클라이언트
- `module-core:port`의 Repository 인터페이스 구현 (외부 API 기반)

## 의존성

```groovy
dependencies {
    implementation project(':module-core:port')
    implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'
}
```

## 활용 예시

### Feign Client 정의

```java
package com.toy.cnr.external.foo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "foo-service", url = "${external.foo-service.url}")
public interface FooFeignClient {

    @GetMapping("/api/foo/{id}")
    FooExternalResponse findById(@PathVariable Long id);

    @GetMapping("/api/foo")
    List<FooExternalResponse> findAll();
}
```

### Port 구현체

```java
package com.toy.cnr.external.foo;

import com.toy.cnr.port.foo.FooRepository;
import com.toy.cnr.port.foo.model.FooDto;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class FooExternalRepository implements FooRepository {
    private final FooFeignClient fooFeignClient;

    public FooExternalRepository(FooFeignClient fooFeignClient) {
        this.fooFeignClient = fooFeignClient;
    }

    @Override
    public List<FooDto> findAll() {
        return fooFeignClient.findAll().stream()
                .map(r -> new FooDto(r.id(), r.name(), r.description()))
                .toList();
    }

    @Override
    public Optional<FooDto> findById(Long id) {
        var response = fooFeignClient.findById(id);
        return Optional.of(new FooDto(response.id(), response.name(), response.description()));
    }

    // ... 나머지 메서드 구현
}
```

## 모듈 활성화

이 모듈을 사용하려면:

1. `module-bootstrap/build.gradle`에 의존성 추가:
   ```groovy
   implementation project(':module-adaptor:outbound:external:foo-client')
   ```
2. `ApiApplication.java`에 `@EnableFeignClients` 추가
3. `application.properties`에 외부 서비스 URL 설정:
   ```properties
   external.foo-service.url=https://api.example.com
   ```

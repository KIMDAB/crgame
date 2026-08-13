# module-adaptor:inbound:event

**이벤트 인바운드 어댑터** 모듈입니다. 이벤트(메시지 큐, 애플리케이션 이벤트 등)를 수신하여 내부 비즈니스 로직을 호출합니다.

> 현재 **스켈레톤** 상태입니다. 이벤트 기반 처리가 필요할 때 이 모듈에 구현을 추가하세요.

## 역할

- 메시지 큐(Kafka, RabbitMQ 등) 이벤트 리스너
- Spring Application Event 리스너
- 비동기 이벤트 처리

## 의존성

```groovy
dependencies {
    implementation project(':module-core:application')
}
```

## 활용 예시

### Spring Application Event 리스너

```java
package com.toy.cnr.event.foo;

import com.toy.cnr.application.foo.service.FooQueryService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class FooEventListener {
    private final FooQueryService fooQueryService;

    public FooEventListener(FooQueryService fooQueryService) {
        this.fooQueryService = fooQueryService;
    }

    @EventListener
    public void handleFooCreated(FooCreatedEvent event) {
        // 이벤트 처리 로직
    }
}
```

### Kafka 이벤트 리스너 (의존성 추가 필요)

```groovy
// build.gradle에 추가
dependencies {
    implementation 'org.springframework.kafka:spring-kafka'
}
```

```java
@Component
public class FooKafkaListener {
    @KafkaListener(topics = "foo-events")
    public void listen(String message) {
        // Kafka 메시지 처리 로직
    }
}
```

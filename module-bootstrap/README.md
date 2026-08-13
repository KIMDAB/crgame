# module-bootstrap

애플리케이션의 **진입점(entry point)** 모듈입니다. Spring Boot 메인 클래스와 애플리케이션 설정을 포함합니다.

## 역할

- Spring Boot 애플리케이션 시작점 (`ApiApplication.java`)
- 전체 모듈을 조합(compose)하여 실행 가능한 단일 JAR로 패키징
- `application.properties` 등 런타임 설정 관리

## 구조

```
module-bootstrap/
├── build.gradle
└── src/main/
    ├── java/com/toy/cnr/server/
    │   └── ApiApplication.java          ← Spring Boot main class
    └── resources/
        └── application.properties       ← 애플리케이션 설정
```

## 의존성

이 모듈은 애플리케이션 실행에 필요한 모든 모듈을 조합합니다:

```groovy
dependencies {
    implementation project(':module-adaptor:inbound:api')   // REST API 엔드포인트
    implementation project(':module-core:application')      // 비즈니스 서비스
    implementation project(':module-adaptor:outbound:rds')  // 데이터베이스 구현
}
```

> **참고**: 새로운 어댑터 모듈(예: event, batch, external client)을 추가하면 이 모듈의 `build.gradle`에 의존성을 추가해야 합니다.

## 주요 파일

### ApiApplication.java

```java
@SpringBootApplication(scanBasePackages = {"com.toy.cnr"})
@EnableScheduling
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
```

- `scanBasePackages = "com.toy.cnr"`: 모든 모듈의 컴포넌트를 스캔합니다
- `@EnableScheduling`: 스케줄링 기능을 활성화합니다

## 실행 방법

```bash
# Gradle로 실행
./gradlew :module-bootstrap:bootRun

# JAR 빌드 후 실행
./gradlew :module-bootstrap:bootJar
java -jar module-bootstrap/build/libs/module-bootstrap-0.0.1-SNAPSHOT.jar
```

## 새로운 어댑터 모듈 추가 시

1. `build.gradle`에 새 모듈 의존성 추가:
   ```groovy
   implementation project(':module-adaptor:outbound:external:foo-client')
   ```
2. 해당 모듈의 컴포넌트가 `com.toy.cnr` 패키지 하위에 있으면 자동 스캔됩니다

# module-utils

프로젝트 전역에서 사용하는 **공통 유틸리티** 모듈입니다. 현재는 스켈레톤 상태입니다.

## 역할

- 여러 모듈에서 공통으로 사용하는 유틸리티 클래스 제공
- 특정 도메인이나 프레임워크에 종속되지 않는 범용 기능

## 구조

```
module-utils/
└── build.gradle
```

## 활용 예시

이 모듈에 추가하기 적합한 코드 예시:

### 날짜/시간 유틸리티

```java
package com.toy.cnr.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {
    private static final DateTimeFormatter DEFAULT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtils() {}

    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DEFAULT_FORMAT);
    }
}
```

### 문자열 유틸리티

```java
package com.toy.cnr.utils;

public final class StringUtils {
    private StringUtils() {}

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
}
```

## 다른 모듈에서 사용하기

```groovy
// 사용할 모듈의 build.gradle
dependencies {
    implementation project(':module-utils')
}
```

## 가이드라인

- **프레임워크 의존성 최소화**: 가능한 한 순수 Java만 사용
- **도메인 로직 금지**: 비즈니스 로직은 `module-core:domain`에 위치해야 합니다
- **static 메서드 선호**: 유틸리티 클래스는 `final class` + `private 생성자` + `static 메서드` 패턴 사용

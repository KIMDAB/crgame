# module-adaptor:inbound:batch

**배치 인바운드 어댑터** 모듈입니다. Spring Batch를 사용한 정기적/대량 데이터 처리 작업을 정의합니다.

> 현재 **스켈레톤** 상태입니다. 배치 작업이 필요할 때 이 모듈에 구현을 추가하세요.

## 역할

- Spring Batch Job/Step 정의
- 정기적인 대량 데이터 처리
- 스케줄러와 연동한 배치 작업 실행

## 의존성

```groovy
dependencies {
    implementation project(':module-core:application')
    implementation 'org.springframework.boot:spring-boot-starter-batch'
}
```

## 활용 예시

### 배치 Job 정의

```java
package com.toy.cnr.batch.foo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class FooBatchConfig {

    @Bean
    public Job fooProcessingJob(JobRepository jobRepository, Step fooStep) {
        return new JobBuilder("fooProcessingJob", jobRepository)
                .start(fooStep)
                .build();
    }

    @Bean
    public Step fooStep(JobRepository jobRepository,
                        PlatformTransactionManager transactionManager) {
        return new StepBuilder("fooStep", jobRepository)
                .<String, String>chunk(100, transactionManager)
                .reader(/* ItemReader */)
                .processor(/* ItemProcessor */)
                .writer(/* ItemWriter */)
                .build();
    }
}
```

## 모듈 활성화

배치 모듈을 사용하려면 `module-bootstrap/build.gradle`에 의존성 추가가 필요합니다:

```groovy
implementation project(':module-adaptor:inbound:batch')
```

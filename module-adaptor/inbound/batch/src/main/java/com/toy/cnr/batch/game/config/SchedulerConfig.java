package com.toy.cnr.batch.game.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Scheduling 활성화 설정.
 * <p>
 * @Scheduled 어노테이션이 동작하도록 EnableScheduling을 선언합니다.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}

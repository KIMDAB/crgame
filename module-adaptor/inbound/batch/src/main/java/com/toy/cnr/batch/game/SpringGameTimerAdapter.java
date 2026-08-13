package com.toy.cnr.batch.game;

import com.toy.cnr.port.game.GameTimerPort;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * ScheduledExecutorService 기반 게임 타이머 구현.
 * <p>
 * 게임 ID별로 개별 타이머를 관리합니다.
 * 등록된 taskId를 통해 취소가 가능합니다.
 */
@Component
public class SpringGameTimerAdapter implements GameTimerPort {

    private static final int THREAD_POOL_SIZE = 4;

    private final ScheduledExecutorService executor =
        Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

    private final ConcurrentHashMap<String, ScheduledFuture<?>> tasks =
        new ConcurrentHashMap<>();

    @Override
    public void schedule(String taskId, long delayMs, Runnable task) {
        var future = executor.schedule(
            () -> {
                try {
                    task.run();
                } finally {
                    tasks.remove(taskId);
                }
            },
            delayMs,
            TimeUnit.MILLISECONDS
        );
        tasks.put(taskId, future);
    }

    @Override
    public void cancel(String taskId) {
        var future = tasks.remove(taskId);
        if (future != null) {
            future.cancel(false);
        }
    }
}

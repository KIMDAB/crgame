package com.toy.cnr.port.game;

/**
 * 게임 라이프사이클 타이머 포트 인터페이스.
 * <p>
 * 구현체(SpringGameTimerAdapter)는 batch 모듈에 위치하며,
 * ScheduledExecutorService로 per-game 개별 타이머를 관리합니다.
 */
public interface GameTimerPort {

    /**
     * 지정된 지연 후 task를 한 번 실행합니다.
     *
     * @param taskId  취소에 사용할 고유 ID (예: "escape:{gameId}", "end:{gameId}")
     * @param delayMs 지연 시간 (밀리초)
     * @param task    실행할 작업
     */
    void schedule(String taskId, long delayMs, Runnable task);

    /**
     * 예약된 작업을 취소합니다. 이미 실행된 경우 무시합니다.
     *
     * @param taskId 취소할 작업 ID
     */
    void cancel(String taskId);
}

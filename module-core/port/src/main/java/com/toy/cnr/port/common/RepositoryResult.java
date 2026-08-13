package com.toy.cnr.port.common;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Repository 계층의 결과를 표현하는 sealed 인터페이스.
 * <p>
 * 예외를 던지는 대신 타입으로 성공/실패를 표현하여,
 * 컴파일 타임에 모든 케이스를 처리하도록 강제합니다.
 * <p>
 * 사용 예시:
 * <pre>{@code
 * // switch 패턴 매칭
 * return switch (result) {
 *     case RepositoryResult.Found(var data) -> handleSuccess(data);
 *     case RepositoryResult.NotFound(var msg) -> handleNotFound(msg);
 *     case RepositoryResult.Error(var t) -> handleError(t);
 * };
 *
 * // map으로 성공 데이터 변환 (실패는 자동 전파)
 * RepositoryResult<Foo> result = repository.findById(id).map(FooMapper::toDomain);
 * }</pre>
 *
 * @param <T> 조회/저장 결과 데이터 타입
 */
public sealed interface RepositoryResult<T>
    permits RepositoryResult.Found, RepositoryResult.NotFound, RepositoryResult.Error {

    /**
     * 성공 데이터를 변환합니다. 실패 케이스는 타입만 변경되어 그대로 전파됩니다.
     *
     * @param mapper 성공 데이터 변환 함수
     * @param <R>    변환된 타입
     * @return 변환된 RepositoryResult
     */
    default <R> RepositoryResult<R> map(Function<T, R> mapper) {
        return switch (this) {
            case Found(var data) -> new Found<>(mapper.apply(data));
            case NotFound(var msg) -> new NotFound<>(msg);
            case Error(var t) -> new Error<>(t);
        };
    }

    /**
     * try-catch 보일러플레이트를 제거합니다.
     * action 실행 중 예외가 발생하면 {@link Error}로 감쌉니다.
     *
     * @param action RepositoryResult를 반환하는 작업
     * @param <T>    결과 데이터 타입
     * @return 작업 결과 또는 Error
     */
    static <T> RepositoryResult<T> wrap(Supplier<RepositoryResult<T>> action) {
        try {
            return action.get();
        } catch (Exception e) {
            return new Error<>(e);
        }
    }

    /**
     * Optional을 반환하는 작업을 실행하고 Found / NotFound / Error로 변환합니다.
     * try-catch와 Optional 매핑을 한 번에 처리합니다.
     *
     * @param supplier         Optional을 반환하는 작업
     * @param notFoundMessage  찾지 못했을 때 메시지
     * @param <T>              결과 데이터 타입
     * @return Found, NotFound, 또는 Error
     */
    static <T> RepositoryResult<T> ofOptional(Supplier<Optional<T>> supplier, String notFoundMessage) {
        try {
            return supplier.get()
                .map(data -> (RepositoryResult<T>) new Found<>(data))
                .orElse(new NotFound<>(notFoundMessage));
        } catch (Exception e) {
            return new Error<>(e);
        }
    }

    /**
     * 데이터를 성공적으로 찾은 경우.
     */
    record Found<T>(T data) implements RepositoryResult<T> {}

    /**
     * 요청한 데이터를 찾지 못한 경우.
     */
    record NotFound<T>(String message) implements RepositoryResult<T> {}

    /**
     * 저장소 접근 중 오류가 발생한 경우.
     */
    record Error<T>(Throwable t) implements RepositoryResult<T> {}
}

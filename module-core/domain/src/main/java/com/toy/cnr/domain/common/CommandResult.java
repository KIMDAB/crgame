package com.toy.cnr.domain.common;

import lombok.Builder;

import java.util.List;
import java.util.function.Function;

/**
 * 서비스/애플리케이션 계층의 명령 실행 결과를 표현하는 sealed 인터페이스.
 * <p>
 * 예외를 던지는 대신 타입으로 성공/실패를 표현하여,
 * 호출자가 switch 패턴 매칭으로 모든 케이스를 명시적으로 처리하도록 강제합니다.
 * <p>
 * {@link #map(Function)}, {@link #flatMap(Function)} 메서드를 통해
 * 성공 데이터를 변환하면서 실패 케이스를 자동으로 전파할 수 있습니다.
 * <p>
 * 사용 예시:
 * <pre>{@code
 * // switch 패턴 매칭 (모든 케이스를 직접 처리할 때)
 * return switch (result) {
 *     case CommandResult.Success(var data, var msg) -> ResponseEntity.ok(data);
 *     case CommandResult.ValidationError(var errors) -> ResponseEntity.badRequest().body(errors);
 *     case CommandResult.BusinessError(var reason) -> ResponseEntity.status(404).body(reason);
 * };
 *
 * // map (성공 데이터를 변환, 실패는 자동 전파)
 * CommandResult<FooResponse> result = service.findById(id).map(FooResponse::from);
 *
 * // flatMap (변환 결과가 다시 CommandResult일 때)
 * CommandResult<Bar> result = service.findFoo(id).flatMap(foo -> service.toBar(foo));
 * }</pre>
 *
 * @param <T> 성공 시 반환할 데이터 타입
 */
public sealed interface CommandResult<T>
    permits CommandResult.Success, CommandResult.ValidationError, CommandResult.BusinessError {

    /**
     * 성공 데이터를 변환합니다. 실패 케이스는 타입만 변경되어 그대로 전파됩니다.
     * <p>
     * {@code Optional.map()}과 동일한 패턴입니다.
     *
     * @param mapper 성공 데이터 변환 함수
     * @param <R>    변환된 타입
     * @return 변환된 CommandResult
     */
    default <R> CommandResult<R> map(Function<T, R> mapper) {
        return switch (this) {
            case Success(var data, var msg) -> new Success<>(mapper.apply(data), msg);
            case ValidationError(var errors) -> new ValidationError<>(errors);
            case BusinessError(var reason) -> new BusinessError<>(reason);
        };
    }

    /**
     * 성공 데이터를 CommandResult를 반환하는 함수로 변환합니다.
     * 실패 케이스는 타입만 변경되어 그대로 전파됩니다.
     * <p>
     * {@code Optional.flatMap()}과 동일한 패턴입니다.
     * 연속된 CommandResult 연산을 체이닝할 때 사용합니다.
     *
     * @param mapper 성공 데이터를 CommandResult로 변환하는 함수
     * @param <R>    변환된 타입
     * @return 변환된 CommandResult
     */
    default <R> CommandResult<R> flatMap(Function<T, CommandResult<R>> mapper) {
        return switch (this) {
            case Success(var data, var msg) -> mapper.apply(data);
            case ValidationError(var errors) -> new ValidationError<>(errors);
            case BusinessError(var reason) -> new BusinessError<>(reason);
        };
    }

    /**
     * 명령이 성공적으로 수행된 경우.
     *
     * @param data    결과 데이터
     * @param message 성공 메시지 (선택적, null 가능)
     */
    @Builder
    record Success<T>(T data, String message) implements CommandResult<T> {}

    /**
     * 입력 데이터 검증에 실패한 경우.
     *
     * @param errors 검증 오류 목록
     */
    @Builder
    record ValidationError<T>(List<String> errors) implements CommandResult<T> {}

    /**
     * 비즈니스 규칙 위반 또는 리소스를 찾지 못한 경우.
     *
     * @param reason 실패 사유
     */
    @Builder
    record BusinessError<T>(String reason) implements CommandResult<T> {}
}

package com.toy.cnr.api.common.util;

import com.toy.cnr.api.common.error.ApiError;
import com.toy.cnr.api.common.error.ApiErrorResponse;
import com.toy.cnr.domain.common.CommandResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * {@link CommandResult} sealed 타입을 {@link ResponseEntity}로 변환하는 유틸리티.
 * <p>
 * Controller에서 공통으로 사용하며, switch 패턴 매칭으로 모든 케이스를 처리합니다.
 * <p>
 * 사용 예시:
 * <pre>{@code
 * // 200 OK + body
 * return ResponseMapper.toResponseEntity(useCase.findById(id));
 *
 * // 204 No Content (delete 등)
 * return ResponseMapper.toNoContentResponse(useCase.delete(id));
 * }</pre>
 */
public final class ResponseMapper {

    private ResponseMapper() {}

    /**
     * CommandResult를 ResponseEntity(200 OK)로 변환합니다.
     */
    public static <T> ResponseEntity<T> toResponseEntity(CommandResult<T> result) {
        return switch (result) {
            case CommandResult.Success(var data, var msg) ->
                ResponseEntity.ok(data);
            case CommandResult.ValidationError(var errors) ->
                errorResponse(HttpStatus.BAD_REQUEST, errors);
            case CommandResult.BusinessError(var reason) ->
                errorResponse(HttpStatus.NOT_FOUND, reason);
        };
    }

    /**
     * CommandResult를 ResponseEntity(201 Created)로 변환합니다.
     * 리소스 생성 성공 시 BusinessError는 409 Conflict로 응답합니다.
     */
    public static <T> ResponseEntity<T> toCreatedResponseEntity(CommandResult<T> result) {
        return switch (result) {
            case CommandResult.Success(var data, var msg) ->
                ResponseEntity.status(HttpStatus.CREATED).body(data);
            case CommandResult.ValidationError(var errors) ->
                errorResponse(HttpStatus.BAD_REQUEST, errors);
            case CommandResult.BusinessError(var reason) ->
                conflictResponse(reason);
        };
    }

    /**
     * CommandResult를 ResponseEntity(204 No Content)로 변환합니다.
     * delete 등 본문 없이 성공을 응답하는 경우에 사용합니다.
     */
    public static <T> ResponseEntity<Void> toNoContentResponse(CommandResult<T> result) {
        return switch (result) {
            case CommandResult.Success(var data, var msg) ->
                ResponseEntity.noContent().build();
            case CommandResult.ValidationError(var errors) ->
                errorResponse(HttpStatus.BAD_REQUEST, errors);
            case CommandResult.BusinessError(var reason) ->
                errorResponse(HttpStatus.NOT_FOUND, reason);
        };
    }

    @SuppressWarnings("unchecked")
    private static <T> ResponseEntity<T> errorResponse(
        HttpStatus status,
        java.util.List<String> errors
    ) {
        var body = ApiErrorResponse.from(
            new ApiError.BadRequest("Validation failed", errors)
        );
        return (ResponseEntity<T>) ResponseEntity.status(status).body(body);
    }

    @SuppressWarnings("unchecked")
    private static <T> ResponseEntity<T> errorResponse(
        HttpStatus status,
        String reason
    ) {
        var body = ApiErrorResponse.from(
            new ApiError.NotFound("resource", reason)
        );
        return (ResponseEntity<T>) ResponseEntity.status(status).body(body);
    }

    @SuppressWarnings("unchecked")
    private static <T> ResponseEntity<T> conflictResponse(String reason) {
        var body = ApiErrorResponse.from(new ApiError.Conflict(reason));
        return (ResponseEntity<T>) ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}

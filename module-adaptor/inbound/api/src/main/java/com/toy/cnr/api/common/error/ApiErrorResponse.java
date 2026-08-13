package com.toy.cnr.api.common.error;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 에러 응답 JSON 구조를 정의하는 record.
 * <p>
 * {@link ApiError} sealed 타입을 switch 패턴 매칭으로 변환하여
 * 일관된 에러 응답 형식을 제공합니다.
 * <p>
 * 응답 예시:
 * <pre>{@code
 * {
 *   "errorType": "NOT_FOUND",
 *   "message": "Foo not found with id: 1",
 *   "details": [],
 *   "timestamp": "2026-02-08T12:00:00"
 * }
 * }</pre>
 */
public record ApiErrorResponse(
    String errorType,
    String message,
    List<String> details,
    LocalDateTime timestamp
) {
    /**
     * ApiError sealed 타입을 ApiErrorResponse로 변환합니다.
     * switch 패턴 매칭으로 모든 에러 유형을 빠짐없이 처리합니다.
     */
    public static ApiErrorResponse from(ApiError error) {
        return switch (error) {
            case ApiError.NotFound(var resource, var msg) ->
                new ApiErrorResponse(
                    "NOT_FOUND",
                    msg,
                    List.of("resource: " + resource),
                    LocalDateTime.now()
                );
            case ApiError.BadRequest(var msg, var details) ->
                new ApiErrorResponse(
                    "BAD_REQUEST",
                    msg,
                    details,
                    LocalDateTime.now()
                );
            case ApiError.Conflict(var msg) ->
                new ApiErrorResponse(
                    "CONFLICT",
                    msg,
                    List.of(),
                    LocalDateTime.now()
                );
            case ApiError.InternalError(var msg) ->
                new ApiErrorResponse(
                    "INTERNAL_ERROR",
                    msg,
                    List.of(),
                    LocalDateTime.now()
                );
        };
    }
}

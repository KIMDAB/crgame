package com.toy.cnr.api.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * 전역 예외 처리기.
 * <p>
 * 컨트롤러에서 처리되지 않은 예외를 {@link ApiError} sealed 타입으로 변환하여
 * 일관된 {@link ApiErrorResponse} 형식으로 응답합니다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * NoSuchElementException -> 404 NOT_FOUND
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
        NoSuchElementException ex
    ) {
        var error = new ApiError.NotFound("resource", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse.from(error));
    }

    /**
     * @Valid 검증 실패 -> 400 BAD_REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex
    ) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
            .map(field -> field.getField() + ": " + field.getDefaultMessage())
            .toList();
        var error = new ApiError.BadRequest("Validation failed", errors);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse.from(error));
    }

    /**
     * IllegalArgumentException -> 400 BAD_REQUEST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
        IllegalArgumentException ex
    ) {
        var error = new ApiError.BadRequest(ex.getMessage(), java.util.List.of());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse.from(error));
    }

    /**
     * 그 외 모든 예외 -> 500 INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleInternalError(Exception ex) {
        var error = new ApiError.InternalError(ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiErrorResponse.from(error));
    }
}

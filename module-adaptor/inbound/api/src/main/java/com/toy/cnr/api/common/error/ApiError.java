package com.toy.cnr.api.common.error;

import java.util.List;

/**
 * API 에러를 표현하는 sealed 인터페이스.
 * <p>
 * 모든 API 에러 유형을 타입으로 정의하여,
 * switch 패턴 매칭으로 각 에러에 맞는 HTTP 상태 코드와 응답을 생성합니다.
 * <p>
 * 사용 예시:
 * <pre>{@code
 * ApiError error = new ApiError.NotFound("Foo", "Foo not found with id: 1");
 * ApiErrorResponse response = ApiErrorResponse.from(error);
 * }</pre>
 */
public sealed interface ApiError
    permits ApiError.NotFound, ApiError.BadRequest, ApiError.Conflict, ApiError.InternalError {

    String message();

    /**
     * 요청한 리소스를 찾지 못한 경우 (HTTP 404).
     *
     * @param resource 리소스 종류 (예: "Foo", "User")
     * @param message  상세 메시지
     */
    record NotFound(String resource, String message) implements ApiError {}

    /**
     * 클라이언트 요청이 유효하지 않은 경우 (HTTP 400).
     *
     * @param message 에러 메시지
     * @param details 상세 검증 오류 목록
     */
    record BadRequest(String message, List<String> details) implements ApiError {}

    /**
     * 리소스가 이미 존재하는 경우 (HTTP 409).
     *
     * @param message 에러 메시지
     */
    record Conflict(String message) implements ApiError {}

    /**
     * 서버 내부 오류가 발생한 경우 (HTTP 500).
     *
     * @param message 에러 메시지
     */
    record InternalError(String message) implements ApiError {}
}

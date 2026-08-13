package com.toy.cnr.api.foo.usecase;

import com.toy.cnr.api.foo.request.FooCreateRequest;
import com.toy.cnr.api.foo.request.FooUpdateRequest;
import com.toy.cnr.api.foo.response.FooResponse;
import com.toy.cnr.application.foo.service.FooQueryService;
import com.toy.cnr.domain.common.CommandResult;
import java.util.Collection;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Foo 도메인 유즈케이스.
 * <p>
 * 비즈니스 로직을 오케스트레이션하고 응답 표현 객체({@link FooResponse})를 생성합니다.
 * HTTP 관심사({@code ResponseEntity}, 상태 코드 등)는 Controller가 담당합니다.
 * <p>
 * {@link CommandResult#map(java.util.function.Function)}을 활용하여
 * 성공 데이터만 변환하고, 실패 케이스는 자동으로 전파합니다.
 */
@Component
public class FooUseCase {

    private final FooQueryService fooQueryService;

    public FooUseCase(FooQueryService fooQueryService) {
        this.fooQueryService = fooQueryService;
    }

    /**
     * UseCase: 모든 Foo 엔티티를 조회합니다.
     */
    public CommandResult<List<FooResponse>> findAll() {
        return fooQueryService.findAll()
            .map(fooList -> fooList.stream().map(FooResponse::from).toList());
    }

    /**
     * UseCase: ID로 Foo 엔티티를 조회하고 비즈니스 로직을 적용합니다.
     */
    public CommandResult<FooResponse> findFooWithBusinessLogic(Long id) {
        return fooQueryService.findById(id)
            .map(fooQueryService::doBusinessLogic)
            .map(FooResponse::from);
    }

    /**
     * UseCase: 새로운 Foo 엔티티를 생성합니다.
     */
    public CommandResult<FooResponse> create(FooCreateRequest request) {
        return fooQueryService.create(request.toCommand())
            .map(FooResponse::from);
    }

    /**
     * UseCase: 기존 Foo 엔티티를 업데이트합니다.
     */
    public CommandResult<FooResponse> update(Long id, FooUpdateRequest request) {
        return fooQueryService.update(id, request.toCommand())
            .map(FooResponse::from);
    }

    /**
     * UseCase: ID로 Foo 엔티티를 삭제합니다.
     */
    public CommandResult<Void> delete(Long id) {
        return fooQueryService.delete(id);
    }
}

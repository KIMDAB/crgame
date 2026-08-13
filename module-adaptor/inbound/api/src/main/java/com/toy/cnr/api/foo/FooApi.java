package com.toy.cnr.api.foo;

import com.toy.cnr.api.common.util.ResponseMapper;
import com.toy.cnr.api.foo.request.FooCreateRequest;
import com.toy.cnr.api.foo.request.FooUpdateRequest;
import com.toy.cnr.api.foo.response.FooResponse;
import com.toy.cnr.api.foo.usecase.FooUseCase;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Hidden
@Tag(name = "Foo", description = "Foo 도메인 CRUD API")
@RestController
@RequestMapping("/v1/foo")
public class FooApi {

    private final FooUseCase fooUseCase;

    public FooApi(FooUseCase fooUseCase) {
        this.fooUseCase = fooUseCase;
    }

    @Operation(summary = "전체 Foo 조회", description = "등록된 모든 Foo 엔티티를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<FooResponse>> findAll() {
        return ResponseMapper.toResponseEntity(fooUseCase.findAll());
    }

    @Operation(summary = "Foo 단건 조회", description = "ID로 Foo 엔티티를 조회하고 비즈니스 로직을 적용합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<FooResponse> findById(
        @Parameter(description = "Foo ID", example = "1") @PathVariable Long id
    ) {
        return ResponseMapper.toResponseEntity(
            fooUseCase.findFooWithBusinessLogic(id)
        );
    }

    @Operation(summary = "Foo 생성", description = "새로운 Foo 엔티티를 생성합니다.")
    @PostMapping
    public ResponseEntity<FooResponse> create(
        @RequestBody FooCreateRequest request
    ) {
        return ResponseMapper.toResponseEntity(fooUseCase.create(request));
    }

    @Operation(summary = "Foo 수정", description = "기존 Foo 엔티티를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<FooResponse> update(
        @Parameter(description = "Foo ID", example = "1") @PathVariable Long id,
        @RequestBody FooUpdateRequest request
    ) {
        return ResponseMapper.toResponseEntity(
            fooUseCase.update(id, request)
        );
    }

    @Operation(summary = "Foo 삭제", description = "ID로 Foo 엔티티를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @Parameter(description = "Foo ID", example = "1") @PathVariable Long id
    ) {
        return ResponseMapper.toNoContentResponse(fooUseCase.delete(id));
    }
}

package com.toy.cnr.api.foo.response;

import com.toy.cnr.domain.foo.Foo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Foo 응답")
public record FooResponse(
    @Schema(description = "Foo ID", example = "1")
    Long id,

    @Schema(description = "이름", example = "Foo 1")
    String name,

    @Schema(description = "설명", example = "첫 번째 Foo")
    String description
) {
    public static FooResponse from(Foo foo) {
        return new FooResponse(
            foo.id(),
            foo.name(),
            foo.description()
        );
    }
}

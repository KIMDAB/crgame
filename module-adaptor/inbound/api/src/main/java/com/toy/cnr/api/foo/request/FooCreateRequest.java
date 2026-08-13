package com.toy.cnr.api.foo.request;

import com.toy.cnr.domain.foo.FooCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Foo 생성 요청")
public record FooCreateRequest(
    @Schema(description = "이름", example = "New Foo")
    String name,

    @Schema(description = "설명", example = "새로운 Foo 설명")
    String description
) {
    public FooCreateCommand toCommand() {
        return new FooCreateCommand(this.name, this.description);
    }
}

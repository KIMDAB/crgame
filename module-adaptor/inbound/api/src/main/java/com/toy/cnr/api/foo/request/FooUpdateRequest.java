package com.toy.cnr.api.foo.request;

import com.toy.cnr.domain.foo.FooUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Foo 수정 요청")
public record FooUpdateRequest(
    @Schema(description = "이름", example = "Updated Foo")
    String name,

    @Schema(description = "설명", example = "수정된 Foo 설명")
    String description
) {
    public FooUpdateCommand toCommand() {
        return new FooUpdateCommand(this.name, this.description);
    }
}

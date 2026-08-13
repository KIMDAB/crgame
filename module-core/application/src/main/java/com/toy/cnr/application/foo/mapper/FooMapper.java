package com.toy.cnr.application.foo.mapper;

import com.toy.cnr.domain.foo.Foo;
import com.toy.cnr.domain.foo.FooCreateCommand;
import com.toy.cnr.domain.foo.FooUpdateCommand;
import com.toy.cnr.port.foo.model.FooCreateDto;
import com.toy.cnr.port.foo.model.FooDto;
import com.toy.cnr.port.foo.model.FooUpdateDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FooMapper {

    public static Foo toDomain(FooDto dto) {
        return new Foo(
            dto.id(),
            dto.name(),
            dto.description()
        );
    }

    public static FooCreateDto toExternal(FooCreateCommand command) {
        return new FooCreateDto(
            command.name(),
            command.description()
        );
    }

    public static FooUpdateDto toExternal(FooUpdateCommand command) {
        return new FooUpdateDto(
            command.name(),
            command.description()
        );
    }
}

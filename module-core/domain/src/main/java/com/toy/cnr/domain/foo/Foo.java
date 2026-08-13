package com.toy.cnr.domain.foo;

public record Foo(
    Long id,
    String name,
    String description
) {
    public Foo fooBusinessLogic(Number arg) {
        return new Foo(
            this.id,
            this.name + "_processed" + arg,
            this.description
        );
    }
}

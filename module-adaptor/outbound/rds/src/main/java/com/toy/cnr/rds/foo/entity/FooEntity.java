package com.toy.cnr.rds.foo.entity;

import com.toy.cnr.port.foo.model.FooCreateDto;
import com.toy.cnr.port.foo.model.FooDto;
import com.toy.cnr.port.foo.model.FooUpdateDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "foo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FooEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    private FooEntity(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static FooEntity create(FooCreateDto from) {
        return new FooEntity(null, from.name(), from.description());
    }

    public void update(FooUpdateDto from) {
        this.name = from.name();
        this.description = from.description();
    }

    public FooDto toDto() {
        return new FooDto(id, name, description);
    }
}

package com.toy.cnr.rds.foo;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.foo.FooRepository;
import com.toy.cnr.port.foo.model.FooCreateDto;
import com.toy.cnr.port.foo.model.FooDto;
import com.toy.cnr.port.foo.model.FooUpdateDto;
import com.toy.cnr.rds.foo.entity.FooEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FooRepositoryImpl implements FooRepository {

    private final FooJpaRepository fooJpaRepository;

    public FooRepositoryImpl(FooJpaRepository fooJpaRepository) {
        this.fooJpaRepository = fooJpaRepository;
    }

    @Override
    public RepositoryResult<List<FooDto>> findAll() {
        return RepositoryResult.wrap(() -> {
            var list = fooJpaRepository.findAll().stream()
                .map(FooEntity::toDto)
                .toList();
            return new RepositoryResult.Found<>(list);
        });
    }

    @Override
    public RepositoryResult<FooDto> findById(Long id) {
        return RepositoryResult.ofOptional(
            () -> fooJpaRepository.findById(id).map(FooEntity::toDto),
            "Foo not found with id: " + id
        );
    }

    @Override
    public RepositoryResult<FooDto> save(FooCreateDto dto) {
        return RepositoryResult.wrap(() -> {
            var saved = fooJpaRepository.save(FooEntity.create(dto));
            return new RepositoryResult.Found<>(saved.toDto());
        });
    }

    @Override
    public RepositoryResult<FooDto> update(Long id, FooUpdateDto dto) {
        return RepositoryResult.ofOptional(
            () -> fooJpaRepository.findById(id).map(entity -> {
                entity.update(dto);
                return fooJpaRepository.save(entity).toDto();
            }),
            "Foo not found with id: " + id
        );
    }

    @Override
    public RepositoryResult<Void> deleteById(Long id) {
        return RepositoryResult.wrap(() -> {
            fooJpaRepository.deleteById(id);
            return new RepositoryResult.Found<>(null);
        });
    }
}

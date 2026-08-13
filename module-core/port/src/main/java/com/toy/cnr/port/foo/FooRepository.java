package com.toy.cnr.port.foo;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.foo.model.FooCreateDto;
import com.toy.cnr.port.foo.model.FooDto;
import com.toy.cnr.port.foo.model.FooUpdateDto;

import java.util.List;

public interface FooRepository {
    RepositoryResult<List<FooDto>> findAll();
    RepositoryResult<FooDto> findById(Long id);
    RepositoryResult<FooDto> save(FooCreateDto dto);
    RepositoryResult<FooDto> update(Long id, FooUpdateDto dto);
    RepositoryResult<Void> deleteById(Long id);
}

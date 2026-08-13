package com.toy.cnr.application.foo.service;

import com.toy.cnr.application.common.ResultMapper;
import com.toy.cnr.application.foo.mapper.FooMapper;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.foo.Foo;
import com.toy.cnr.domain.foo.FooCreateCommand;
import com.toy.cnr.domain.foo.FooUpdateCommand;
import com.toy.cnr.port.foo.FooRepository;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class FooQueryService {

    private static final int FOO_BUSINESS_LOGIC_NUMBER = 9999; // for example

    private final FooRepository fooRepository;

    public FooQueryService(FooRepository fooRepository) {
        this.fooRepository = fooRepository;
    }

    public CommandResult<List<Foo>> findAll() {
        return ResultMapper.toCommandResult(
            fooRepository.findAll()
                .map(list -> list.stream().map(FooMapper::toDomain).toList())
        );
    }

    @Cacheable(cacheNames = {"FooCache"}, key = "#id")
    public CommandResult<Foo> findById(Long id) {
        return ResultMapper.toCommandResult(fooRepository.findById(id))
            .map(FooMapper::toDomain);
    }

    public Foo doBusinessLogic(Foo foo) {
        return foo.fooBusinessLogic(FOO_BUSINESS_LOGIC_NUMBER);
    }

    public CommandResult<Foo> create(FooCreateCommand command) {
        return ResultMapper.toCommandResult(fooRepository.save(FooMapper.toExternal(command)))
            .map(FooMapper::toDomain);
    }

    public CommandResult<Foo> update(Long id, FooUpdateCommand command) {
        return ResultMapper.toCommandResult(fooRepository.update(id, FooMapper.toExternal(command)))
            .map(FooMapper::toDomain);
    }

    public CommandResult<Void> delete(Long id) {
        return ResultMapper.toCommandResult(fooRepository.deleteById(id));
    }
}

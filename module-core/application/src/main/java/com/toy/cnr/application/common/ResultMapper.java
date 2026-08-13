package com.toy.cnr.application.common;

import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.port.common.RepositoryResult;

import java.util.function.Function;

/**
 * {@link RepositoryResult}를 {@link CommandResult}로 변환하는 유틸리티.
 * <p>
 * application 모듈은 domain(CommandResult)과 port(RepositoryResult) 모두에 의존하므로,
 * 두 sealed 타입 간의 변환을 이 유틸리티가 담당합니다.
 * <p>
 * 사용 예시:
 * <pre>{@code
 * // 단순 변환
 * return ResultMapper.toCommandResult(fooRepository.findById(id))
 *     .map(FooMapper::toDomain);
 *
 * // mapper를 함께 전달
 * return ResultMapper.toCommandResult(fooRepository.findById(id), FooMapper::toDomain);
 * }</pre>
 */
public final class ResultMapper {

    private ResultMapper() {}

    /**
     * RepositoryResult를 CommandResult로 변환합니다.
     * Found → Success, NotFound → BusinessError, Error → BusinessError
     */
    public static <T> CommandResult<T> toCommandResult(RepositoryResult<T> result) {
        return switch (result) {
            case RepositoryResult.Found(var data) ->
                new CommandResult.Success<>(data, null);
            case RepositoryResult.NotFound(var msg) ->
                new CommandResult.BusinessError<>(msg);
            case RepositoryResult.Error(var e) ->
                new CommandResult.BusinessError<>(e.getMessage());
        };
    }
}

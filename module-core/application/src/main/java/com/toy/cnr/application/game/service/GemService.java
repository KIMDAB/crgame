package com.toy.cnr.application.game.service;

import com.toy.cnr.application.common.ResultMapper;
import com.toy.cnr.application.game.mapper.GemMapper;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.game.Gem;
import com.toy.cnr.port.game.GemStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 보석 조회 서비스.
 */
@Service
public class GemService {

    private final GemStore gemStore;

    public GemService(GemStore gemStore) {
        this.gemStore = gemStore;
    }

    public CommandResult<List<Gem>> getAvailableGems(String gameId) {
        return ResultMapper.toCommandResult(gemStore.getAllGems(gameId))
            .map(dtos -> dtos.stream()
                .map(GemMapper::toDomain)
                .filter(gem -> gem.status() == com.toy.cnr.domain.game.GemStatus.AVAILABLE)
                .toList()
            );
    }

    public CommandResult<List<Gem>> getAllGems(String gameId) {
        return ResultMapper.toCommandResult(gemStore.getAllGems(gameId))
            .map(dtos -> dtos.stream().map(GemMapper::toDomain).toList());
    }
}

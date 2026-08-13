package com.toy.cnr.application.game.service;

import com.toy.cnr.application.common.ResultMapper;
import com.toy.cnr.application.game.mapper.GemMapper;
import com.toy.cnr.application.game.mapper.InGamePlayerMapper;
import com.toy.cnr.application.room.mapper.RoomMapper;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.game.*;
import com.toy.cnr.domain.room.RoomPlayer;
import com.toy.cnr.domain.room.RoomSettings;
import com.toy.cnr.domain.room.GeoPoint;
import com.toy.cnr.port.game.*;
import com.toy.cnr.port.game.model.GameStateDto;
import com.toy.cnr.port.game.model.InGamePlayerDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 인게임 액션 서비스.
 * <p>
 * 게임 시작(역할 배정), 체포, 구출, 보석 획득, 핑 전송을 담당합니다.
 */
@Service
public class GameActionService {

    private final GameStateStore gameStateStore;
    private final InGamePlayerStore inGamePlayerStore;
    private final GemStore gemStore;
    private final LocationStore locationStore;
    private final GameEventService gameEventService;
    private final GameRegistryStore gameRegistryStore;
    private final GameTimerService gameTimerService;
    private final GemSpawnService gemSpawnService;

    public GameActionService(
        GameStateStore gameStateStore,
        InGamePlayerStore inGamePlayerStore,
        GemStore gemStore,
        LocationStore locationStore,
        GameEventService gameEventService,
        GameRegistryStore gameRegistryStore,
        GameTimerService gameTimerService,
        GemSpawnService gemSpawnService
    ) {
        this.gameStateStore = gameStateStore;
        this.inGamePlayerStore = inGamePlayerStore;
        this.gemStore = gemStore;
        this.locationStore = locationStore;
        this.gameEventService = gameEventService;
        this.gameRegistryStore = gameRegistryStore;
        this.gameTimerService = gameTimerService;
        this.gemSpawnService = gemSpawnService;
    }

    /**
     * 게임 시작: gameId 생성, 역할 배정, GameState/InGamePlayer 저장, 이벤트 발행.
     */
    public CommandResult<String> startGame(String roomId, RoomSettings settings, List<RoomPlayer> players) {
        var gameId = UUID.randomUUID().toString();
        var now = System.currentTimeMillis();
        var endsAt = now + (long) settings.gameDurationMinutes() * 60 * 1000;

        // Save GameState (ESCAPE_PHASE)
        var gameState = new GameStateDto(
            gameId, roomId, GameStatus.ESCAPE_PHASE.name(),
            RoomMapper.toSettingsDto(settings), now, endsAt
        );
        var saveStateResult = gameStateStore.saveGameState(gameState);
        if (saveStateResult instanceof com.toy.cnr.port.common.RepositoryResult.Error<Void> e) {
            return new CommandResult.BusinessError<>(e.t().getMessage());
        }

        // Assign roles randomly
        var shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled);
        int copsCount = settings.copsCount();

        for (int i = 0; i < shuffled.size(); i++) {
            var player = shuffled.get(i);
            var role = i < copsCount ? PlayerRole.POLICE : PlayerRole.THIEF;
            var inGamePlayer = new InGamePlayerDto(
                player.playerId(),
                player.playerName(),
                role.name(),
                PlayerStatus.ACTIVE.name(),
                0, 0, 0, 0,
                now
            );
            inGamePlayerStore.savePlayer(gameId, inGamePlayer);

            // Publish role assigned event
            gameEventService.publish(new GameEvent.RoleAssigned(
                gameId, player.playerId(), role.name(), now
            ));
        }

        // Publish game started event
        gameEventService.publish(new GameEvent.GameStarted(gameId, now));

        // Register in active game registry
        gameRegistryStore.register(gameId);

        // Schedule lifecycle timers
        long escapeMs = (long) settings.escapeTimeMinutes() * 60 * 1000;
        long totalMs = (long) settings.gameDurationMinutes() * 60 * 1000;
        gameTimerService.scheduleGame(gameId, escapeMs, totalMs);

        // Spawn initial gems inside playArea polygon
        var playArea = settings.mapZone() != null ? settings.mapZone().playArea() : null;
        if (playArea != null && !playArea.isEmpty()) {
            gemSpawnService.spawnInitialGems(gameId, playArea);
        }

        return new CommandResult.Success<>(gameId, "Game started");
    }

    /**
     * 도둑 체포 (경찰 전용).
     */
    public CommandResult<Void> arrest(ArrestCommand command) {
        // Validate cops
        var copsResult = inGamePlayerStore.getPlayer(command.gameId(), command.copsId());
        return ResultMapper.toCommandResult(copsResult).flatMap(copsDto -> {
            if (!copsDto.role().equals(PlayerRole.POLICE.name())) {
                return new CommandResult.BusinessError<>("Only cops can arrest");
            }
            if (!copsDto.status().equals(PlayerStatus.ACTIVE.name())) {
                return new CommandResult.BusinessError<>("Cops player is not active");
            }

            // Validate robber
            var robberResult = inGamePlayerStore.getPlayer(command.gameId(), command.robberId());
            return ResultMapper.toCommandResult(robberResult).flatMap(robberDto -> {
                if (!robberDto.role().equals(PlayerRole.THIEF.name())) {
                    return new CommandResult.BusinessError<>("Target is not a robber");
                }
                if (!robberDto.status().equals(PlayerStatus.ACTIVE.name())) {
                    return new CommandResult.BusinessError<>("Robber is already arrested");
                }

                // Check distance
                var stateResult = gameStateStore.getGameState(command.gameId());
                return ResultMapper.toCommandResult(stateResult).flatMap(stateDto -> {
                    double radius = stateDto.settings() != null
                        ? stateDto.settings().actionRadiusMeters()
                        : 1.0;
                    var distResult = locationStore.getDistanceMeters(
                        command.gameId(), command.copsId(), command.robberId()
                    );
                    return ResultMapper.toCommandResult(distResult).flatMap(dist -> {
                        if (dist > radius) {
                            return new CommandResult.BusinessError<>(
                                "Robber is out of arrest range: " + dist + "m"
                            );
                        }

                        // Arrest: update robber status
                        var arrestedRobber = new InGamePlayerDto(
                            robberDto.playerId(), robberDto.playerName(),
                            robberDto.role(), PlayerStatus.ARRESTED.name(),
                            robberDto.arrestCount(), robberDto.gemsCollected(),
                            robberDto.rescueCount(), robberDto.escapeCount(),
                            System.currentTimeMillis()
                        );
                        inGamePlayerStore.updatePlayer(command.gameId(), arrestedRobber);

                        // Update cops arrest count
                        var updatedCops = new InGamePlayerDto(
                            copsDto.playerId(), copsDto.playerName(),
                            copsDto.role(), copsDto.status(),
                            copsDto.arrestCount() + 1, copsDto.gemsCollected(),
                            copsDto.rescueCount(), copsDto.escapeCount(),
                            System.currentTimeMillis()
                        );
                        inGamePlayerStore.updatePlayer(command.gameId(), updatedCops);

                        // Publish event
                        gameEventService.publish(new GameEvent.PlayerArrested(
                            command.gameId(), command.copsId(), command.robberId(),
                            System.currentTimeMillis()
                        ));

                        // Check if all robbers arrested
                        checkAllRobbersArrested(command.gameId());

                        return new CommandResult.Success<>(null, "Arrested");
                    });
                });
            });
        });
    }

    /**
     * 동료 구출 (도둑 전용).
     */
    public CommandResult<Void> rescue(RescueCommand command) {
        var rescuerResult = inGamePlayerStore.getPlayer(command.gameId(), command.rescuerId());
        return ResultMapper.toCommandResult(rescuerResult).flatMap(rescuerDto -> {
            if (!rescuerDto.role().equals(PlayerRole.THIEF.name())) {
                return new CommandResult.BusinessError<>("Only robbers can rescue");
            }
            if (!rescuerDto.status().equals(PlayerStatus.ACTIVE.name())) {
                return new CommandResult.BusinessError<>("Rescuer is not active");
            }

            var rescuedResult = inGamePlayerStore.getPlayer(command.gameId(), command.rescuedId());
            return ResultMapper.toCommandResult(rescuedResult).flatMap(rescuedDto -> {
                if (!rescuedDto.status().equals(PlayerStatus.ARRESTED.name())) {
                    return new CommandResult.BusinessError<>("Target is not arrested");
                }

                var stateResult = gameStateStore.getGameState(command.gameId());
                return ResultMapper.toCommandResult(stateResult).flatMap(stateDto -> {
                    double radius = stateDto.settings() != null
                        ? stateDto.settings().actionRadiusMeters()
                        : 1.0;
                    var distResult = locationStore.getDistanceMeters(
                        command.gameId(), command.rescuerId(), command.rescuedId()
                    );
                    return ResultMapper.toCommandResult(distResult).flatMap(dist -> {
                        if (dist > radius) {
                            return new CommandResult.BusinessError<>(
                                "Target is out of rescue range: " + dist + "m"
                            );
                        }

                        // Rescue: restore to ACTIVE
                        var rescuedActive = new InGamePlayerDto(
                            rescuedDto.playerId(), rescuedDto.playerName(),
                            rescuedDto.role(), PlayerStatus.ACTIVE.name(),
                            rescuedDto.arrestCount(), rescuedDto.gemsCollected(),
                            rescuedDto.rescueCount(), rescuedDto.escapeCount(),
                            System.currentTimeMillis()
                        );
                        inGamePlayerStore.updatePlayer(command.gameId(), rescuedActive);

                        // Update rescuer rescue count
                        var updatedRescuer = new InGamePlayerDto(
                            rescuerDto.playerId(), rescuerDto.playerName(),
                            rescuerDto.role(), rescuerDto.status(),
                            rescuerDto.arrestCount(), rescuerDto.gemsCollected(),
                            rescuerDto.rescueCount() + 1, rescuerDto.escapeCount(),
                            System.currentTimeMillis()
                        );
                        inGamePlayerStore.updatePlayer(command.gameId(), updatedRescuer);

                        gameEventService.publish(new GameEvent.PlayerRescued(
                            command.gameId(), command.rescuerId(), command.rescuedId(),
                            System.currentTimeMillis()
                        ));

                        return new CommandResult.Success<>(null, "Rescued");
                    });
                });
            });
        });
    }

    /**
     * 보석 획득 (도둑 전용).
     */
    public CommandResult<Void> collectGem(CollectGemCommand command) {
        var playerResult = inGamePlayerStore.getPlayer(command.gameId(), command.robberId());
        return ResultMapper.toCommandResult(playerResult).flatMap(playerDto -> {
            if (!playerDto.role().equals(PlayerRole.THIEF.name())) {
                return new CommandResult.BusinessError<>("Only robbers can collect gems");
            }
            if (!playerDto.status().equals(PlayerStatus.ACTIVE.name())) {
                return new CommandResult.BusinessError<>("Player is not active");
            }

            var gemResult = gemStore.getGem(command.gameId(), command.gemId());
            return ResultMapper.toCommandResult(gemResult).flatMap(gemDto -> {
                if (!gemDto.status().equals(GemStatus.AVAILABLE.name())) {
                    return new CommandResult.BusinessError<>("Gem is already collected");
                }

                var stateResult = gameStateStore.getGameState(command.gameId());
                return ResultMapper.toCommandResult(stateResult).flatMap(stateDto -> {
                    double radius = stateDto.settings() != null
                        ? stateDto.settings().actionRadiusMeters()
                        : 1.0;
                    var playerLocResult = locationStore.getLocation(command.gameId(), command.robberId());
                    return ResultMapper.toCommandResult(playerLocResult).flatMap(loc -> {
                        double dist = haversineMeters(
                            loc.latitude(), loc.longitude(),
                            gemDto.latitude(), gemDto.longitude()
                        );
                        if (dist > radius) {
                            return new CommandResult.BusinessError<>(
                                "Gem is out of collect range: " + dist + "m"
                            );
                        }

                        // Collect gem
                        var collectedGem = new com.toy.cnr.port.game.model.GemDto(
                            gemDto.gemId(), gemDto.latitude(), gemDto.longitude(),
                            GemStatus.COLLECTED.name(), command.robberId(), gemDto.spawnedAt()
                        );
                        gemStore.updateGem(command.gameId(), collectedGem);

                        var updatedPlayer = new InGamePlayerDto(
                            playerDto.playerId(), playerDto.playerName(),
                            playerDto.role(), playerDto.status(),
                            playerDto.arrestCount(), playerDto.gemsCollected() + 1,
                            playerDto.rescueCount(), playerDto.escapeCount(),
                            System.currentTimeMillis()
                        );
                        inGamePlayerStore.updatePlayer(command.gameId(), updatedPlayer);

                        gameEventService.publish(new GameEvent.GemCollected(
                            command.gameId(), command.robberId(), command.gemId(),
                            System.currentTimeMillis()
                        ));

                        return new CommandResult.Success<>(null, "Gem collected");
                    });
                });
            });
        });
    }

    /**
     * 핑/알람 전송.
     */
    public CommandResult<Void> sendPing(SendPingCommand command) {
        var playerResult = inGamePlayerStore.getPlayer(command.gameId(), command.senderId());
        return ResultMapper.toCommandResult(playerResult).flatMap(playerDto -> {
            gameEventService.publish(new GameEvent.PingAlert(
                command.gameId(),
                command.senderId(),
                command.pingType().name(),
                command.latitude(),
                command.longitude(),
                System.currentTimeMillis()
            ));
            return new CommandResult.Success<>(null, "Ping sent");
        });
    }

    private void checkAllRobbersArrested(String gameId) {
        var allResult = inGamePlayerStore.getAllPlayers(gameId);
        if (!(allResult instanceof com.toy.cnr.port.common.RepositoryResult.Found<List<InGamePlayerDto>> found)) {
            return;
        }
        var players = found.data();
        boolean allRobbersArrested = players.stream()
            .filter(p -> p.role().equals(PlayerRole.THIEF.name()))
            .allMatch(p -> p.status().equals(PlayerStatus.ARRESTED.name()));

        if (allRobbersArrested) {
            gameTimerService.endGame(gameId, PlayerRole.POLICE.name());
        }
    }

    /** 두 GPS 좌표 간 거리 계산 (미터) */
    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}

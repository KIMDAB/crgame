package com.toy.cnr.port.room;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.room.model.RoomDto;
import com.toy.cnr.port.room.model.RoomPlayerDto;

import java.util.List;

/**
 * 방(Room) 저장/조회 포트 인터페이스.
 * <p>
 * Redis HASH를 통해 방 정보를 저장하고 조회합니다.
 */
public interface RoomStore {

    RepositoryResult<Void> saveRoom(RoomDto room);

    RepositoryResult<RoomDto> getRoom(String roomId);

    RepositoryResult<Void> addPlayer(String roomId, RoomPlayerDto player);

    RepositoryResult<Void> removePlayer(String roomId, String playerId);

    RepositoryResult<List<RoomPlayerDto>> getPlayers(String roomId);

    RepositoryResult<Void> updateStatus(String roomId, String status);

    RepositoryResult<Void> deleteRoom(String roomId);
}

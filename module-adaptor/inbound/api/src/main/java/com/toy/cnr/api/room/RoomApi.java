package com.toy.cnr.api.room;

import com.toy.cnr.api.common.error.ApiError;
import com.toy.cnr.api.common.error.ApiErrorResponse;
import com.toy.cnr.api.common.util.ResponseMapper;
import com.toy.cnr.api.common.util.UserPrincipalAdaptorUtil;
import com.toy.cnr.api.room.request.*;
import com.toy.cnr.api.room.response.*;
import com.toy.cnr.api.room.usecase.RoomUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.toy.cnr.domain.common.CommandResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Room", description = "게임 방 관리 API")
@RestController
@RequestMapping("/v1/rooms")
public class RoomApi {

    private final RoomUseCase roomUseCase;

    public RoomApi(RoomUseCase roomUseCase) {
        this.roomUseCase = roomUseCase;
    }

    @Operation(summary = "방 만들기", description = "새로운 게임 방을 생성합니다. 요청자가 방장이 됩니다.")
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@RequestBody RoomCreateRequest request) {
        var user = UserPrincipalAdaptorUtil.getUserInfo();
        return ResponseMapper.toResponseEntity(
            roomUseCase.createRoom(request, user.id().toString())
        );
    }

    @Operation(
        summary = "방 상세 조회",
        description = "방 설정 및 참가자 정보를 조회합니다. 게임 장소는 settings.mapZone (PUT /settings 로 저장)."
    )
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDetailResponse> getRoom(@PathVariable String roomId) {
        return ResponseMapper.toResponseEntity(roomUseCase.getRoom(roomId));
    }

    @Operation(
        summary = "방 설정 변경",
        description = "방 설정을 변경합니다. 방장만 가능. mapZone 으로 게임 장소(좌표) 저장."
    )
    @PutMapping("/{roomId}/settings")
    public ResponseEntity<RoomDetailResponse> updateSettings(
        @PathVariable String roomId,
        @RequestBody RoomUpdateSettingsRequest request
    ) {
        var user = UserPrincipalAdaptorUtil.getUserInfo();
        return ResponseMapper.toResponseEntity(
            roomUseCase.updateSettings(roomId, request, user.id().toString())
        );
    }

    @Operation(summary = "방 참가", description = "방ID 또는 초대 코드로 방에 참가합니다.")
    @PostMapping("/{roomId}/join")
    public ResponseEntity<RoomDetailResponse> joinRoom(
        @PathVariable String roomId,
        @RequestBody RoomJoinRequest request
    ) {
        var user = UserPrincipalAdaptorUtil.getUserInfo();
        return ResponseMapper.toResponseEntity(
            roomUseCase.joinRoom(roomId, request, user.id().toString())
        );
    }

    @Operation(summary = "방 나가기", description = "방에서 나갑니다.")
    @PostMapping("/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(@PathVariable String roomId) {
        var user = UserPrincipalAdaptorUtil.getUserInfo();
        var result = roomUseCase.leaveRoom(roomId, user.id().toString());
        return switch (result) {
            case CommandResult.Success(var data, var msg) ->
                ResponseEntity.noContent().build();
            case CommandResult.ValidationError(var errors) ->
                ResponseEntity.badRequest().body(
                    ApiErrorResponse.from(new ApiError.BadRequest("Validation failed", errors))
                );
            case CommandResult.BusinessError(var reason) -> {
                // roomId가 없을 때만 404, 그 외 정책/상태 충돌은 409로 내려준다.
                if (reason != null && reason.startsWith("Room not found")) {
                    yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiErrorResponse.from(new ApiError.NotFound("room", reason))
                    );
                }
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ApiErrorResponse.from(new ApiError.Conflict(reason))
                );
            }
        };
    }

    @Operation(summary = "참가자 목록", description = "방에 참가한 플레이어 목록을 조회합니다.")
    @GetMapping("/{roomId}/players")
    public ResponseEntity<RoomPlayersResponse> getPlayers(@PathVariable String roomId) {
        return ResponseMapper.toResponseEntity(roomUseCase.getPlayers(roomId));
    }

    @Operation(
        summary = "게임 시작",
        description = "게임을 시작합니다. 방장만 가능하며, 최소 인원 이상이 모여야 합니다."
    )
    @PostMapping("/{roomId}/start")
    public ResponseEntity<String> startGame(@PathVariable String roomId) {
        var user = UserPrincipalAdaptorUtil.getUserInfo();
        return ResponseMapper.toResponseEntity(
            roomUseCase.startGame(roomId, user.id().toString())
        );
    }
}

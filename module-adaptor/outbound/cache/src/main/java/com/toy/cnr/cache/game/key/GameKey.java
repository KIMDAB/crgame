package com.toy.cnr.cache.game.key;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class GameKey {

    // Location
    public static String location(String gameId, String playerId) {
        return "game:" + gameId + ":player:" + playerId + ":location";
    }

    public static String locations(String gameId) {
        return "game:" + gameId + ":locations";
    }

    // Game events
    public static String events(String gameId) {
        return "game:" + gameId + ":events";
    }

    // Game state
    public static String gameState(String gameId) {
        return "game:" + gameId + ":state";
    }

    // InGame players
    public static String gamePlayers(String gameId) {
        return "game:" + gameId + ":players";
    }

    // Gems
    public static String gameGems(String gameId) {
        return "game:" + gameId + ":gems";
    }

    // Room
    public static String room(String roomId) {
        return "room:" + roomId;
    }

    public static String roomPlayers(String roomId) {
        return "room:" + roomId + ":players";
    }

    // Pings (Pub/Sub)
    public static String gamePings(String gameId) {
        return "game:" + gameId + ":pings";
    }

    // Active game registry
    public static String activeGames() {
        return "game:active";
    }

    public static String generateSubscriberId(String gameId) {
        return gameId + ":" + UUID.randomUUID();
    }

    // Room events (Pub/Sub)
    public static String roomEvents(String roomId) {
        return "room:" + roomId + ":events";
    }

    public static String generateRoomSubscriberId(String roomId) {
        return "room:" + roomId + ":" + UUID.randomUUID();
    }
}

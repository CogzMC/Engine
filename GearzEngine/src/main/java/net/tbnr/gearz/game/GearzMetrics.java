/*
 * Copyright (c) 2014.
 * Cogz Development LLC USA
 * All Right reserved
 *
 * This software is the confidential and proprietary information of Cogz Development, LLC.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with Cogz LLC.
 */

package net.tbnr.gearz.game;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import lombok.Getter;
import lombok.NonNull;
import net.tbnr.gearz.player.GearzPlayer;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;

/**
 * Stores metrics on the games
 */
public final class GearzMetrics<PlayerType extends GearzPlayer> {
    @Getter @NonNull
    private DBCollection metricsCollection;
    @Getter @NonNull
    private GearzGame<PlayerType, ?> game;
    @Getter
    private Long gameStart;
    @Getter
    private Long gameEnd;
    private Set<PlayerType> players;

    public static <T extends GearzPlayer> GearzMetrics<T> beginTracking(GearzGame<T, ?> game) {
        DB mongoDB = game.getPlugin().getMongoDB();
        DBCollection metrics = mongoDB.getCollection("metrics");
        GearzMetrics<T> gearzMetrics = new GearzMetrics<>();
        gearzMetrics.metricsCollection = metrics;
        gearzMetrics.game = game;
        return gearzMetrics;
    }

    public GearzMetrics<PlayerType> startGame() {
        this.gameStart = Calendar.getInstance().getTimeInMillis();
        this.players = game.allPlayers();
        return this;
    }

    public GearzMetrics<PlayerType> finishGame() {
        this.gameEnd = Calendar.getInstance().getTimeInMillis();
        return this;
    }

    public void done(Map<String, Object> data) {
        BasicDBObject object = new BasicDBObject();
        object.put("game", game.getGameMeta().key());
        object.put("game_start", gameStart);
        object.put("game_end", gameEnd);
        object.put("game_length", gameEnd - gameStart);
        object.put("arena", game.getArena().getId());
        BasicDBList players = new BasicDBList();
        for (GearzPlayer player : this.players) {
            players.add(player.getTPlayer().getPlayerDocument().get("_id"));
        }
        object.put("players", players);
        if (data != null) {
            for (Map.Entry<String, Object> stringObjectEntry : data.entrySet()) {
                object.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
            }
        }
        this.metricsCollection.save(object);
    }

    public void done() {
        done(null);
    }
}

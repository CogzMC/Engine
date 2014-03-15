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

package net.tbnr.gearz.player.bungee;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.tbnr.gearz.GearzBungee;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;


/**
 * Used to access the database, disconnect, etc.
 */
public final class GearzPlayer {
    /**
     * The player's username
     */
    private final String username;
    @Getter @Setter public String nickname;
    /**
     * The player document
     */
    private DBObject playerDocument;

    private GearzPlayer(@NonNull DBObject object) throws PlayerNotFoundException {
        String username1;
        try {
            username1 = (String) object.get("username");
        } catch (ClassCastException ex) {
            throw new PlayerNotFoundException("Invalid document");
        }
        this.playerDocument = object;
        this.username = username1;
        updateNickname();
    }

    /**
     * Creates a player from a proxied player
     *
     * @param player The proxied player. :o
     */

    public GearzPlayer(String player) throws PlayerNotFoundException {
        this.username = player;
        loadDocument();
    }

    public GearzPlayer(ProxiedPlayer player) throws PlayerNotFoundException {
        this(player.getName());
    }

    public static GearzPlayer getById(ObjectId id) throws PlayerNotFoundException {
        DBObject query = new BasicDBObject("_id", id);
        DBObject one = getCollection().findOne(query);
        if (one == null) throw new PlayerNotFoundException("Invalid ID");
        return new GearzPlayer(one);
    }

    /**
     * Loads the document from the database representing the player :D
     *
     * @throws PlayerNotFoundException This occurs when there is no database object for that player, can be used as a hook
     *                                 for retrying the find.
     */
    public void loadDocument() throws PlayerNotFoundException {
        DBObject object = new BasicDBObject("username", this.username);
        DBObject cursor = getCollection().findOne(object);
        if (cursor != null) {
            this.playerDocument = cursor;
        } else {
            throw new PlayerNotFoundException("Player not found yet!");
        }
    }

    /**
     * Gets the base collection.
     *
     * @return Collection
     */
    public static DBCollection getCollection() {
        return GearzBungee.getInstance().getMongoDB().getCollection(GearzBungee.getInstance().getFormat("db-collection", false, false));
    }

    /**
     * Player Not Found Exception
     */
    public static class PlayerNotFoundException extends Exception {
        public PlayerNotFoundException(String s) {
            super(s);
        }
    }

    public DBObject getPlayerDocument() {
        if (this.playerDocument != null) return this.playerDocument;
        try {
            loadDocument();
        } catch (PlayerNotFoundException e) {
            return null;
        }
        return this.playerDocument;
    }

    public ProxiedPlayer getProxiedPlayer() {
        return ProxyServer.getInstance().getPlayer(this.username);
    }

    public void updateNickname() {
        try {
            loadDocument();
        } catch (PlayerNotFoundException e) {
            return;
        }
        Object nick = playerDocument.get("gearz_nickname");
        if (nick == null || !(nick instanceof String)) return;
        String nickname = (String) nick;
        setNickname(nickname);
        getProxiedPlayer().setDisplayName(nickname);
    }

    public List<String> getIgnoredUsers() {
        List<String> ignores = new ArrayList<>();
        Object ignoreObj = getPlayerDocument().get("ignored");
        if (ignoreObj == null || !(ignoreObj instanceof BasicDBList)) return ignores;
        BasicDBList ignoreList = (BasicDBList) ignoreObj;
        for (Object obj : ignoreList) {
            if (!(obj instanceof String)) continue;
            ignoreList.add(obj);
        }
        return ignores;
    }

    public void ignorePlayer(ProxiedPlayer player) {
        Object ignoreObj = getPlayerDocument().get("ignored");
        if (ignoreObj == null || !(ignoreObj instanceof BasicDBList)) {
            ignoreObj = new BasicDBList();
        }
        BasicDBList ignoreList = (BasicDBList) ignoreObj;
        ignoreList.add(player.getName());
        getPlayerDocument().put("ignored", ignoreList);
    }

    public void unignorePlayer(ProxiedPlayer player) {
        Object ignoreObj = getPlayerDocument().get("ignored");
        if (ignoreObj == null || !(ignoreObj instanceof BasicDBList)) {
            ignoreObj = new BasicDBList();
        }
        BasicDBList ignoreList = (BasicDBList) ignoreObj;
        if (!ignoreList.contains(player.getName())) return;
        ignoreList.remove(player.getName());
        getPlayerDocument().put("ignored", ignoreList);
    }

    public String getName() {
        return this.username;
    }

    public void save() {
        getCollection().save(this.playerDocument);
    }
}

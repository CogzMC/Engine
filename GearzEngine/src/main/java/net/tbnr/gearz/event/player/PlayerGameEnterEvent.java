/*
 * Copyright (c) 2014.
 * CogzMC LLC USA
 * All Right reserved
 *
 * This software is the confidential and proprietary information of Cogz Development, LLC.
 * ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with Cogz LLC.
 */

package net.tbnr.gearz.event.player;

import net.tbnr.gearz.game.GearzGame;
import net.tbnr.gearz.player.GearzPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created with IntelliJ IDEA.
 * User: Joey
 * Date: 10/6/13
 * Time: 9:18 PM
 * To change this template use File | Settings | File Templates.
 */
public final class PlayerGameEnterEvent extends Event {
    private final GearzGame game;
    private final GearzPlayer player;
    /*
    Event code
     */
    private static final HandlerList handlers = new HandlerList();

    public PlayerGameEnterEvent(GearzGame game, GearzPlayer player) {
        this.game = game;
        this.player = player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public GearzPlayer getPlayer() {
        return player;
    }

    public GearzGame getGame() {
        return game;
    }
}
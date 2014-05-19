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

import lombok.Getter;
import net.tbnr.gearz.game.GearzGame;
import net.tbnr.gearz.player.GearzPlayer;

/**
 * Created by Joey on 1/12/14.
 * <p/>
 * Purpose Of File:
 * <p/>
 * Latest Change:
 */
public final class PlayerGameKillEvent extends PlayerGameDeathEvent {
    @Getter
    private final GearzPlayer killer;

    public PlayerGameKillEvent(GearzGame game, GearzPlayer dead, GearzPlayer killer) {
        super(game, dead);
        this.killer = killer;
    }
}

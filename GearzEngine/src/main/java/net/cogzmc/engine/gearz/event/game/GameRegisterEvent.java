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

package net.cogzmc.engine.gearz.event.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.cogzmc.engine.gearz.GearzPlugin;
import net.cogzmc.engine.gearz.arena.Arena;
import net.cogzmc.engine.gearz.event.GearzEvent;
import net.cogzmc.engine.gearz.game.GameMeta;
import net.cogzmc.engine.gearz.game.GearzGame;
import org.bukkit.event.Cancellable;

/**
 * Created with IntelliJ IDEA.
 * User: Joey
 * Date: 10/6/13
 * Time: 9:04 PM
 * To change this template use File | Settings | File Templates.
 */
@RequiredArgsConstructor
public final class GameRegisterEvent extends GearzEvent implements Cancellable {
    @Getter
    private final Class<? extends Arena> arena;
    @Getter
    private final Class<? extends GearzGame> game;
    @Getter
    private final GameMeta meta;
    @Getter
    private final GearzPlugin plugin;
    @Getter
    @Setter
    private boolean cancelled = false;
}

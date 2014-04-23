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

package net.tbnr.gearz;

import net.tbnr.gearz.netcommand.NetCommand;
import net.tbnr.util.player.TPlayerJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created with IntelliJ IDEA.
 * User: Joey
 * Date: 10/31/13
 * Time: 11:29 PM
 */
public final class PlayerListener implements Listener {
    @EventHandler
    @SuppressWarnings("unused")
    public void tPlayerJoinEvent(TPlayerJoinEvent event) {
        NetCommand.withName("update_p").withArg("name", event.getPlayer());
    }
}

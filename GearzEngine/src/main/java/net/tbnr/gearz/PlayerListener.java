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

package net.tbnr.gearz;

import net.tbnr.gearz.effects.entityblocks.EntityBlock;
import net.tbnr.gearz.netcommand.NetCommand;
import net.tbnr.gearz.server.ServerManager;
import net.tbnr.util.player.TPlayerJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

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

    @EventHandler
    public void playerJoinEvent(PlayerLoginEvent event) {
        if (ServerManager.canJoin()) return;
        event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        event.setKickMessage("You are not permitted to join this server at this time.");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getKickMessage().equalsIgnoreCase("zPermissions failed to initialize")) {
            Gearz.getInstance().getLogger().info("zPermissions error detected, restarting server with hopes of a better server!");
            Bukkit.shutdown();
        }
    }

	@EventHandler
	public void onEntityBlockTest(final PlayerJoinEvent event) {
		new BukkitRunnable() {

			@Override
			public void run() {
				Location l = event.getPlayer().getLocation();
				EntityBlock.newBlock(l, Material.GOLD_BLOCK, (byte)0).showBlock(event.getPlayer());
				EntityBlock.newBlock(l.subtract(1, 1, 1), Material.DIAMOND_BLOCK, (byte)0).showBlock(event.getPlayer());
				EntityBlock.newBlock(l.add(1,1,1), Material.EMERALD_BLOCK, (byte)0).showBlock(event.getPlayer());
			}

		}.runTaskLater(Gearz.getInstance(), 40);
	}
}

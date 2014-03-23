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
import net.tbnr.gearz.effects.entityblocks.EntityBlockUtil;
import net.tbnr.gearz.netcommand.NetCommand;
import net.tbnr.gearz.server.ServerManager;
import net.tbnr.util.player.TPlayerJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
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
				//Earth
				for(EntityBlock entityBlock : EntityBlockUtil.createSphere(60, event.getPlayer().getLocation().clone().add(0, 20, 0), Material.WOOL, DyeColor.LIGHT_BLUE.getWoolData(), 16)) {
					if(Gearz.getRandom().nextInt(11) <= 1) entityBlock.setData(DyeColor.LIME.getWoolData());
					entityBlock.showBlock(event.getPlayer());
				}
				//Sun
				for(EntityBlock entityBlock : EntityBlockUtil.createSphere(160, event.getPlayer().getLocation().clone().add(40, 20, 0), Material.WOOL, DyeColor.YELLOW.getWoolData(), 16)) {
					if(Gearz.getRandom().nextInt(11) <= 1) entityBlock.setData(DyeColor.ORANGE.getWoolData());
					entityBlock.showBlock(event.getPlayer());
				}
				for(EntityBlock entityBlock : EntityBlockUtil.createSphere(140, event.getPlayer().getLocation().clone().add(40, 20, 0), Material.WOOL, DyeColor.YELLOW.getWoolData(), 16)) {
					if(Gearz.getRandom().nextInt(11) <= 1) entityBlock.setData(DyeColor.ORANGE.getWoolData());
					entityBlock.showBlock(event.getPlayer());
				}
				for(EntityBlock entityBlock : EntityBlockUtil.createSphere(120, event.getPlayer().getLocation().clone().add(40, 20, 0), Material.WOOL, DyeColor.YELLOW.getWoolData(), 16)) {
					if(Gearz.getRandom().nextInt(11) <= 1) entityBlock.setData(DyeColor.ORANGE.getWoolData());
					entityBlock.showBlock(event.getPlayer());
				}
				//Mercury
				for(EntityBlock entityBlock : EntityBlockUtil.createSphere(40, event.getPlayer().getLocation().clone().add(30, 20, 0), Material.WOOL, DyeColor.RED.getWoolData(), 16)) {
					if(Gearz.getRandom().nextInt(11) <= 1) entityBlock.setData(DyeColor.ORANGE.getWoolData());
					entityBlock.showBlock(event.getPlayer());
				}
				//Venus
				for(EntityBlock entityBlock : EntityBlockUtil.createSphere(70, event.getPlayer().getLocation().clone().add(20, 20, 0), Material.WOOL, DyeColor.YELLOW.getWoolData(), 10)) {
					if(Gearz.getRandom().nextInt(11) <= 1) {
						entityBlock.setData((byte)12);
						entityBlock.setType(Material.STAINED_GLASS);
					}
					entityBlock.showBlock(event.getPlayer());
				}
				//Mars
				for(EntityBlock entityBlock : EntityBlockUtil.createSphere(75, event.getPlayer().getLocation().clone().add(-10, 20, 0), Material.WOOL, DyeColor.RED.getWoolData(), 10)) {
					if(Gearz.getRandom().nextInt(11) <= 0) entityBlock.setData(DyeColor.WHITE.getWoolData());
					entityBlock.showBlock(event.getPlayer());
				}
				//Jupiter
				for(EntityBlock entityBlock : EntityBlockUtil.createSphere(150, event.getPlayer().getLocation().clone().add(-10, 20, 0), Material.WOOL, DyeColor.RED.getWoolData(), 8)) {
					if(Gearz.getRandom().nextInt(11) <= 0) entityBlock.setData(DyeColor.ORANGE.getWoolData());
					if(Gearz.getRandom().nextInt(11) == 2 ) entityBlock.setData(DyeColor.WHITE.getWoolData());
					entityBlock.showBlock(event.getPlayer());
				}
			}

		}.runTaskLater(Gearz.getInstance(), 40);
	}
}

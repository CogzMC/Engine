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

package net.cogz.engine.hub.annotations;

import lombok.extern.java.Log;
import net.cogz.engine.hub.GearzHub;
import net.tbnr.util.player.TPlayerJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.reflections.Reflections;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by rigor789 on 2013.12.21.
 * <p/>
 * Purpose Of File:
 * <p/>
 * Latest Change:
 */
@Log
public class HubItems implements Listener {

    private final ArrayList<HubItem> items;

    /**
     * Creates a new HubItems instance
     *
     * @param reflections ~ the reflections where all the items are
     */
    public HubItems(Set<Class<? extends HubItem>> reflections) {
        items = new ArrayList<>();
        log.info(reflections.size() + "size");
        for (Class<? extends HubItem> hubItem : reflections) {

            HubItemMeta itemMeta = hubItem.getAnnotation(HubItemMeta.class);
            if (itemMeta == null) continue;
            if (itemMeta.hidden()) continue;
            log.info(GearzHub.getInstance().getSubHub().getConfig().getString("prefix") + " prefix");
            log.info(itemMeta.key());
            if (GearzHub.getInstance().getSubHub().getConfig().getBoolean("hub-items." + itemMeta.key() + ".isEnabled", false)) {
                try {
                    HubItem item = hubItem.newInstance();
                    items.add(item);
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    @SuppressWarnings("unused")
    public void onPlayerJoin(TPlayerJoinEvent event) {
        Player player = event.getPlayer().getPlayer();
        ItemStack itemStack;
        ItemStack itemInSlot;
        for (HubItem item : items) {
            if (!shouldAdd(player, item.getItems())) continue;

            HubItemMeta itemMeta = item.getClass().getAnnotation(HubItemMeta.class);
            if (itemMeta == null) continue;
            if (itemMeta.hidden()) continue;

            if (!player.hasPermission(itemMeta.permission()) && !itemMeta.permission().isEmpty()) return;
            itemStack = item.getItems().get(0);
            if (itemMeta.slot() == -1) {
                player.getInventory().addItem(itemStack);
                continue;
            }

            itemInSlot = player.getInventory().getItem(itemMeta.slot());
            if (itemInSlot != null && itemInSlot.getType() != Material.AIR) return;
            player.getInventory().setItem(itemMeta.slot(), itemStack);
        }
    }

    private boolean shouldAdd(Player player, List<ItemStack> item) {
        for (ItemStack i : item) {
            if (player.getInventory().contains(i)) return false;
        }
        return true;
    }
}

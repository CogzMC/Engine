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

package net.cogzmc.engine.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import net.cogzmc.engine.hub.annotations.HubItem;
import net.cogzmc.engine.hub.annotations.HubItems;
import net.cogzmc.engine.hub.modules.HideStream;
import net.cogzmc.engine.hub.modules.Restrictions;
import net.cogzmc.engine.hub.modules.Spawn;
import net.cogzmc.engine.server.Server;
import net.cogzmc.engine.server.ServerManager;
import net.cogzmc.engine.util.IPUtils;
import net.cogzmc.engine.util.TPlugin;
import org.bukkit.Bukkit;

import java.net.SocketException;
import java.util.Set;

/**
 * <p/>
 * Latest Change:
 * <p/>
 *
 * @author Jake
 * @since 5/16/2014
 */
@Log
public class GearzHub extends TPlugin {
    public static GearzHub instance;
    @Getter
    @Setter
    private GearzHub subHub;
    private HubItems hubItems;

    public static GearzHub getInstance() {
        return instance;
    }

    @Override
    public void enable() {
        //set instance
        instance = this;

        //Register modules
        Spawn spawn = new Spawn();
        registerCommands(spawn);
        registerEvents(spawn);
        registerEvents(new HideStream());
        registerEvents(new Restrictions());

        //Saves the hub to the database, always last lines of code
        ServerManager.setGame("lobby");
        ServerManager.setStatusString("HUB_DEFAULT");
        ServerManager.setOpenForJoining(true);
        Server thisServer = ServerManager.getThisServer();
        try {
            thisServer.setAddress(IPUtils.getExternalIP());
        } catch (SocketException e) {
            e.printStackTrace();
        }
        thisServer.setPort(Bukkit.getPort());
        thisServer.save();
    }

    @Override
    public void disable() {
    }

    @Override
    public String getStorablePrefix() {
        return "gearzhub";
    }

    public void registerHubItems(Set<Class<? extends HubItem>> items) {
        this.hubItems = new HubItems(items, this);
        registerEvents(this.hubItems);
    }
}

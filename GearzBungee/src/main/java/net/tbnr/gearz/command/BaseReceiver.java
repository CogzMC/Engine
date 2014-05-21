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

package net.tbnr.gearz.command;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.tbnr.util.ConnectionUtils;
import net.tbnr.gearz.player.bungee.GearzPlayerManager;

import java.util.HashMap;

/**
 * {@link net.tbnr.gearz.command.BaseReceiver}, gets all incoming {@link net.tbnr.gearz.command.NetCommand} calls from this base {@link net.tbnr.util.TPluginBungee}.
 */
public class BaseReceiver {
    @NetCommandHandler(args = {"player", "server"}, name = "send")
    public void onSend(HashMap<String, Object> args) {
        Object p = args.get("player");
        Object s = args.get("server");
        if (!(p instanceof String) || !(s instanceof String)) return;
        String player = (String) p;
        String server = (String) s;
        ProxiedPlayer player1 = ProxyServer.getInstance().getPlayer(player);
        if (player1 == null) return;
        ConnectionUtils.connectPlayer(player1, server);
    }

    @NetCommandHandler(args = {"name"}, name = "update_p")
    public void onUpdate(HashMap<String, Object> args) {
        Object p = args.get("player");
        if (!(p instanceof String)) return;
        String player = (String) p;
        ProxiedPlayer player1 = ProxyServer.getInstance().getPlayer(player);
        if (player1 == null) return;
        GearzPlayerManager.getInstance().storePlayer(player1);
    }
}

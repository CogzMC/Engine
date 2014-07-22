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

package net.tbnr.gearz.modules;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
import com.maxmind.geoip.timeZone;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.tbnr.gearz.GearzBungee;
import net.tbnr.gearz.player.bungee.GearzPlayer;
import net.tbnr.gearz.player.bungee.GearzPlayerManager;
import net.tbnr.gearz.server.Server;
import net.tbnr.gearz.server.ServerManager;
import net.tbnr.util.StringUtils;
import net.tbnr.util.WeatherUtils;
import net.tbnr.util.bungee.command.TCommand;
import net.tbnr.util.bungee.command.TCommandHandler;
import net.tbnr.util.bungee.command.TCommandSender;
import net.tbnr.util.bungee.command.TCommandStatus;
import net.tbnr.util.bungee.cooldowns.TCooldown;
import net.tbnr.util.bungee.cooldowns.TCooldownManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Module to display large amounts of information
 * about a player including their UUID, IP, location,
 * and much more information.
 * <p>
 * Latest Change: Added previous usernames and IPs
 * <p>
 *
 * @author Joey
 * @since Unknown
 */
public final class PlayerInfoModule implements TCommandHandler, Listener {
    private LookupService lookupService = null;

    public PlayerInfoModule() {
        try {
            File resource = createStorageFile(GearzBungee.getInstance().getResourceAsStream("geocity.dat"));
            if (resource == null) return;
            lookupService = new LookupService(resource, LookupService.GEOIP_MEMORY_CACHE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File createStorageFile(InputStream is) throws IOException {
        File tmp = null;
        FileOutputStream tmpOs = null;
        try {
            tmp = File.createTempFile("dat", "tmp");
            tmpOs = new FileOutputStream(tmp);
            int len;
            byte[] b = new byte[4096];
            while ((len = is.read(b)) != -1) {
                tmpOs.write(b, 0, len);
            }
        } finally {
            try {
                is.close();
            } catch (Exception ignored) {
            }
            try {
                if (tmpOs != null) {
                    tmpOs.close();
                }
            } catch (Exception ignored) {
                //Ignored
            }
        }
        return tmp;
    }

    @TCommand(usage = "/playerinfo <target>", senders = {TCommandSender.Player, TCommandSender.Console}, permission = "gearz.playerinfo", aliases = {"whois"}, name = "playerinfo")
    @SuppressWarnings("unused")
    public TCommandStatus playerInfo(CommandSender sender, TCommandSender type, TCommand meta, String[] args) {
        if (args.length < 1) return TCommandStatus.FEW_ARGS;
        if (!TCooldownManager.canContinueLocal("playerinfo", new TCooldown(TimeUnit.SECONDS.toMillis(7)))) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("cooling-down", false, false));
            return TCommandStatus.SUCCESSFUL;
        }
        boolean anyFound = false;
        for (String s : args) {
            List<ProxiedPlayer> matchedPlayers = GearzBungee.getInstance().getPlayerManager().getMatchedPlayers(s);
            if (matchedPlayers.size() == 0) continue;
            for (ProxiedPlayer player : matchedPlayers) {
                anyFound = true;
                provideInformation(player, sender);
            }
        }
        if (!anyFound) {
            sender.sendMessage(GearzBungee.getInstance().getFormat("no-matches", false));
        }
        return TCommandStatus.SUCCESSFUL;
    }

    private void provideInformation(final ProxiedPlayer player, final CommandSender sender) {
        ProxyServer.getInstance().getScheduler().runAsync(GearzBungee.getInstance(), new Runnable() {
            @Override
            public void run() {
                sender.sendMessage(GearzBungee.getInstance().getFormat("playerinfo-header", false, false, new String[]{"<target>", player.getName()}));
                sender.sendMessage(formatData("IP", player.getAddress().getHostString()));
                sender.sendMessage(formatData("UUID", player.getUniqueId().toString()));
                Server serverForBungee = getServerForBungee(player.getServer().getInfo());
                sender.sendMessage(formatData("Server", serverForBungee.getGame() + serverForBungee.getNumber()));
                sender.sendMessage(formatData("Server State", serverForBungee.getStatusString()));
                sender.sendMessage(formatData("Server Players Count", String.valueOf(serverForBungee.getPlayerCount())));
                Location location = lookupService == null ? null : lookupService.getLocation(player.getAddress().getAddress());
                if (lookupService == null) {
                    GearzBungee.getInstance().getLogger().severe("Player Lookup Service not loaded!");
                }
                sender.sendMessage(formatData("Location", (location == null ? "Error" : location.city + " " + location.countryName)));
                sender.sendMessage(formatData("Weather", (location == null ? "Error" : WeatherUtils.getWeatherConditons(location.city))));
                String timezone = location == null ? null : timeZone.timeZoneByCountryAndRegion(location.countryCode, location.region);
                TimeZone tz = timezone == null ? null : TimeZone.getTimeZone(timezone);
                DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
                dateFormatter.setTimeZone(tz);
                sender.sendMessage(formatData("Timezone", timezone == null ? "Error" : timezone));
                sender.sendMessage(formatData("Local Time", tz == null ? "Error" : dateFormatter.format(new Date())));
                GearzPlayer gearzPlayer = GearzPlayerManager.getGearzPlayer(player);
                sender.sendMessage(formatData("Total Time Online", StringUtils.formatDuration((Long) gearzPlayer.getPlayerDocument().get("time-online"))));
                sender.sendMessage(formatData("Previous IPs", StringUtils.formatList(gearzPlayer.getIPHistory(), 5)));
                sender.sendMessage(formatData("Previous Usernames", StringUtils.formatList(gearzPlayer.getUsernameHistory(), 10)));
            }
        });
    }

    public static Server getServerForBungee(ServerInfo info) {
        for (Server server : ServerManager.getAllServers()) {
            if (server.getBungee_name().equals(info.getName())) return server;
        }
        return null;
    }

    private String formatData(String key, String value) {
        return "  " + GearzBungee.getInstance().getFormat("playerinfo-display-item", false, false, new String[]{"<key>", key}, new String[]{"<value>", value});
    }

    @Override
    public void handleCommandStatus(TCommandStatus status, CommandSender sender, TCommandSender senderType) {
        GearzBungee.handleCommandStatus(status, sender);
    }
}

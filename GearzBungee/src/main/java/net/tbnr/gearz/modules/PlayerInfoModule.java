package net.tbnr.gearz.modules;

import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;
import com.maxmind.geoip.timeZone;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.tbnr.gearz.GearzBungee;
import net.tbnr.gearz.player.bungee.GearzPlayer;
import net.tbnr.gearz.player.bungee.GearzPlayerManager;
import net.tbnr.gearz.server.Server;
import net.tbnr.gearz.server.ServerManager;
import net.tbnr.util.WeatherUtils;
import net.tbnr.util.bungee.command.TCommand;
import net.tbnr.util.bungee.command.TCommandHandler;
import net.tbnr.util.bungee.command.TCommandSender;
import net.tbnr.util.bungee.command.TCommandStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Responsible for stalking our player base. #NoPrivacyPolicy
 */
public class PlayerInfoModule implements TCommandHandler, Listener {
    private LookupService lookupService = null;

    public PlayerInfoModule() {
        try {
            File resource = doThing(GearzBungee.getInstance().getResourceAsStream("geocity.dat"));
            if (resource == null) return;
            lookupService = new LookupService(resource, LookupService.GEOIP_MEMORY_CACHE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File doThing(InputStream is) throws IOException {
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
        for (String s : args) {
            List<ProxiedPlayer> matchedPlayers = GearzBungee.getInstance().getPlayerManager().getMatchedPlayers(s);
            if (matchedPlayers.size() == 0) continue;
            for (ProxiedPlayer player : matchedPlayers) {
                provideInformation(player, sender);
            }
        }
        return TCommandStatus.SUCCESSFUL;
    }

    private void provideInformation(ProxiedPlayer player, CommandSender sender) {
        sender.sendMessage(GearzBungee.getInstance().getFormat("playerinfo-header", false, false, new String[]{"<target>", player.getName()}));
        sender.sendMessage(formatData("IP", player.getAddress().getHostString()));
        sender.sendMessage(formatData("UUID", player.getUUID()));
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
        sender.sendMessage(formatData("Total Time Online", formatDuration((Long) gearzPlayer.getPlayerDocument().get("time-online"))));
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

    private String formatDuration(Long mills) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
        return sdf.format(new Date(mills - TimeZone.getDefault().getRawOffset()));
    }

    @Override
    public void handleCommandStatus(TCommandStatus status, CommandSender sender, TCommandSender senderType) {
        GearzBungee.handleCommandStatus(status, sender);
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        GearzPlayer player;
        try {
            player = new GearzPlayer(event.getPlayer().getName());
        } catch (GearzPlayer.PlayerNotFoundException e) {
            return;
        }
        DBObject playerDocument = player.getPlayerDocument();
        BasicDBList ips = (BasicDBList) playerDocument.get("ips");
        if (ips == null) ips = new BasicDBList();
        String hostString = event.getPlayer().getAddress().getHostString();
        if (!ips.contains(hostString)) ips.add(hostString);
        playerDocument.put("ips", ips);
        playerDocument.put("uuid", event.getPlayer().getUUID());
        /*Location location = lookupService == null ? null : lookupService.getLocation(event.getPlayer().getAddress().getAddress());
        if (location != null)
            playerDocument.put("last_location", location.countryCode + "|" + location.region + "|" + location.city + "|" + location.postalCode);*/
        GearzPlayer.getCollection().save(playerDocument);
    }


}
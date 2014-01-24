package net.tbnr.gearz;

import net.tbnr.gearz.netcommand.NetCommand;
import net.tbnr.gearz.server.ServerManager;
import net.tbnr.util.player.TPlayerJoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;

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
}

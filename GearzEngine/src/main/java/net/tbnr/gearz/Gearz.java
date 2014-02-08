package net.tbnr.gearz;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.cogz.permissions.bukkit.GearzBukkitPermissions;
import net.tbnr.gearz.activerecord.GModel;
import net.tbnr.gearz.effects.EnchantmentEffect;
import net.tbnr.gearz.effects.EnderBar;
import net.tbnr.gearz.game.single.GameManagerSingleGame;
import net.tbnr.gearz.netcommand.NetCommand;
import net.tbnr.gearz.player.ClearChat;
import net.tbnr.gearz.player.GearzNickname;
import net.tbnr.gearz.player.GearzPlayerUtils;
import net.tbnr.gearz.server.Server;
import net.tbnr.gearz.server.ServerManager;
import net.tbnr.gearz.server.ServerManagerHelper;
import net.tbnr.util.*;
import net.tbnr.util.command.TCommandHandler;
import net.tbnr.util.command.TCommandSender;
import net.tbnr.util.command.TCommandStatus;
import net.tbnr.util.player.TPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * Gearz Plugin
 */
public final class Gearz extends TPlugin implements TCommandHandler, TDatabaseMaster, ServerManagerHelper {
    /**
     * The Gearz instance.
     */
    private static Gearz instance;
    /**
     * Stores the Random for Gearz
     */
    private static Random random;

    private static String bungeeName2;
    /**
     * Jedis pool
     */
    private JedisPool jedisPool;

    public List<GearzPlugin> getGamePlugins() {
        return gamePlugins;
    }

    private List<GearzPlugin> gamePlugins;

    public static final String CHAN = "GEARZ_NETCOMMAND";

    @Getter
    @Setter
    private boolean isLobbyServer;

    public static Gearz getInstance() {
        return instance;
    }

    @Getter
    InventoryRefresher inventoryRefresher;

    public boolean showDebug() {
        return false;
    }

    public static Random getRandom() {
        return random;
    }

    @Getter
    public GearzBukkitPermissions permissions;

    public Gearz() {
        Gearz.instance = this;
        Gearz.random = new Random();
    }

    @Override
    public void enable() {

        //** ENABLE **
        //This method is a bit confusing. Let's comment/clean it up a bit <3

        //Reset all players for the EnderBar
        EnderBar.resetPlayers();

        //Setup the helper delegation methods
        ServerManager.setHelper(this);

        //Setup a new Bungee Name variable.
        if (Gearz.bungeeName2 == null) Gearz.bungeeName2 = RandomUtils.getRandomString(16);

        //Kick all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer("Server is reloading!");
        }

        //Setup the Jedis pool so we can communicate with BungeeCord using our NetCommand system.
        this.jedisPool = new JedisPool(getConfig().getString("redis.host"), getConfig().getInt("redis.port"));

        //Setup an array to hold a list of all Gearz plugins.
        this.gamePlugins = new ArrayList<>();

        //Get a local variable of the PluginManager, and setup our chat/permission hooks.
        PluginManager pm = getServer().getPluginManager();
        if (pm.isPluginEnabled("GearzBukkitPermissions")) {
            this.permissions = (GearzBukkitPermissions) pm.getPlugin("GearzBukkitPermissions");
            GearzNickname nicknameHandler = new GearzNickname();
            registerEvents(nicknameHandler);
            registerCommands(nicknameHandler);
            registerEvents(new ColoredTablist());
        }

        //Setup the player utils commands and events
        GearzPlayerUtils gearzPlayerUtils = new GearzPlayerUtils();
        registerEvents(gearzPlayerUtils);
        registerCommands(gearzPlayerUtils);

        //Generic player utils
        registerEvents(new PlayerListener());
        registerEvents(new EnderBar.EnderBarListeners());
        registerCommands(new ClearChat());
        new TabListener();

        //EnderBar utils
        registerEvents(new EnderBar.EnderBarListeners());

        //ClearChat
        registerCommands(new ClearChat());

        //Silk Touch 32 listener for effects
        EnchantmentEffect.addEnchantmentListener();

        //Setup the inv refresher. This is used in the server selector.
        this.inventoryRefresher = new InventoryRefresher();

        //Setup some hooks for the GModel class to connect to our database.
        GModel.setDefaultDatabase(this.getMongoDB());

        //Deprecated.
        Gearz.getInstance().getConfig().set("bg.name", RandomUtils.getRandomString(16));

        //Link the server up in the database.
        Bukkit.getScheduler().runTaskLater(Gearz.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (Gearz.getInstance().isGameServer()) {
                    Server thisServer = ServerManager.getThisServer();
                    try {
                        thisServer.setAddress(Gearz.getExternalIP());
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                    thisServer.setPort(Bukkit.getPort());
                    thisServer.save();
                    GameManagerSingleGame temp = (GameManagerSingleGame) getGamePlugins().get(0).getGameManager();
                    registerCommands(temp);
                }
                Gearz.getInstance().getLogger().info("Server linked and in the database");
            }
        }, 1);

    }

    @Override
    public void disable() {
        ServerManager.getThisServer().remove();
        NetCommand.withName("disconnect").withArg("name", Gearz.bungeeName2);
        EnderBar.resetPlayers();
    }

    @Override
    public String getStorablePrefix() {
        return "gearz";
    }

    @Override
    public void handleCommandStatus(TCommandStatus status, org.bukkit.command.CommandSender sender, TCommandSender senderType) {
        if (status == TCommandStatus.SUCCESSFUL) return;
        String msgFormat = null;
        switch (status) {
            case PERMISSIONS:
                msgFormat = "formats.no-permission";
                break;
            case INVALID_ARGS:
                msgFormat = "formats.bad-args";
                break;
            case FEW_ARGS:
                msgFormat = "formats.few-args";
                break;
            case MANY_ARGS:
                msgFormat = "formats.many-args";
                break;
            case WRONG_TARGET:
                msgFormat = "formats.wrong-target";
                break;
        }
        if (msgFormat == null) return;
        sender.sendMessage(Gearz.getInstance().getFormat(msgFormat, true));
    }

    @Override
    public TPlayerManager.AuthenticationDetails getAuthDetails() {
        return new TPlayerManager.AuthenticationDetails(getConfig().getString("database.host"), getConfig().getInt("database.port"), getConfig().getString("database.database"), getConfig().getString("database.collection"));
    }


    public Jedis getJedisClient() {
        return jedisPool.getResource();
    }

    public void returnJedis(Jedis jedis) {
        this.jedisPool.returnResource(jedis);
    }

    @SuppressWarnings("unused")
    public void registerGame(GearzPlugin plugin) {
        this.gamePlugins.add(plugin);
    }

    @SuppressWarnings("unused")
    public boolean isGameServer() {
        return this.gamePlugins.size() > 0;
    }

    private static void delete(@NonNull File file) {
        if (file.isDirectory()) {
            try {
                for (File f : file.listFiles()) {
                    delete(f);
                }
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }
        if (!file.delete()) Gearz.getInstance().getLogger().warning("File: " + file + " could not be deleted");
    }

    @Override
    public String getGame() {
        return (this.isGameServer() ? this.gamePlugins.get(0).getGameManager().getGameMeta().key() : null);
    }

    public static String getBungeeName2() {
        return bungeeName2;
    }

    public String getBungeeName() {
        return Gearz.bungeeName2;
    }

    public static String getExternalIP() throws SocketException, IndexOutOfBoundsException {
        if (!Bukkit.getIp().equals("")) return Bukkit.getIp();
        NetworkInterface eth0 = NetworkInterface.getByName(Gearz.getInstance().getConfig().getString("network_interface"));

        if (eth0 == null) eth0 = NetworkInterface.getByName("eth0");

        Enumeration<InetAddress> inetAddresses = eth0.getInetAddresses();
        ArrayList<InetAddress> list = Collections.list(inetAddresses);
        InetAddress fin = null;
        for (InetAddress inetAddress : list) {
            if (inetAddress.getHostAddress().matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}")) {
                fin = inetAddress;
                break;
            }
        }
        return fin == null ? null : fin.getHostAddress();
    }
}

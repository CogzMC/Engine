package net.tbnr.gearz;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBList;
import lombok.Getter;
import net.craftminecraft.bungee.bungeeyaml.bukkitapi.InvalidConfigurationException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.tbnr.gearz.activerecord.GModel;
import net.tbnr.gearz.chat.Chat;
import net.tbnr.gearz.chat.ChatManager;
import net.tbnr.gearz.chat.Messaging;
import net.tbnr.gearz.command.BaseReceiver;
import net.tbnr.gearz.command.NetCommandDispatch;
import net.tbnr.gearz.modules.*;
import net.tbnr.gearz.player.bungee.GearzPlayerManager;
import net.tbnr.gearz.punishments.IPBanHandler;
import net.tbnr.gearz.punishments.LoginHandler;
import net.tbnr.gearz.punishments.PunishCommands;
import net.tbnr.gearz.punishments.UnPunishCommands;
import net.tbnr.util.TDatabaseManagerBungee;
import net.tbnr.util.TPluginBungee;
import net.tbnr.util.bungee.command.TCommandStatus;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * - Whitelist support
 * - Kickall support
 * - Announcer
 * - Reconnect attempts
 * - Register on Site TODO
 * - Minigame quick-join using /server
 * - Help command
 * - Report command
 * - Chat logger
 * - Stream chat to a site for viewing by staff
 */
@SuppressWarnings("NullArgumentToVariableArgMethod")
public class GearzBungee extends TPluginBungee implements TDatabaseManagerBungee {
    /**
     * Gearz Instance
     */
    private static GearzBungee instance;
    /**
     * Stores the static strings file loaded into memory
     */
    private Properties strings;
    /**
     * Responder object, in it's own thread
     */
    /**
     * The JEDIS pool object.
     */
    private JedisPool pool;
    /**
     * Random number generator
     */
    private static Random random = new Random();
    /**
     * Stores the player manager.
     */
    private GearzPlayerManager playerManager;
    /**
     * Stores chat utils
     */
    private ChatManager chatUtils;

    /**
     * Stores chat data
     */
    @Getter
    private Chat chat;

    /**
     * Announcer module
     */
    @Getter
    private AnnouncerModule announcerModule;

    /**
     * Has our NetCommandDispatch for registration.
     */
    @Getter
    private NetCommandDispatch dispatch;

    @Getter
    private HelpMe helpMeModule;

    @Getter
    private ModBroadcast modBroadCastModule;

    @Getter
    private IPBanHandler ipBanHandler;

    @Getter
    private ShuffleModule shuffleModule;

    @Getter
    private ListModule listModule;

    /**
     * Gets the current instance of the GearzBungee plugin.
     *
     * @return The instance.
     */
    public static GearzBungee getInstance() {
        return GearzBungee.instance;
    }

    public static Random getRandom() {
        return random;
    }

    @Override
    protected void start() {
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();
        GearzBungee.instance = this;
        GModel.setDefaultDatabase(this.getMongoDB());
        this.pool = new JedisPool(new JedisPoolConfig(), getConfig().getString("host"));
        //this.responder = new ServerResponder();
        this.dispatch = new NetCommandDispatch();
        this.getDispatch().registerNetCommands(new BaseReceiver());
        this.playerManager = new GearzPlayerManager();
        registerEvents(this.playerManager);
        MotdHandler motdHandler = new MotdHandler();
        registerEvents(motdHandler);
        registerCommandHandler(motdHandler);
        this.chatUtils = new ChatManager();
        this.chat = new Chat();
        registerCommandHandler(new Messaging());
        registerEvents(this.chatUtils);
        registerCommandHandler(this.chatUtils);
        Hub hub = new Hub();
        registerEvents(hub);
        registerCommandHandler(hub);
        registerCommandHandler(new UtilCommands());
        registerCommandHandler(new ServerModule());
        listModule = new ListModule();
        registerCommandHandler(listModule);
        registerEvents(listModule);
        this.strings = new Properties();
        reloadStrings();
        this.helpMeModule = new HelpMe();
        this.helpMeModule.registerReminderTask(30);
        registerCommandHandler(this.helpMeModule);
        registerEvents(this.helpMeModule);
        this.modBroadCastModule = new ModBroadcast();
        registerCommandHandler(this.modBroadCastModule);
        registerEvents(this.modBroadCastModule);
        PlayerInfoModule infoModule = new PlayerInfoModule();
        registerCommandHandler(infoModule);
        registerEvents(infoModule);
        registerEvents(new LoginHandler());
        registerCommandHandler(new PunishCommands());
        registerCommandHandler(new UnPunishCommands());
        this.ipBanHandler = new IPBanHandler(getMongoDB().getCollection("ipbans"));
        this.shuffleModule = new ShuffleModule();
        registerEvents(this.shuffleModule);
        registerCommandHandler(this.shuffleModule);
        ProxyServer.getInstance().getScheduler().schedule(this, new ServerModule.BungeeServerReloadTask(), 0, 1, TimeUnit.SECONDS);
    }

    private List<AnnouncerModule.Announcement> loadAnnouncements() {
        List<AnnouncerModule.Announcement> announcements = Lists.newArrayList();
        BasicDBList objects = (BasicDBList) getBungeeConfig().get("announcements");
        if (objects == null) {
            return announcements;
        }

        for (Object object : objects) {
            String string = (String) object;
            AnnouncerModule.Announcement announcement = new AnnouncerModule.Announcement(string);
            announcements.add(announcement);
        }
        return announcements;
    }

    private int getInterval() {
        Integer interval = (Integer) getBungeeConfig().get("announcements_interval");
        if (interval == null) {
            return 60;
        }
        return interval;
    }

    private void reloadStrings() {
        try {
            this.strings.load(getResourceAsStream("strings.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void stop() {
        saveConfig();
    }

    public Object[] getMotds() {
        Object motd = getBungeeConfig().get("motds");
        if (motd == null || !(motd instanceof BasicDBList)) {
            BasicDBList dbList = new BasicDBList();
            dbList.add("Another Gearz Server");
            getBungeeConfig().put("motds", dbList);
            return dbList.toArray();
        }
        return ((BasicDBList) motd).toArray();
    }

    public String[] getCensoredWords() {
        Object censoredWords = getBungeeConfig().get("censoredWords");
        if (censoredWords == null || !(censoredWords instanceof BasicDBList)) {
            return new String[0];
        }
        BasicDBList dbListCensored = (BasicDBList)censoredWords;
        return dbListCensored.toArray(new String[dbListCensored.size()]);
    }

    public void setCensoredWords(BasicDBList dbList) {
        bungeeConfigSet("censoredWords", dbList);
    }

    public int getMaxPlayers() {
        Object maxPlayers = bungeeConfigGet("max-players");
        if (maxPlayers == null || !(maxPlayers instanceof Integer)) return 1;
        return (Integer) maxPlayers;  //To change body of created methods use File | Settings | File Templates.
    }

    @SuppressWarnings("unused")
    public void setMaxPlayers(Integer maxPlayers) {
        bungeeConfigSet("max-players", maxPlayers);
    }

    public void setMotds(BasicDBList motds) {
        bungeeConfigSet("motds", motds);
    }

    @Override
    public String database() {
        return getConfig().getString("database");  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String host() {
        return getConfig().getString("host");  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int port() {
        return getConfig().getInt("port");  //To change body of implemented methods use File | Settings | File Templates.
    }

    @SuppressWarnings("unused")
    public GearzPlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public String getFormat(String key, boolean prefix, boolean color, String[]... datas) {
        String property = this.strings.getProperty(key);
        if (prefix)
            property = ChatColor.translateAlternateColorCodes('&', this.strings.getProperty("prefix")) + property;
        property = ChatColor.translateAlternateColorCodes('&', property);
        if (datas == null) return property;
        for (String[] data : datas) {
            if (data.length != 2) continue;
            property = property.replaceAll(data[0], data[1]);
        }
        if (color) property = ChatColor.translateAlternateColorCodes('&', property);
        return property;
    }

    public String getFormat(String key, boolean prefix, boolean color) {
        return getFormat(key, prefix, color, null);
    }

    public String getFormat(String key, boolean prefix) {
        return getFormat(key, prefix, true);
    }

    public String getFormat(String key) {
        return getFormat(key, true);
    }

    public List<String> getData(String file) {
        File f = new File(getDataFolder(), file);
        if (!(f.canRead() && f.exists())) try {
            boolean newFile = f.createNewFile();
            if (!newFile) return null;
            getData(file);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
        BufferedReader stream;
        try {
            stream = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(f))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        List<String> lines = new ArrayList<>();
        String line;
        try {
            while ((line = stream.readLine()) != null) {
                lines.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
        return lines;
    }

    public static List<String> boxMessage(ChatColor firstColor, ChatColor secondColor, List<String> message) {
        List<String> stringList = new ArrayList<>();
        char[] chars = new char[50];
        Arrays.fill(chars, ' ');
        String result = new String(chars);
        stringList.add(firstColor + "" + ChatColor.STRIKETHROUGH + result);
        stringList.addAll(message);
        stringList.add(secondColor + "" + ChatColor.STRIKETHROUGH + result);
        return stringList;
    }

    public static List<String> boxMessage(ChatColor firstColor, String... message) {
        return boxMessage(firstColor, firstColor, Arrays.asList(message));
    }

    @SuppressWarnings("unused")
    public static List<String> boxMessage(String... message) {
        return boxMessage(ChatColor.WHITE, message);
    }

    public static List<String> boxMessage(ChatColor color, List<String> message) {
        return boxMessage(color, color, message);
    }

    @SuppressWarnings("unused")
    public static List<String> boxMessage(List<String> message) {
        return boxMessage(ChatColor.WHITE, message);
    }

    public Jedis getJedisClient() {
        return this.pool.getResource();
    }

    public void returnJedisClient(Jedis client) {
        this.pool.returnResource(client);
    }

    /*
        @SuppressWarnings("unused")
        @Deprecated
        public ServerResponder getResponder() {
            return responder;
        }*/
    public static void handleCommandStatus(TCommandStatus status, CommandSender sender) {
        String msgFormat = null;
        switch (status) {
            case PERMISSIONS:
                msgFormat = "no-permission";
                break;
            case INVALID_ARGS:
                msgFormat = "bad-args";
                break;
            case FEW_ARGS:
                msgFormat = "few-args";
                break;
            case MANY_ARGS:
                msgFormat = "many-args";
                break;
            case WRONG_TARGET:
                msgFormat = "wrong-target";
                break;
        }
        if (msgFormat == null) return;
        sender.sendMessage(GearzBungee.getInstance().getFormat(msgFormat, true));
    }

    public static void connectPlayer(ProxiedPlayer player1, String server) {
        ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(server);
        if (serverInfo == null) {
            player1.sendMessage(GearzBungee.getInstance().getFormat("server-not-online", true, true));
            return;
        }
        if (player1.getServer().getInfo().getName().equals(server)) {
            player1.sendMessage(GearzBungee.getInstance().getFormat("already-connected"));
            return;
        }
        player1.sendMessage(GearzBungee.getInstance().getFormat("connecting", true, true));
        player1.connect(serverInfo);
    }

    public String compile(String[] args, int min, int max) {
        StringBuilder builder = new StringBuilder();

        for (int i = min; i < args.length; i++) {
            builder.append(args[i]);
            if (i == max) return builder.toString();
            builder.append(" ");
        }
        return builder.toString();
    }
}

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

package net.tbnr.util.player;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.java.Log;
import net.tbnr.gearz.Gearz;
import net.tbnr.gearz.packets.wrapper.WrapperPlayServerWorldParticles;
import net.tbnr.util.IPUtils;
import net.tbnr.util.RandomUtils;
import net.tbnr.util.RedFactory;
import net.tbnr.util.TPlugin;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static net.tbnr.gearz.packets.wrapper.WrapperPlayServerWorldParticles.ParticleEffect;

/**
 * {@link net.tbnr.util.player.TPlayer} is a representation of the {@link org.bukkit.entity.Player} in {@link org.bukkit.Bukkit} that can store more data about the {@link net.tbnr.util.player.TPlayer}, and also execute methods
 * on the player that are considered "helper" methods. These methods are utilities to preform very simple tasks that would
 * otherwise be more difficult using the vanilla Bukkit API.
 */
@SuppressWarnings("UnusedDeclaration")
@EqualsAndHashCode(of = {"playerName", "timeJoined", "uuid"}, doNotUseGetters = true)
@ToString(of = {"playerName", "timeJoined"}, includeFieldNames = true, doNotUseGetters = true)
@Log
public final class TPlayer {
    /**
     * The variable storing the actual {@link org.bukkit.entity.Player} this represents. R/O
     */
    @Getter
    private final String playerName;

    @Getter
    private final String uuid;
    /**
     * The database document representing the {@link org.bukkit.entity.Player}.
     */
    @Getter
    private DBObject playerDocument;
    /**
     * The time the {@link org.bukkit.entity.Player} joined.
     */
    @Getter
    private final long timeJoined;
    /**
     * The time the {@link org.bukkit.entity.Player} has spent online.
     */
    private long timeOnline;
    /**
     * A boolean representing that a {@link org.bukkit.entity.Player} has joined for the first time.
     */
    @Getter
    private boolean firstJoin;
    /**
     * Scoreboard object for the {@link org.bukkit.entity.Player}
     */
    private Scoreboard scoreboard;
    /**
     * Objective for {@link org.bukkit.entity.Player} on sidebar
     */
    private Objective sidebar = null;

    /**
     * This is a protected method for creating a {@link net.tbnr.util.player.TPlayer} instance from a {@link org.bukkit.Bukkit} {@link org.bukkit.entity.Player}.
     *
     * @param player The {@link org.bukkit.Bukkit} {@link org.bukkit.entity.Player} this represents.
     */
    protected TPlayer(Player player) {
        this.playerName = player.getName();
        this.uuid = player.getUniqueId().toString();
        this.timeJoined = Calendar.getInstance().getTimeInMillis();

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        if (TPlayerManager.getInstance().getCollection() == null) return;

        this.playerDocument = TPlayer.getPlayerObject(player.getUniqueId());
        if (this.playerDocument == null) {
            this.playerDocument = new BasicDBObject("uuid", player.getUniqueId().toString()); //So we didn't find it, create our own, and set the username var.
            this.playerDocument.put("time-online", 0l); //Sets the online time to 0 so this var is present (long).
            this.playerDocument.put("first-join", Calendar.getInstance().getTimeInMillis());
            this.firstJoin = true;
        } else {
            this.firstJoin = false;
        }
        if (this.playerDocument.get("first-join") == null) {
            this.playerDocument.put("first-join", Calendar.getInstance().getTimeInMillis());
        }
        this.playerDocument.put("last-seen", Calendar.getInstance().getTimeInMillis()); //Update last-seen
        this.playerDocument.put("online", true); //Update the online variable
        addToList("usernames", this.playerName);

        this.save();
        this.timeOnline = (Long) this.playerDocument.get("time-online");
    }

    private void addToList(String fieldName, String toAdd) {
        BasicDBList field = (BasicDBList) this.playerDocument.get(fieldName);
        if (field == null) {
            field = new BasicDBList();
        }
        if (!field.contains(toAdd)) {
            field.add(toAdd);
        }
        this.playerDocument.put(fieldName, field);
    }

    public void logIp(String ip) {
        addToList("ips", ip);
        this.save();
    }

    public static DBObject getPlayerObject(UUID uuid) {
        return getPlayerObject(uuid.toString());
    }

    public static DBObject getPlayerObject(String uuid) {
        BasicDBObject query = new BasicDBObject("uuid", uuid); //Query the database for the player's UUID
        DBCursor cursor = TPlayerManager.getInstance().getCollection().find(query);
        if (cursor.hasNext()) {
            return cursor.next();
        } else {
            return null;
        }
    }

    public static DBObject getPlayerObjectByLastKnownName(String name) {
        BasicDBObject query = new BasicDBObject("current_username", name); //Query the database for the player's last known username
        return TPlayerManager.getInstance().getCollection().findOne(query);
    }

    public static DBObject getAnyPlayerWithUUID(String uuid) {
        BasicDBObject query = new BasicDBObject("uuid", uuid);
        return TPlayerManager.getInstance().getCollection().findOne(query);
    }

    public static DBObject getPlayerByObjectId(String objectId) {
        ObjectId created = new ObjectId(objectId);
        BasicDBObject query = new BasicDBObject("_id", created);
        return TPlayerManager.getInstance().getCollection().findOne(query);
    }

    /**
     * Get the actual {@link org.bukkit.entity.Player} this object represents
     *
     * @return The player object from Bukkit that this represents
     */
    public Player getPlayer() {
        Gearz.getInstance().debug("GEARZ DEBUG ---<TPlayer|120>--------< getPlayer has been CAUGHT for: " + this.playerName + " and got: " + Bukkit.getPlayerExact(this.playerName));
        return Bukkit.getPlayerExact(this.playerName);
    }

    /**
     * Plays a sound for the player at a volume with no pitch modification.
     *
     * @param sound  The sound.
     * @param volume The volume.
     */
    public void playSound(Sound sound, Integer volume) {
        this.playSound(sound, volume, 0);
    }

    /**
     * Plays a sound at the volume of 10
     *
     * @param sound The sound.
     */
    public void playSound(Sound sound) {
        this.playSound(sound, 10);
    }

    /**
     * Plays a sound at a volume with a specific pitch.
     *
     * @param sound  The sound.
     * @param volume The volume.
     * @param pitch  The pitch.
     */
    public void playSound(Sound sound, Integer volume, Integer pitch) {
        if (!this.isOnline()) return;
        this.getPlayer().playSound(getPlayer().getLocation(), sound, volume, pitch);
    }

    /**
     * Adds a potion effect to a {@link net.tbnr.util.player.TPlayer} quickly.
     *
     * @param type      The potion effect type
     * @param length    The length of the potion effect.
     * @param intensity The intensity of the potion effect.
     * @param ambient   Is this potion effect ambient? Check Bukkit docs for more info on this one.
     */
    public void addPotionEffect(PotionEffectType type, Integer length, Integer intensity, boolean ambient) {
        PotionEffect toAdd = new PotionEffect(type, (length == Integer.MAX_VALUE ? Integer.MAX_VALUE : length * 20), intensity, ambient);
        this.getPlayer().addPotionEffect(toAdd);
    }

    /**
     * Adds a potion effect to a {@link net.tbnr.util.player.TPlayer} quickly. Has a default ambiance of "true".
     *
     * @param type      The potion effect type
     * @param length    The length (I believe in ticks).
     * @param intensity The intensity of the potion effect.
     */
    public void addPotionEffect(PotionEffectType type, Integer length, Integer intensity) {
        this.addPotionEffect(type, length, intensity, true);
    }

    /**
     * Adds a potion effect to a {@link net.tbnr.util.player.TPlayer} quickly. Has a default ambiance of "true", and a default intensity of 0.
     *
     * @param type   The potion effect type.
     * @param length The length in ticks
     */
    public void addPotionEffect(PotionEffectType type, Integer length) {
        this.addPotionEffect(type, length, 0);
    }

    /**
     * Adds a potion effect to a player quickly. Adds the potion effect forever, with an ambiance of "true", and an intensity of 0.
     *
     * @param type The type of the potion effect.
     */
    public void addPotionEffect(PotionEffectType type) {
        this.addPotionEffect(type, Integer.MAX_VALUE);
    }

    /**
     * Adds a potion effect to a player quickly, and forever. This allows you to specify an intensity.
     *
     * @param type      The type of the potion effect.
     * @param intensity The intensity of the potion effect. (0 = Level 1)
     */
    public void addInfinitePotionEffect(PotionEffectType type, Integer intensity) {
        this.addPotionEffect(type, Integer.MAX_VALUE, intensity);
    }

    /**
     * This will remove ALL active potion effects except those specified.
     *
     * @param exclusions Specify some potion effect types, or none, and they will not be removed.
     */
    public void removeAllPotionEffects(PotionEffectType... exclusions) {
        List<PotionEffectType> doNotRemove = Arrays.asList(exclusions);
        Player player = this.getPlayer();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (doNotRemove.contains(effect.getType())) continue;
            player.removePotionEffect(effect.getType());
        }
    }

    /**
     * Remove specified potion effects
     *
     * @param potionEffects Specify the potion effect types to remove. This method will not run if none are specified
     */
    public void removePotionEffects(PotionEffectType... potionEffects) {
        if (potionEffects.length < 1) {
            return;
        }
        List<PotionEffectType> potionEffectTypes = Arrays.asList(potionEffects);
        for (PotionEffect effect : this.getPlayer().getActivePotionEffects()) {
            if (potionEffectTypes.contains(effect.getType())) this.getPlayer().removePotionEffect(effect.getType());
        }
    }

	/**
	 * Gets The Level of a certain type of potion
	 * @param effectType ~ The Type of potion
	 * @return the potion level OR -1 if potion not active
	 */
	public Integer getCurrentPotionLevel(PotionEffectType effectType) {
		Integer level = -1;
		for (PotionEffect effect : this.getPlayer().getActivePotionEffects()) {
			if (!effect.getType().equals(effectType)) continue;
			level = effect.getAmplifier();
			break;
		}
		return level;
	}

	/**
	 * Gets the Duration of a certain type of potion
	 * @param type ~ The type of the potion
	 * @return the potion duration OR -1 if potion not active
	 */
	public Integer getCurrentPotionDuration(PotionEffectType type) {
		Integer level = -1;
		for (PotionEffect effect : this.getPlayer().getActivePotionEffects()) {
			if (!effect.getType().equals(type)) continue;
			level = effect.getDuration();
			break;
		}
		return level;
	}

    /**
     * Test is player has certain potion effect
     *
     * @param e ~ Potion Type
     * @return True if the player does
     */
    public boolean hasPotionEffect(PotionEffectType e) {
        return e != null && getPlayer().hasPotionEffect(e);

    }

    /**
     * Give an item to a {@link net.tbnr.util.player.TPlayer}
     *
     * @param type       The material of the item
     * @param quantity   The quantity of the item
     * @param data_value The data value (used for wool colors, etc)
     * @param title      The title of the item.
     * @param lore       The lore of the item
     * @param slot       The slot to put the item in
     */
    public ItemStack giveItem(Material type, Integer quantity, short data_value, String title, String[] lore, Integer slot) {
        Player player = this.getPlayer();
        if (type == null || quantity < 1) return null;

        ItemStack itemStack = new ItemStack(type, quantity);
        if (data_value > 1) itemStack.setDurability(data_value);

        ItemMeta meta = itemStack.getItemMeta();
        if (title != null) meta.setDisplayName(title);
        if (lore != null) meta.setLore(Arrays.asList(lore));
        itemStack.setItemMeta(meta);
        //HotBar slots are from 1-9
        if (slot < 1 || slot > 9) {
            Integer toGive = quantity;
            while (toGive > 0) {
                itemStack.setAmount(Math.min(itemStack.getMaxStackSize(), toGive));
                player.getInventory().addItem(itemStack);
                toGive = toGive - itemStack.getAmount();
            }
        } else {
            player.getInventory().setItem(slot - 1, itemStack);
        }
        return itemStack;
    }

    /**
     * Give an item to a {@link net.tbnr.util.player.TPlayer}
     *
     * @param type       The material of the item
     * @param quantity   The quantity of the item
     * @param data_value The data value (used for wool colors, etc)
     * @param title      The title of the item.
     * @param lore       The lore of the item
     */
    public ItemStack giveItem(Material type, Integer quantity, short data_value, String title, String[] lore) {
        return giveItem(type, quantity, data_value, title, lore, -1);
    }

    /**
     * Give an item to a {@link net.tbnr.util.player.TPlayer}
     *
     * @param type       The material of the item
     * @param quantity   The quantity of the item
     * @param data_value The data value (used for wool colors, etc)
     * @param title      The title of the item.
     */
    public ItemStack giveItem(Material type, Integer quantity, short data_value, String title) {
        return giveItem(type, quantity, data_value, title, null);
    }

    /**
     * Give an item to a player
     *
     * @param type       The material of the item
     * @param quantity   The quantity of the item
     * @param data_value The data value (used for wool colors, etc)
     */
    public ItemStack giveItem(Material type, Integer quantity, short data_value) {
        return giveItem(type, quantity, data_value, null);
    }

    /**
     * Give an item to a {@link net.tbnr.util.player.TPlayer}
     *
     * @param type     The material of the item
     * @param quantity The quantity of the item
     */
    public ItemStack giveItem(Material type, Integer quantity) {
        return giveItem(type, quantity, (short) 0);
    }

    /**
     * Gives a single item to a {@link net.tbnr.util.player.TPlayer}
     *
     * @param type The material of the item
     */
    public ItemStack giveItem(Material type) {
        return giveItem(type, 1);
    }

    /**
     * @param material The material of the item to search for
     * @param quantity How many of the items to remove
     * @return If the item was removed (if the user had enough).
     */
    public boolean removeItem(Material material, Integer quantity) {
        if (!getPlayer().getInventory().contains(material, quantity)) {
            return false;
        }
        getPlayer().getInventory().removeItem(new ItemStack(material, quantity));
        return true;
    }

    /**
     * Removes a singular item from the {@link net.tbnr.util.player.TPlayer}'s inventory
     *
     * @param material The material of the item to remove.
     * @return If the item was removed (ie; if they had the item in their inventory)
     */
    public boolean removeItem(Material material) {
        return removeItem(material, 1);
    }

    /**
     * Kills the player.
     */
    public void kill() {
        this.getPlayer().damage(this.getPlayer().getHealth());
    }

    /**
     * Sends messages to the {@link net.tbnr.util.player.TPlayer}
     *
     * @param message The message(s) to send to the {@link net.tbnr.util.player.TPlayer}.
     */
    public void sendMessage(String... message) {
        if (!this.isOnline()) {
            return;
        }
        for (String m : message) {
            this.getPlayer().sendMessage(m);
        }
    }

    /**
     * Called by the {@link net.tbnr.util.player.TPlayerManager} when the player disconnects. Do not call otherwise
     */
    void disconnected() {
        this.playerDocument.put("online", false);
        Object o = getPlayerDocument().get("time-online");
        if (o == null) o = 0l;
        if (!(o instanceof Long)) return;
        long timeOnline = (Long) o;
        long now = Calendar.getInstance().getTimeInMillis();
        timeOnline = timeOnline + (now - timeJoined);
        this.playerDocument.put("time-online", timeOnline);
        this.playerDocument.put("last-seen", now);
        this.save();
    }

    /**
     * Saves the {@link net.tbnr.util.player.TPlayer} {@link com.mongodb.DBObject} to the database.
     */
    public void save() {
        TPlayerManager.getInstance().getCollection().save(this.playerDocument);
    }

    /**
     * Use this to store prefixed and managed data about a {@link net.tbnr.util.player.TPlayer} that can be accessed later.
     *
     * @param plugin   The {@link net.tbnr.util.TPlugin} responsible for storing the data.
     * @param storable The {@link net.tbnr.util.player.TPlayerStorable} object.
     */
    public void store(TPlugin plugin, TPlayerStorable storable) {
        this.playerDocument.put(TPlayer.formatStorable(plugin.getStorablePrefix(), storable.getName()), storable.getValue());
        this.save();
    }

    public void store(TPlugin plugin, final String key, final Object o) {
        store(plugin, new TPlayerStorable() {
            @Override
            public String getName() {
                return key;
            }

            @Override
            public Object getValue() {
                return o;
            }
        });
    }

    /**
     * Gets the value of a {@link net.tbnr.util.player.TPlayerStorable}
     *
     * @param plugin   The {@link net.tbnr.util.TPlugin} storing this data. Used for keys.
     * @param storable An empty {@link net.tbnr.util.player.TPlayerStorable} object with the proper data name.
     * @return The stored {@link java.lang.Object}.
     */
    public Object getStorable(TPlugin plugin, TPlayerStorable storable) {
        return this.getStorable(plugin, storable.getName());
    }

    /**
     * Gets the value of a {@link net.tbnr.util.player.TPlayerStorable}
     *
     * @param plugin       The {@link net.tbnr.util.TPlugin} storing this data. Used for keys.
     * @param storable_key The {@link net.tbnr.util.player.TPlayerStorable} key.
     * @return The stored {@link java.lang.Object}.
     */
    public Object getStorable(TPlugin plugin, String storable_key) {
        return this.playerDocument.get(TPlayer.formatStorable(plugin.getStorablePrefix(), storable_key));
    }

    public <T> T getStorable(TPlugin plugin, String storable_key, Class<T> clazz) {
        //noinspection unchecked
        return (T) getStorable(plugin, storable_key);
    }

    /**
     * Formats {@link java.lang.String} for a {@link net.tbnr.util.player.TPlayerStorable} key.
     *
     * @param prefix The prefix of the {@link net.tbnr.util.player.TPlayerStorable} (from the plugin)
     * @param name   The name of the {@link net.tbnr.util.player.TPlayerStorable}
     * @return Fully formatted {@link net.tbnr.util.player.TPlayerStorable} key.
     */
    public static String formatStorable(String prefix, String name) {
        return prefix + "_" + name;
    }

    /**
     * Checks if there is another object matching a {@link net.tbnr.util.player.TPlayerStorable} in the database
     *
     * @param storable The {@link net.tbnr.util.player.TPlayerStorable} prefix of the object
     * @param value    The value to match
     * @return If there is a match!
     */
    public static boolean anyMatchesToStorable(TPlugin plugin, String storable, Object value) {
        DBObject object = new BasicDBObject(TPlayer.formatStorable(plugin.getStorablePrefix(), storable), value);
        DBCursor cursor = TPlayerManager.getInstance().getCollection().find(object);
        return cursor.hasNext();
    }

    /**
     * Gets the amount of time spent online by this {@link org.bukkit.entity.Player} (accurate at time being called)
     *
     * @return The time online in milliseconds.
     */
    public long getTimeOnline() {
        return timeOnline + (Calendar.getInstance().getTimeInMillis() - this.timeJoined);
    }

    /**
     * Plays a particle effect for the user
     *
     * @param effect The effect to play
     * @throws Exception
     */
    public void playParticleEffect(TParticleEffect effect) throws Exception {
        for (ParticleEffect type : effect.getParticleEffectType()) {
            WrapperPlayServerWorldParticles packet = new WrapperPlayServerWorldParticles();
            packet.setParticleName(type.getParticleName());
            packet.setX((float) effect.getLocation().getX());
            packet.setY((float) effect.getLocation().getY());
            packet.setZ((float) effect.getLocation().getZ());
            packet.setOffsetX(effect.getOffset());
            packet.setOffsetY(effect.getHeight());
            packet.setOffsetZ(effect.getOffset());
            packet.setParticleSpeed(effect.getSpeed());
            packet.setNumberOfParticles(effect.getCount());

            packet.sendPacket(getPlayer());
        }
    }

    /**
     * sets a value of an {@link Object} via reflection
     *
     * @param instance  instance the class to use
     * @param fieldName the name of the {@link Field} to modify
     * @param value     the value to set
     * @throws Exception
     */
    public static void setValue(Object instance, String fieldName, Object value) throws Exception {
        Field field = instance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }

    /**
     * Teleports the {@link org.bukkit.entity.Player} to a {@link org.bukkit.Location} with a nice {@link Sound#ENDERMAN_TELEPORT} sound.
     *
     * @param location The {@link org.bukkit.Location} to teleport the {@link org.bukkit.entity.Player} to
     */
    public void teleport(Location location) {
        this.playSound(Sound.ENDERMAN_TELEPORT);
        this.getPlayer().teleport(location);
    }

    /**
     * Clears the {@link org.bukkit.inventory.PlayerInventory} of the {@link org.bukkit.entity.Player}.
     */
    public void clearInventory() {
        this.getPlayer().getInventory().clear();
        this.getPlayer().getInventory().setArmorContents(new ItemStack[4]);
    }

    /**
     * Used to reset everything about the {@link org.bukkit.entity.Player}, can be fine tuned the {@link net.tbnr.util.player.PlayerResetParams}.
     */
    public void resetPlayer(PlayerResetParams params) {
        final Player player = getPlayer();
        player.getInventory().setHeldItemSlot(0);
        if (!isOnline()) return;
        if (params == null) params = new PlayerResetParams();

        if (params.isClearXP()) {
            player.setExp(0);
            player.setLevel(0);
            player.setTotalExperience(0);
        }
        if (params.isClearPotions()) removeAllPotionEffects();
        if (params.isResetInventory()) clearInventory();
        if (params.isRestoreHealth()) {
            player.setHealth(player.getMaxHealth());
            player.setRemainingAir(20);
        }
        Bukkit.getScheduler().runTaskLater(Gearz.getInstance(), new Runnable() {
            @Override
            public void run() {
                player.setFireTicks(0);
            }
        }, 2L);
        if (params.isRestoreFood()) {
            player.setFoodLevel(20);
            player.setExhaustion(0);
        }
        player.setSneaking(false);
        if (!params.isResetFlight()) {
            return;
        }

        player.setVelocity(new Vector(0, 0, 0));
        player.setFallDistance(0F);
        player.setAllowFlight(false);
        RandomUtils.setPlayerCollision(player, true);
    }

    /**
     * No params reset.
     */
    public void resetPlayer() {
        this.resetPlayer(null);
    }

    /**
     * Resets the {@link org.bukkit.scoreboard.Scoreboard}
     */
    public void resetScoreboard() {
        if (!this.isOnline()) return;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.sidebar = null;
        this.getPlayer().setScoreboard(this.scoreboard);
    }

    public void setScoreboardSideTitle(String title) {
        if (!this.isOnline()) return;

        if (this.sidebar == null) {
            String s = new BigInteger(13, Gearz.getRandom()).toString(5);
            this.sidebar = this.scoreboard.registerNewObjective(s.substring(0, Math.min(s.length(), 15)), "dummy");
            this.sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        this.sidebar.setDisplayName(title);
    }

    public void setScoreBoardSide(String key, Integer value) {
        if (!this.isOnline()) return;

        Score score = this.sidebar.getScore(key.substring(0, Math.min(key.length(), 15)));
        score.setScore(value);
        Player player = getPlayer();
        if (player == null) return;
        if (!player.isOnline()) return;
        player.setScoreboard(this.scoreboard);
    }

    public void removeScoreboardSide(String key) {
        if (!this.isOnline()) {
            return;
        }
        this.scoreboard.resetScores(key);
        getPlayer().setScoreboard(this.scoreboard);
    }

    public static final class TParticleEffect {
        @Getter
        private final Location location;
        @Getter
        private final float height;
        @Getter
        private final float offset;
        @Getter
        private final Integer count;
        @Getter
        private final float speed;
        private final List<ParticleEffect> particleEffectTypes;

        public TParticleEffect(Location location, float height, float offset, Integer count, float speed, ParticleEffect... particleEffectType) {
            this.location = location;
            this.height = height;
            this.offset = offset;
            this.count = count;
            this.speed = speed;
            this.particleEffectTypes = Arrays.asList(particleEffectType);
        }

        public List<ParticleEffect> getParticleEffectType() {
            return particleEffectTypes;
        }

    }

    public boolean isOnline() {
        return Bukkit.getPlayer(this.playerName) != null;
    }

    /**
     * Returns the ping asynchronously via the {@link net.tbnr.util.IPUtils.PingCallbackEventHandler} you pass in
     * In that event handler you can do {@link net.tbnr.util.IPUtils.PingCallbackEventHandler#getPing(java.net.InetAddress, net.tbnr.util.IPUtils.PingCallbackEventHandler)} to the passed in {@link net.tbnr.util.IPUtils.PingCallbackEvent}.
     *
     * @param eventHandler ~ The PingCallbackEventHandler
     * @see net.tbnr.util.IPUtils.PingCallbackEvent
     * @see net.tbnr.util.IPUtils.PingCallbackEventHandler
     */
    public void getPing(IPUtils.PingCallbackEventHandler eventHandler) {
        IPUtils.getPing(getPlayer().getAddress().getAddress(), eventHandler);
    }

	public void flashRed() {
		RedFactory.addRed(this);
	}

	public void stopFlashRed() {
		RedFactory.removeRed(this);
	}

	public boolean isFlashingRed() {
		return RedFactory.isRed(this);
	}
}

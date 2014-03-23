package net.tbnr.gearz.settings;

import net.gearz.settings.SettingsManager;
import net.gearz.settings.SettingsRegistry;
import net.gearz.settings.base.BaseSettingsRegistry;
import net.tbnr.gearz.Gearz;
import org.bukkit.entity.Player;

/**
 * Created by Jake on 1/26/14.
 */
public class PlayerSettings {
    /**
     * Settings Registry
     */
    private static final BaseSettingsRegistry registry = new SettingsRegistry();

    /**
     * Retrieve the settings registry
     * @return the setting registry
     */
    public static BaseSettingsRegistry getRegistry() {
        return registry;
    }

    /**
     * Gets a settings manager for a player
     * @param player player to get manager for
     * @return a new PlayerSettingsManager for the player
     */
    public static SettingsManager getManager(Player player) {
        return new PlayerSettingsManager(Gearz.getInstance(), player);
    }
}

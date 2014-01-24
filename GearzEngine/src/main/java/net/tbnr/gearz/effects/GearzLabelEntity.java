package net.tbnr.gearz.effects;

import lombok.Getter;
import lombok.Setter;
import net.tbnr.gearz.packets.FakeEntity;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * GearzLabelEntity defines a fake bat, tracks location, packets, meta, etc :D
 */
public final class GearzLabelEntity {

    /**
     * Player this is sent to
     */
    @Getter
    private final Player player;

    /**
     * The current title of the bat
     */
    @Getter @Setter
    private String title;

    /**
     * The location the bat is at
     */
    @Getter @Setter
    private final Location location;

    /**
     * The bat object, stored to track the entity ID mostly, and use the DataWatcher, which is a vanilla thing.
     */
    private FakeEntity bat;

    /**
     * Tracks if we're visible.
     */
    boolean showing = false;

    /**
     * Creates a label entity with the params
     *
     * @param player   The player to display this to
     * @param title    The title to display to the player
     * @param location The location of the entity.
     */
    public GearzLabelEntity(Player player, String title, Location location) {
        this.player = player;
        this.title = title;
        this.location = location;
        create();
    }

    public void create() {
        if (this.showing) {
            return;
        }
        bat = new FakeEntity(player, EntityType.BAT, 6, location, FakeEntity.EntityFlags.NONE);
        bat.create();
        updateMeta();
        this.showing = true;
    }

    public void updateTag(String string) {
        if (this.title.equals(string)) {
            return;
        }
        this.title = string;
        updateMeta();
    }

    private void updateMeta() {
        bat.setCustomName(this.title.substring(0, Math.min(64, this.title.length())));
    }

    public void destroy() {
        if (!this.showing) {
            return;
        }
        bat.destroy();
        this.showing = false;
    }
}
package net.tbnr.gearz.effects.entityblocks;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import lombok.Setter;
import net.tbnr.gearz.packets.wrapper.WrapperPlayServerEntityMetadata;
import net.tbnr.gearz.packets.wrapper.WrapperPlayServerSpawnEntity;
import net.tbnr.gearz.packets.wrapper.WrapperPlayServerSpawnEntity.ObjectTypes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by George on 04/02/14.
 * <p/>
 * Purpose Of File:
 * <p/>
 * Latest Change:
 */
public class EntityBlock {

	private Location location;
	@Setter
	private Material type;
	@Setter
	private byte data;
	static int entityIDLevel = 1000;
	private int entityID;
	private float yaw;
	private int offsetY;
	private float pitch;

	private EntityBlock(Location location, Material material, byte data, float yaw, float pitch, int offsetY) {
		this.location = location;
		this.type = material;
		this.data = data;
		this.yaw = yaw;
		this.pitch = pitch;
		this.offsetY = offsetY;
		if(entityIDLevel >= 500000) entityIDLevel = 1000;
		entityID = entityIDLevel++;
	}

	public int showBlock(Player player) {

		if(player.getLocation().distanceSquared(location) > 1024) return entityID;

		ProtocolManager manager = ProtocolLibrary.getProtocolManager();

		// Give the illusion of containing a portal block
		WrapperPlayServerSpawnEntity spawnVehicle = new WrapperPlayServerSpawnEntity();
		WrapperPlayServerEntityMetadata entityMeta = new WrapperPlayServerEntityMetadata();
		WrappedDataWatcher watcher = new WrappedDataWatcher();

		spawnVehicle.setEntityID(this.entityID);
		spawnVehicle.setType(ObjectTypes.MINECART);
		spawnVehicle.setX(location.getX());
		spawnVehicle.setY(location.getY());
		spawnVehicle.setZ(location.getZ());
		spawnVehicle.setYaw(yaw);
		spawnVehicle.setPitch(pitch);

		watcher.setObject(20, type.getId() | (data << 16));
		watcher.setObject(21, offsetY);
		watcher.setObject(22, (byte)1);

		// Initialize packet
		entityMeta.setEntityMetadata(watcher.getWatchableObjects());
		entityMeta.setEntityId(this.entityID);

		try {
			manager.sendServerPacket(player, spawnVehicle.getHandle());
			manager.sendServerPacket(player, entityMeta.getHandle());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		entityIDLevel++;
		return entityID;
	}

	/*public static void destroyBlock(Player p) {

		if()
		WrapperPlayServerEntityDestroy destroyVehicle = new WrapperPlayServerEntityDestroy();
	}*/

	public static EntityBlock newBlock(Location location, Material material, byte data, float yaw, float pitch, int offsetY) {
		return new EntityBlock(location, material, data, yaw, pitch, offsetY).register();
	}

	private EntityBlock register() {
		return EntityBlockManager.registerBlock(this);
	}
}

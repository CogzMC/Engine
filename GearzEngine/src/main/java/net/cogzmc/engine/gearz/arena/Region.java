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

package net.cogzmc.engine.gearz.arena;

import lombok.Data;
import net.cogzmc.engine.gearz.Gearz;
import net.cogzmc.engine.gearz.player.GearzPlayer;
import net.cogzmc.engine.util.Range;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Random;

@Data
public final class Region {
    private final Point maximum;
    private final Point minimum;

    public Region(Point p1, Point p2) {
        this.minimum = new Point(
                Math.min(p1.getX(), p2.getX()),
                Math.min(p1.getY(), p2.getY()),
                Math.min(p1.getZ(), p2.getZ())
        );
        this.maximum = new Point(
                Math.max(p1.getX(), p2.getX()),
                Math.max(p1.getY(), p2.getY()),
                Math.max(p1.getZ(), p2.getZ())
        );
    }

    public boolean isPlayerInRegion(GearzPlayer player) {
        return isPlayerInRegion(player.getPlayer());
    }

    public boolean isPlayerInRegion(Player player) {
        Location location = player.getLocation();
        net.cogzmc.engine.util.Range xRange = Range.in(this.minimum.getX(), this.maximum.getX());
        Range yRange = Range.in(this.minimum.getX(), this.maximum.getX());
        Range zRange = Range.in(this.minimum.getX(), this.maximum.getX());
        return xRange.isWithinRange(location.getX()) && yRange.isWithinRange(location.getY()) && zRange.isWithinRange(location.getZ());
    }

    public Point getRandomLocationInRange() {
        Random random = Gearz.getRandom();
        Double x = random.nextDouble() * (this.maximum.getX()-this.minimum.getX());
        Double y = random.nextDouble() * (this.maximum.getY()-this.minimum.getY());
        Double z = random.nextDouble() * (this.maximum.getZ()-this.minimum.getZ());
        return new Point(x, y, z);
    }
}

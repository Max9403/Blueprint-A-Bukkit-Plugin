package com.emberringstudios.blueprint;

import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author Max9403 <Max9403@live.com>
 */
public class BasicLocation {

    private final double locX;
    private final double locY;
    private final double locZ;

    public BasicLocation(final double locX, final double locY, final double locZ) {
        this.locX = locX;
        this.locY = locY;
        this.locZ = locZ;
    }

    /**
     * @return the locX
     */
    public double getX() {
        return locX;
    }

    /**
     * @return the locY
     */
    public double getY() {
        return locY;
    }

    /**
     * @return the locZ
     */
    public double getZ() {
        return locZ;
    }

    public Location convertToLocation(final World world) {
        return new Location(world, locX, locY, locZ);
    }
}

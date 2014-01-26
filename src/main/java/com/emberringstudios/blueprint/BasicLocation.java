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

    /**
     * Creates a location with having a world specified
     *
     * @param locX
     * @param locY
     * @param locZ
     */
    public BasicLocation(final double locX, final double locY, final double locZ) {
        this.locX = locX;
        this.locY = locY;
        this.locZ = locZ;
    }

    /**
     * @return The X value of the location
     */
    public double getX() {
        return locX;
    }

    /**
     * @return The Y value of the location
     */
    public double getY() {
        return locY;
    }

    /**
     * @return The Z value of the location
     */
    public double getZ() {
        return locZ;
    }

    /**
     *
     * @param world The world the location is part of
     * @return A fully formed Bukkit location
     */
    public Location convertToLocation(final World world) {
        return new Location(world, locX, locY, locZ);
    }
}

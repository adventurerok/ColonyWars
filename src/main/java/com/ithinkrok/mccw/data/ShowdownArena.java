package com.ithinkrok.mccw.data;

import com.ithinkrok.mccw.util.BoundingBox;
import org.bukkit.Location;

/**
 * Created by paul on 13/11/15.
 *
 * Represents a showdown arena
 */
public class ShowdownArena {

    private int radiusX, radiusZ;
    private Location center;
    private BoundingBox bounds;

    public ShowdownArena(int radiusX, int radiusZ, Location center, BoundingBox bounds) {
        this.radiusX = radiusX;
        this.radiusZ = radiusZ;
        this.center = center;
        this.bounds = bounds;
    }

    public int getRadiusX() {
        return radiusX;
    }

    public int getRadiusZ() {
        return radiusZ;
    }

    public Location getCenter() {
        return center;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public boolean isInBounds(Location loc){
        double xd = Math.abs(loc.getX() - center.getX());
        double zd = Math.abs(loc.getZ() - center.getZ());

        return !(xd > radiusX || zd > radiusZ);
    }
}

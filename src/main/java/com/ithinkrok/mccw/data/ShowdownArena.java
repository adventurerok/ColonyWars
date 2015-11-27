package com.ithinkrok.mccw.data;

import com.ithinkrok.mccw.handler.GameInstance;
import com.ithinkrok.mccw.util.BoundingBox;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Created by paul on 13/11/15.
 * <p>
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

    public boolean checkUserMove(User user, Location target) {
        if (isInBounds(target)) return false;

        double xv = 0;
        if (target.getX() > center.getX() + radiusX - 2) xv = -1;
        else if (target.getX() < center.getX() - radiusZ + 2) xv = 1;

        double zv = 0;
        if (target.getZ() > center.getZ() + radiusZ - 2) zv = -1;
        else if (target.getZ() < center.getZ() - radiusZ + 2) zv = 1;

        user.getPlayer().setVelocity(new Vector(xv * 0.5, 0.3, zv * 0.5));

        return true;
    }

    public boolean isInBounds(Location loc) {
        double xd = Math.abs(loc.getX() - center.getX());
        double zd = Math.abs(loc.getZ() - center.getZ());

        return !(xd > radiusX || zd > radiusZ);
    }

    public void startShrinkTask(GameInstance gameInstance){
        gameInstance.scheduleRepeatingTask(() -> {
            if(radiusX > 5) radiusX -= 1;
            if(radiusZ > 5) radiusZ -= 1;
        }, 20 * 30, 20 * 30);
    }
}

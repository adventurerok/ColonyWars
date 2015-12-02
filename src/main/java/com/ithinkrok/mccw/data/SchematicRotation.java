package com.ithinkrok.mccw.data;

import com.ithinkrok.mccw.util.BoundingBox;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Created by paul on 02/12/15.
 *
 * Handles a rotation of a schematic
 */
public class SchematicRotation {
    byte[] blocks;
    byte[] data;
    boolean xzSwap = false;
    boolean xFlip = false;
    boolean zFlip = false;
    private int rotation;
    private int width, height, length;
    private int offsetX, offsetY, offsetZ;

    public SchematicRotation(Schematic schematic, int rotation) {
        this.width = schematic.getSize().getBlockX();
        this.height = schematic.getSize().getBlockY();
        this.length = schematic.getSize().getBlockZ();
        this.offsetX = schematic.getOffset().getBlockX();
        this.offsetY = schematic.getOffset().getBlockY();
        this.offsetZ = schematic.getOffset().getBlockZ();
        this.blocks = schematic.getBlocks();
        this.data = schematic.getData();
        this.rotation = rotation;

        if (rotation == 1 || rotation == 3) xzSwap = true;
        if (rotation == 2 || rotation == 3) xFlip = true;
        if (rotation == 1 || rotation == 2) zFlip = true;
    }

    public int getRotation() {
        return rotation;
    }

    public int getBlock(int x, int y, int z) {
        return blocks[calcIndex(x, y, z)] & 0xFF;
    }

    private int calcIndex(int x, int y, int z) {
        if (xzSwap) {
            int i = x;
            x = z;
            z = i;
        }

        if (xFlip) x = width - x - 1;
        if (zFlip) z = length - z - 1;

        return width * (y * length + z) + x;
    }

    public byte getData(int x, int y, int z) {
        return data[calcIndex(x, y, z)];
    }

    public BoundingBox calcBounds(Location loc) {
        Vector minBB = new Vector(loc.getX() + getOffsetX(), loc.getY() + getOffsetY(), loc.getZ() + getOffsetZ());
        Vector maxBB = new Vector(minBB.getX() + getWidth() - 1, minBB.getY() + getHeight() - 1,
                minBB.getZ() + getLength() - 1);

        return new BoundingBox(minBB, maxBB);
    }

    public int getOffsetX() {
        int base = xzSwap ? offsetZ : offsetX;

        if (xFlip) base = 1 - base - getWidth();
        return base;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getOffsetZ() {
        int base = xzSwap ? offsetX : offsetZ;

        if (zFlip) base = 1 - base - getLength();
        return base;
    }

    public int getWidth() {
        return xzSwap ? length : width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return xzSwap ? width : length;
    }
}

package com.ithinkrok.mccw.util;

/**
 * Created by paul on 07/11/15.
 */
public class Facing {

    /**
     * Returns the facing direction from player yaw for use in building rotations.
     * NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3
     *
     * @param yaw The yaw of the player
     * @return The rotation for use in SchematicBuilder.buildSchematic()
     */
    public static int getFacing(float yaw){
        return (floor((yaw + 45f) / 90f) + 2) % 4;
    }

    private static int floor(float f){
        int i = (int) f;

        if(i > f) return i - 1;
        return i;
    }
}

package com.ithinkrok.mccw.enumeration;

import org.bukkit.Color;
import org.bukkit.DyeColor;

/**
 * Created by paul on 01/11/15.
 *
 * An enum to represent the four team colors
 */
public enum TeamColor {

    RED(Color.RED, DyeColor.RED),
    BLUE(Color.fromRGB(100, 100, 255), DyeColor.BLUE),
    GREEN(Color.fromRGB(0, 200, 0), DyeColor.GREEN),
    YELLOW(Color.fromRGB(200, 200, 0), DyeColor.YELLOW);

    public final Color armorColor;
    public final DyeColor dyeColor;

    TeamColor(Color armorColor, DyeColor dyeColor) {
        this.armorColor = armorColor;
        this.dyeColor = dyeColor;
    }
}

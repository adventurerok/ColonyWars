package com.ithinkrok.mccw.enumeration;

import org.bukkit.Color;

/**
 * Created by paul on 01/11/15.
 *
 * An enum to represent the four team colors
 */
public enum TeamColor {

    RED(Color.RED),
    BLUE(Color.fromRGB(100, 100, 255)),
    GREEN(Color.fromRGB(0, 200, 0)),
    YELLOW(Color.fromRGB(200, 200, 0));

    public final Color armorColor;

    TeamColor(Color armorColor) {
        this.armorColor = armorColor;
    }
}

package com.ithinkrok.mccw.enumeration;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

/**
 * Created by paul on 01/11/15.
 *
 * An enum to represent the four team colors
 */
public enum TeamColor {

    RED(Color.RED, DyeColor.RED, ChatColor.RED),
    BLUE(Color.fromRGB(100, 100, 255), DyeColor.BLUE, ChatColor.BLUE),
    GREEN(Color.fromRGB(0, 200, 0), DyeColor.GREEN, ChatColor.GREEN),
    YELLOW(Color.fromRGB(200, 200, 0), DyeColor.YELLOW, ChatColor.YELLOW);

    public final Color armorColor;
    public final DyeColor dyeColor;
    public final ChatColor chatColor;

    TeamColor(Color armorColor, DyeColor dyeColor, ChatColor chatColor) {
        this.armorColor = armorColor;
        this.dyeColor = dyeColor;
        this.chatColor = chatColor;
    }
}

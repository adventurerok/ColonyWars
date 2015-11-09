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

    RED(ChatColor.RED + "Red", Color.RED, DyeColor.RED, ChatColor.RED),
    BLUE(ChatColor.BLUE + "Blue", Color.fromRGB(100, 100, 255), DyeColor.BLUE, ChatColor.BLUE),
    GREEN(ChatColor.GREEN + "Green", Color.fromRGB(0, 200, 0), DyeColor.GREEN, ChatColor.GREEN),
    YELLOW(ChatColor.YELLOW + "Yellow", Color.fromRGB(200, 200, 0), DyeColor.YELLOW, ChatColor.YELLOW);

    public final Color armorColor;
    public final DyeColor dyeColor;
    public final ChatColor chatColor;
    public final String name;

    TeamColor(String name, Color armorColor, DyeColor dyeColor, ChatColor chatColor) {
        this.name = name;
        this.armorColor = armorColor;
        this.dyeColor = dyeColor;
        this.chatColor = chatColor;
    }
}

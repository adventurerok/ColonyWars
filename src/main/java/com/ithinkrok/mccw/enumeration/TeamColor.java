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

    RED(ChatColor.RED + "Red", DyeColor.RED, ChatColor.RED),
    BLUE(ChatColor.BLUE + "Blue", DyeColor.BLUE, ChatColor.BLUE),
    GREEN(ChatColor.GREEN + "Green", DyeColor.GREEN, ChatColor.GREEN),
    YELLOW(ChatColor.YELLOW + "Yellow", DyeColor.YELLOW, ChatColor.YELLOW);

    public final Color armorColor;
    public final DyeColor dyeColor;
    public final ChatColor chatColor;
    public final String name;

    TeamColor(String name, DyeColor dyeColor, ChatColor chatColor) {
        this.name = name;
        this.armorColor = dyeColor.getColor();
        this.dyeColor = dyeColor;
        this.chatColor = chatColor;
    }

    public static TeamColor fromWoolColor(short woolColor) {
        for(TeamColor c : values()){
            if(c.dyeColor.getWoolData() == woolColor) return c;
        }

        return null;
    }
}

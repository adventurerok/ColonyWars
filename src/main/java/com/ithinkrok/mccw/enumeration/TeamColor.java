package com.ithinkrok.mccw.enumeration;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

/**
 * Created by paul on 01/11/15.
 *
 * An enum to represent the four team colors
 */
public enum TeamColor {

    RED(DyeColor.RED, ChatColor.RED),
    BLUE(DyeColor.BLUE, ChatColor.BLUE),
    GREEN(DyeColor.GREEN, ChatColor.GREEN),
    YELLOW(DyeColor.YELLOW, ChatColor.YELLOW);

    public final Color armorColor;
    public final DyeColor dyeColor;
    public final ChatColor chatColor;
    public final String name;

    TeamColor(DyeColor dyeColor, ChatColor chatColor) {
        this.name = chatColor + WordUtils.capitalizeFully(dyeColor.name());
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

package com.ithinkrok.mccw.enumeration;

import com.ithinkrok.mccw.util.DyeToChatColorConverter;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by paul on 01/11/15.
 *
 * An enum to represent the four team colors
 */
public class TeamColor {

    private static List<TeamColor> teamColorList;

    public final Color armorColor;
    public final DyeColor dyeColor;
    public final ChatColor chatColor;
    public final String name;

    private TeamColor(DyeColor dyeColor) {
        this.chatColor = DyeToChatColorConverter.convert(dyeColor);
        this.name = chatColor + WordUtils.capitalizeFully(dyeColor.name());
        this.armorColor = dyeColor.getColor();
        this.dyeColor = dyeColor;
    }

    public static void initialise(int teamCount){
        Validate.isTrue(teamCount > 1, "You must have 2 teams or more for there to be a game");
        Validate.isTrue(teamCount <= 16, "You cannot have more than 16 teams");

        DyeColor[] dyeColors = new DyeColor[16];
        dyeColors[0] = DyeColor.RED;
        dyeColors[1] = DyeColor.BLUE;
        dyeColors[2] = DyeColor.GREEN;
        dyeColors[3] = DyeColor.YELLOW;
        dyeColors[4] = DyeColor.ORANGE;
        dyeColors[5] = DyeColor.LIGHT_BLUE;
        dyeColors[6] = DyeColor.LIME;
        dyeColors[7] = DyeColor.PURPLE;
        dyeColors[8] = DyeColor.CYAN;
        dyeColors[9] = DyeColor.PINK;
        dyeColors[10] = DyeColor.MAGENTA;
        dyeColors[11] = DyeColor.BROWN;
        dyeColors[12] = DyeColor.BLACK;
        dyeColors[13] = DyeColor.WHITE;
        dyeColors[14] = DyeColor.GRAY;
        dyeColors[15] = DyeColor.SILVER;

        List<TeamColor> teamColors = new ArrayList<>();

        for(int team = 0; team < teamCount; ++team){
            teamColors.add(new TeamColor(dyeColors[team]));
        }

        teamColorList = Collections.unmodifiableList(teamColors);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamColor teamColor = (TeamColor) o;

        return dyeColor == teamColor.dyeColor;

    }

    public static TeamColor fromName(String name) {
        for(TeamColor teamColor : teamColorList) {
            if(teamColor.name.equalsIgnoreCase(name)) return teamColor;
        }

        return null;
    }

    @Override
    public int hashCode() {
        return dyeColor.hashCode();
    }

    public static List<TeamColor> values() {
        return teamColorList;
    }

    public static TeamColor fromWoolColor(short woolColor) {
        for(TeamColor c : values()){
            if(c.dyeColor.getWoolData() == woolColor) return c;
        }

        return null;
    }
}

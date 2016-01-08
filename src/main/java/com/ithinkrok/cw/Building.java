package com.ithinkrok.cw;

import com.ithinkrok.minigames.TeamIdentifier;
import com.ithinkrok.minigames.schematic.PastedSchematic;

/**
 * Created by paul on 08/01/16.
 */
public class Building {

    private String buildingName;
    private TeamIdentifier teamIdentifier;

    private PastedSchematic schematic;

    public Building(String buildingName, TeamIdentifier teamIdentifier, PastedSchematic schematic) {
        this.buildingName = buildingName;
        this.teamIdentifier = teamIdentifier;
        this.schematic = schematic;
    }

    public PastedSchematic getSchematic() {
        return schematic;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public TeamIdentifier getTeamIdentifier() {
        return teamIdentifier;
    }

    public void explode() {
        schematic.explode();
    }

    public void remove() {
        schematic.remove();
    }
}

package com.ithinkrok.cw;

import com.ithinkrok.minigames.team.TeamIdentifier;
import com.ithinkrok.minigames.inventory.ClickableInventory;
import com.ithinkrok.minigames.schematic.PastedSchematic;
import com.ithinkrok.minigames.util.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

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

    public ConfigurationSection getConfig() {
        return schematic.getConfig();
    }

    public Location getCenterBlock() {
        return schematic.getCenterBlock();
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

    public ClickableInventory createShop() {
        ConfigurationSection config = getConfig();
        if(config == null || !config.contains("shop")) return null;

        List<ConfigurationSection> shopItems = ConfigUtils.getConfigList(config, "shop");

        ClickableInventory inv = new ClickableInventory(buildingName);
        inv.loadFromConfig(shopItems);

        return inv;
    }

    public boolean isFinished() {
        return schematic.isFinished();
    }

    public boolean isProtected() {
        return getConfig() != null && getConfig().getBoolean("protected");
    }
}

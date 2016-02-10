package com.ithinkrok.cw;

import com.ithinkrok.minigames.base.inventory.ClickableInventory;
import com.ithinkrok.minigames.base.schematic.PastedSchematic;
import com.ithinkrok.minigames.base.team.TeamIdentifier;
import com.ithinkrok.msm.common.util.ConfigUtils;
import com.ithinkrok.util.config.Config;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * Created by paul on 08/01/16.
 */
public class Building {

    private final String buildingName;
    private final TeamIdentifier teamIdentifier;

    private final PastedSchematic schematic;

    public Building(String buildingName, TeamIdentifier teamIdentifier, PastedSchematic schematic) {
        this.buildingName = buildingName;
        this.teamIdentifier = teamIdentifier;
        this.schematic = schematic;
    }

    public Config getConfig() {
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
        Config config = getConfig();
        if(config == null || !config.contains("shop")) return null;

        List<Config> shopItems = config.getConfigList("shop");

        ClickableInventory inv = new ClickableInventory(buildingName);
        inv.loadFromConfig(shopItems);

        return inv;
    }

    public boolean isFinished() {
        return schematic.isFinished();
    }

    public boolean isRemoved() {
        return schematic.isRemoved();
    }

    public boolean isProtected() {
        return getConfig() != null && getConfig().getBoolean("protected");
    }
}

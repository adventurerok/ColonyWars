package com.ithinkrok.mccw.util.item;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.playerclass.items.LinearCalculator;
import com.ithinkrok.mccw.strings.Buildings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 27/11/15.
 * <p>
 * Handles the team compass object
 */
public class TeamCompass implements ClassItem.InteractAction {

    private WarsPlugin plugin;

    public TeamCompass(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    public static ClassItem createTeamCompass(WarsPlugin plugin) {
        ClassItem compass = new ClassItem(plugin.getLangFile(), Material.COMPASS, "items.team-compass.name");

        compass.withUpgradeBuildings(Buildings.CATHEDRAL);
        compass.withRightClickAction(new TeamCompass(plugin));
        compass.withUpgradables(new ClassItem.Upgradable("team-compass", "items.team-compass.name", 1,
                new LinearCalculator(plugin.getWarsConfig().getBuildingItemCost(Buildings.CATHEDRAL, "team-compass"),
                        0)));

        return compass;
    }

    @Override
    public boolean onInteractWorld(UserInteractEvent event) {
        TeamColor target = TeamColor.values().get(0);

        Object lastObject = event.getUser().getMetadata("team-compass");
        if (lastObject != null && lastObject instanceof TeamColor) target = (TeamColor) lastObject;

        target = TeamColor.values().get((TeamColor.values().indexOf(target) + 1) % TeamColor.values().size());

        Location targetLocation = plugin.getMapSpawn(target);

        event.getUser().setMetadata("team-compass", target, true);

        if (targetLocation != null) event.getPlayer().setCompassTarget(targetLocation);

        int compassIndex = event.getUserInventory().first(Material.COMPASS);
        ItemStack newCompass = event.getUserInventory().getItem(compassIndex);

        InventoryUtils
                .setItemNameAndLore(newCompass, target.getChatColor() + plugin.getLocale("items.team-compass.name"),
                        plugin.getLocale("items.team-compass.oriented", target.getFormattedName()));

        event.getUserInventory().setItem(compassIndex, newCompass);

        return true;
    }
}

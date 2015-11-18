package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.*;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 12/11/15.
 *
 * Handles the peasant class
 */
public class PeasantClass extends BuyableInventory implements PlayerClassHandler {

    public PeasantClass(FileConfiguration config) {
        super(new UpgradeBuyable(InventoryUtils
                .createItemWithEnchantments(Material.WOOD_SWORD, 1, 0, "Sharpness Upgrade 1", null,
                        Enchantment.DAMAGE_ALL, 1), Buildings.LUMBERMILL, config.getInt("costs.peasant.sharpness1"),
                "sharpness", 1), new UpgradeBuyable(InventoryUtils
                .createItemWithEnchantments(Material.WOOD_SWORD, 1, 0, "Sharpness Upgrade 2", null,
                        Enchantment.DAMAGE_ALL, 2), Buildings.LUMBERMILL, config.getInt("costs.peasant.sharpness2"),
                "sharpness", 2), new UpgradeBuyable(InventoryUtils
                .createItemWithEnchantments(Material.WOOD_SWORD, 1, 0, "Knockback Upgrade 1", null,
                        Enchantment.KNOCKBACK, 1), Buildings.LUMBERMILL, config.getInt("costs.peasant.knockback1"),
                "knockback", 1), new UpgradeBuyable(InventoryUtils
                .createItemWithEnchantments(Material.WOOD_SWORD, 1, 0, "Knockback Upgrade 2", null,
                        Enchantment.KNOCKBACK, 2), Buildings.LUMBERMILL, config.getInt("costs.peasant.knockback2"),
                "knockback", 2));
    }

    @Override
    public void onBuildingBuilt(UserTeamBuildingBuiltEvent event) {
        if(!Buildings.LUMBERMILL.equals(event.getBuilding().getBuildingName())) return;

        event.getUserInventory().addItem(new ItemStack(Material.WOOD_SWORD));
    }

    @Override
    public void onUserBeginGame(UserBeginGameEvent event) {
        event.getUserInventory().addItem(InventoryUtils.createItemWithEnchantments(Material.IRON_AXE,
                1, 0, "Peasant Axe", "Better than a usual axe!", Enchantment.DIG_SPEED, 2));
    }

    @Override
    public boolean onInteractWorld(UserInteractEvent event) {
        return false;
    }

    @Override
    public void onPlayerUpgrade(UserUpgradeEvent event) {
        User user = event.getUser();

        switch (event.getUpgradeName()) {
            case "sharpness":
            case "knockback":
                ItemStack sword = new ItemStack(Material.WOOD_SWORD);
                InventoryUtils.enchantItem(sword, Enchantment.DAMAGE_ALL, user.getUpgradeLevel("sharpness"),
                        Enchantment.KNOCKBACK, user.getUpgradeLevel("knockback"));

                InventoryUtils.replaceItem(event.getUserInventory(), sword);

                break;
        }
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {

    }
}

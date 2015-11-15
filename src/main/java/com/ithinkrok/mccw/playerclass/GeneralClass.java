package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.UserAttackEvent;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.event.UserUpgradeEvent;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 05/11/15.
 * <p>
 * Handles the General class
 */
public class GeneralClass extends BuyableInventory implements PlayerClassHandler {


    public GeneralClass(FileConfiguration config) {
        super(new UpgradeBuyable(InventoryUtils
                .createItemWithEnchantments(Material.DIAMOND_SWORD, 1, 0, "Sword Upgrade 1", null,
                        Enchantment.DAMAGE_ALL, 1, Enchantment.KNOCKBACK, 5), Buildings.BLACKSMITH,
                config.getInt("costs.general.sword1"), "sword", 1), new UpgradeBuyable(InventoryUtils
                .createItemWithEnchantments(Material.DIAMOND_SWORD, 1, 0, "Sword Upgrade 2", null,
                        Enchantment.DAMAGE_ALL, 2, Enchantment.KNOCKBACK, 5), Buildings.BLACKSMITH,
                config.getInt("costs.general.sword2"), "sword", 2));
    }


    @Override
    public void onBuildingBuilt(String buildingName, User user, Team team) {
        if (!Buildings.BLACKSMITH.equals(buildingName)) return;

        user.setUpgradeLevel("sword", 0);
    }

    @Override
    public void onGameBegin(User user, Team team) {

    }

    @Override
    public boolean onInteractWorld(UserInteractEvent event) {
        return false;
    }

    @Override
    public void onPlayerUpgrade(UserUpgradeEvent event) {
        switch (event.getUpgradeName()) {
            case "sword":
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                InventoryUtils
                        .enchantItem(sword, Enchantment.DAMAGE_ALL, event.getUpgradeLevel(), Enchantment.KNOCKBACK, 5);

                InventoryUtils.replaceItem(event.getUserInventory(), sword);
                break;
        }
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {

    }
}

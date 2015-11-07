package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
                config.getInt("costs.general.sword1"), "sword", 1),
                new UpgradeBuyable(InventoryUtils
                        .createItemWithEnchantments(Material.DIAMOND_SWORD, 1, 0, "Sword Upgrade 2", null,
                                Enchantment.DAMAGE_ALL, 2, Enchantment.KNOCKBACK, 5), Buildings.BLACKSMITH,
                        config.getInt("costs.general.sword2"), "sword", 2));
    }


    @Override
    public void onBuildingBuilt(String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo) {
        if (!Buildings.BLACKSMITH.equals(buildingName)) return;

        PlayerInventory inv = playerInfo.getPlayer().getInventory();

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        InventoryUtils.enchantItem(sword, Enchantment.KNOCKBACK, 5);

        inv.addItem(sword);
    }

    @Override
    public void onGameBegin(PlayerInfo playerInfo, TeamInfo teamInfo) {

    }

    @Override
    public void onInteractWorld(PlayerInteractEvent event) {

    }

    @Override
    public void onPlayerUpgrade(PlayerInfo playerInfo, String upgradeName, int upgradeLevel) {
        switch (upgradeName) {
            case "sword":
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
                InventoryUtils.enchantItem(sword, Enchantment.DAMAGE_ALL, upgradeLevel, Enchantment.KNOCKBACK, 5);

                InventoryUtils.replaceItem(playerInfo.getPlayer().getInventory(), sword);
                break;
        }
    }
}

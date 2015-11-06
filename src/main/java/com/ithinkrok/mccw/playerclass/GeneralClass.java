package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.data.BuildingInfo;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

/**
 * Created by paul on 05/11/15.
 */
public class GeneralClass implements PlayerClassHandler {

    private int sword1Cost = 1350;
    private int sword2Cost = 1600;

    @Override
    public void addInventoryItems(List<ItemStack> inventory, BuildingInfo buildingInfo, PlayerInfo playerInfo,
                                  TeamInfo teamInfo) {
        if (!Buildings.BLACKSMITH.equals(buildingInfo.getBuildingName())) return;

        switch (playerInfo.getUpgradeLevel("sword")) {
            case 0:
                inventory.add(InventoryUtils
                        .createShopItemWithEnchantments(Material.DIAMOND_SWORD, 1, 0, "Sword Upgrade 1", null,
                                sword1Cost, false, Enchantment.DAMAGE_ALL, 1, Enchantment.KNOCKBACK, 5));
                break;
            case 1:
                inventory.add(InventoryUtils
                        .createShopItemWithEnchantments(Material.DIAMOND_SWORD, 1, 0, "Sword Upgrade 2", null,
                                sword2Cost, false, Enchantment.DAMAGE_ALL, 2, Enchantment.KNOCKBACK, 5));
                break;
        }

    }

    @Override
    public boolean onInventoryClick(ItemStack item, BuildingInfo buildingInfo, PlayerInfo playerInfo, TeamInfo teamInfo) {
        if(item.getItemMeta() == null || !item.getItemMeta().hasDisplayName()) return false;

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        int cost;
        int upgrade;

        switch(item.getItemMeta().getDisplayName()){
            case "Sword Upgrade 1":
                cost = sword1Cost;
                upgrade = 1;
                InventoryUtils.enchantItem(sword, Enchantment.DAMAGE_ALL, 1, Enchantment.KNOCKBACK, 5);
                break;
            case "Sword Upgrade 2":
                cost = sword2Cost;
                upgrade = 2;
                InventoryUtils.enchantItem(sword, Enchantment.DAMAGE_ALL, 2, Enchantment.KNOCKBACK, 5);
                break;
            default:
                return false;
        }

        if(!InventoryUtils.checkUpgradeAndTryCharge(playerInfo, cost, "sword", upgrade)) return true;

        PlayerInventory inv = playerInfo.getPlayer().getInventory();

        inv.setItem(inv.first(Material.DIAMOND_SWORD), sword);
        playerInfo.setUpgradeLevel("sword", upgrade);

        InventoryUtils.playBuySound(playerInfo.getPlayer());

        playerInfo.recalculateInventory();

        return true;
    }

    @Override
    public void onBuildingBuilt(String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo) {
        if(!Buildings.BLACKSMITH.equals(buildingName)) return;

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

    }
}

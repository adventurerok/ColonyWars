package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.inventory.ItemBuyable;
import com.ithinkrok.mccw.inventory.ItemPurchaseEvent;
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
 * Created by paul on 10/11/15.
 * <p>
 * Handles the archer class
 */
public class ArcherClass extends BuyableInventory implements PlayerClassHandler {

    public ArcherClass(FileConfiguration config) {
        super(new UpgradeBuyable(InventoryUtils
                        .createItemWithEnchantments(Material.BOW, 1, 0, "Bow Upgrade 1", null, Enchantment.ARROW_KNOCKBACK, 1,
                                Enchantment.ARROW_DAMAGE, 1), Buildings.LUMBERMILL, config.getInt("costs.archer.bow1"), "bow",
                        1), new UpgradeBuyable(InventoryUtils
                        .createItemWithEnchantments(Material.BOW, 1, 0, "Bow Upgrade 2", null, Enchantment.ARROW_KNOCKBACK, 2,
                                Enchantment.ARROW_DAMAGE, 3), Buildings.LUMBERMILL, config.getInt("costs.archer.bow2"), "bow",
                        2), new UpgradeBuyable(InventoryUtils
                        .createItemWithEnchantments(Material.WOOD_SWORD, 1, 0, "Sword Upgrade 1", null, Enchantment.DAMAGE_ALL,
                                1, Enchantment.KNOCKBACK, 1), Buildings.LUMBERMILL, config.getInt("costs.archer.sword1"),
                        "sword", 1), new UpgradeBuyable(InventoryUtils
                        .createItemWithEnchantments(Material.WOOD_SWORD, 1, 0, "Sword Upgrade 2", null, Enchantment.DAMAGE_ALL,
                                2, Enchantment.KNOCKBACK, 2), Buildings.LUMBERMILL, config.getInt("costs.archer.sword2"),
                        "sword", 2),
                new UpgradeBuyable(InventoryUtils.createItemWithNameAndLore(Material.ARROW, 64, 0, "Arrow Upgrade 1"),
                        Buildings.LUMBERMILL, config.getInt("costs.archer.arrows1"), "arrows", 1), new ItemBuyable(
                        InventoryUtils.createItemWithNameAndLore(Material.ARROW, 1, 0, "Arrow Upgrade 2",
                                "Gives you 192 arrows. Can be purchased multiple times"),
                        new ItemStack(Material.ARROW, 64), Buildings.LUMBERMILL, config.getInt("costs.archer.arrows1"),
                        false, true) {
                    @Override
                    public void onPurchase(ItemPurchaseEvent event) {
                        for (int i = 0; i < 3; ++i) super.onPurchase(event);
                    }

                    @Override
                    public boolean canBuy(ItemPurchaseEvent event) {
                        return super.canBuy(event) && event.getPlayerInfo().getUpgradeLevel("arrows") > 0;
                    }
                });
    }


    @Override
    public void onBuildingBuilt(String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo) {
        if (!Buildings.LUMBERMILL.equals(buildingName)) return;

        PlayerInventory inv = playerInfo.getPlayer().getInventory();

        inv.addItem(new ItemStack(Material.BOW));
        inv.addItem(new ItemStack(Material.WOOD_SWORD));
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
            case "bow":
                int power = upgradeLevel == 2 ? 3 : 1;
                ItemStack bow = new ItemStack(Material.BOW);
                InventoryUtils
                        .enchantItem(bow, Enchantment.ARROW_KNOCKBACK, upgradeLevel, Enchantment.ARROW_DAMAGE, power);
                InventoryUtils.replaceItem(playerInfo.getPlayer().getInventory(), bow);
                break;
            case "sword":
                ItemStack sword = new ItemStack(Material.WOOD_SWORD);
                InventoryUtils
                        .enchantItem(sword, Enchantment.DAMAGE_ALL, upgradeLevel, Enchantment.KNOCKBACK, upgradeLevel);
                InventoryUtils.replaceItem(playerInfo.getPlayer().getInventory(), sword);
                break;
            case "arrows":
                playerInfo.getPlayer().getInventory().addItem(new ItemStack(Material.ARROW, 64));
                break;
        }
    }
}

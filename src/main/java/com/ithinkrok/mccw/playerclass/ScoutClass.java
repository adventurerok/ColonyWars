package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Created by paul on 05/11/15.
 */
public class ScoutClass implements PlayerClassHandler {

    private int sword1Cost = 1350;
    private int sword2Cost = 1600;

    private WarsPlugin plugin;

    public ScoutClass(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void addExtraInventoryItems(List<ItemStack> inventory, String buildingName, PlayerInfo playerInfo,
                                       TeamInfo teamInfo) {
        switch(buildingName){
            case Buildings.LUMBERMILL:

                switch(playerInfo.getUpgradeLevel("sharpness")){
                    case 0:
                        inventory.add(InventoryUtils
                                .createShopItemWithEnchantments(Material.WOOD_SWORD, 1, 0, "Sharpness Upgrade 1", null,
                                        sword1Cost, false, Enchantment.DAMAGE_ALL, 1));
                        break;
                    case 1:
                        inventory.add(InventoryUtils
                                .createShopItemWithEnchantments(Material.WOOD_SWORD, 1, 0, "Sharpness Upgrade 2", null,
                                        sword2Cost, false, Enchantment.DAMAGE_ALL, 2));
                        break;
                }

                switch(playerInfo.getUpgradeLevel("knockback")){
                    case 0:
                        inventory.add(InventoryUtils
                                .createShopItemWithEnchantments(Material.WOOD_SWORD, 1, 0, "Knockback Upgrade 1", null,
                                        sword1Cost, false, Enchantment.KNOCKBACK, 1));
                        break;
                    case 1:
                        inventory.add(InventoryUtils
                                .createShopItemWithEnchantments(Material.WOOD_SWORD, 1, 0, "Knockback Upgrade 2", null,
                                        sword2Cost, false, Enchantment.KNOCKBACK, 2));
                        break;
                }
        }
    }

    @Override
    public boolean onInventoryClick(ItemStack item, String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo) {
        if(item.getItemMeta() == null || !item.getItemMeta().hasDisplayName()) return false;

        int cost;
        int upgrade;
        String upgradeName;

        switch(item.getItemMeta().getDisplayName()){
            case "Sharpness Upgrade 1":
                cost = sword1Cost;
                upgrade = 1;
                upgradeName = "sharpness";
                break;
            case "Sharpness Upgrade 2":
                cost = sword2Cost;
                upgrade = 2;
                upgradeName = "sharpness";
                break;
            case "Knockback Upgrade 1":
                cost = sword1Cost;
                upgrade = 1;
                upgradeName = "knockback";
                break;
            case "Knockback Upgrade 2":
                cost = sword2Cost;
                upgrade = 2;
                upgradeName = "knockback";
                break;
            default:
                return false;
        }

        if(!InventoryUtils.checkUpgradeAndTryCharge(playerInfo, cost, upgradeName, upgrade)) return true;
        playerInfo.setUpgradeLevel(upgradeName, upgrade);

        ItemStack sword = new ItemStack(Material.WOOD_SWORD);
        InventoryUtils.enchantItem(sword, Enchantment.DAMAGE_ALL, playerInfo.getUpgradeLevel("sharpness"),
                Enchantment.KNOCKBACK, playerInfo.getUpgradeLevel("knockback"));

        PlayerInventory inv = playerInfo.getPlayer().getInventory();

        inv.setItem(inv.first(Material.WOOD_SWORD), sword);

        InventoryUtils.playBuySound(playerInfo.getPlayer());

        playerInfo.recalculateInventory();

        return true;
    }

    @Override
    public void onBuildingBuilt(String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo) {
        PlayerInventory inv = playerInfo.getPlayer().getInventory();

        switch(buildingName){
            case Buildings.LUMBERMILL:
                inv.addItem(new ItemStack(Material.WOOD_SWORD));
                inv.addItem(InventoryUtils.createItemWithNameAndLore(Material.COMPASS, 1, 0, "Player Compass",
                        "Oriented at: No One"));

                break;
        }
    }

    @Override
    public void onGameBegin(PlayerInfo playerInfo, TeamInfo teamInfo) {
        playerInfo.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1), true);
    }

    @Override
    public void onInteractWorld(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if(item == null) return;

        switch(item.getType()){
            case COMPASS:
                TeamColor exclude = plugin.getPlayerInfo(event.getPlayer()).getTeamColor();
                plugin.updateScoutCompass(item, event.getPlayer(), exclude);
                break;
        }


    }
}

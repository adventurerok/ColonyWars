package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.data.BuildingInfo;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 04/11/15.
 * <p>
 * InventoryHandler for base shops
 */
public class BaseInventory implements InventoryHandler {

    private int farmCost = 3000;
    private int lumbermillCost = 2000;
    private int blacksmithCost = 4000;
    private int magetowerCost = 4000;
    private int churchCost = 4000;

    @Override
    public boolean onInventoryClick(ItemStack item, BuildingInfo buildingInfo, PlayerInfo playerInfo, TeamInfo teamInfo) {
        PlayerInventory inv = playerInfo.getPlayer().getInventory();

        if (inv.firstEmpty() == -1) {
            playerInfo.getPlayer().sendMessage("Please ensure you have one free slot in your inventory");
            return true;
        }

        ItemStack add = null;
        int cost = 0;

        switch (item.getItemMeta().getDisplayName()) {
            case Buildings.FARM:
                cost = farmCost;
                add = InventoryUtils
                        .createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, Buildings.FARM, "Builds a farm when placed!");
                break;
            case Buildings.LUMBERMILL:
                cost = lumbermillCost;
                add = InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, Buildings.LUMBERMILL,
                        "Builds a lumbermill when placed!");
                break;
            case Buildings.BLACKSMITH:
                cost = blacksmithCost;
                add = InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, Buildings.BLACKSMITH,
                        "Builds a blacksmith when placed!");
                break;
            case Buildings.MAGETOWER:
                cost = magetowerCost;
                add = InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, Buildings.MAGETOWER,
                        "Builds a MageTower when placed!");
                break;
            case Buildings.CHURCH:
                cost = churchCost;
                add = InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 1, 0, Buildings.CHURCH,
                        "Builds a MageTower when placed!");
                break;
        }

        if (cost == 0 || add == null) return false;

        if (!InventoryUtils.hasTeamCash(cost, teamInfo, playerInfo)) {
            playerInfo.getPlayer().sendMessage("You don't have that amount of money!");
            return true;
        }

        inv.addItem(add);

        InventoryUtils.payWithTeamCash(cost, teamInfo, playerInfo);
        InventoryUtils.playBuySound(playerInfo.getPlayer());
        return true;
    }

    @Override
    public List<ItemStack> getInventoryContents(BuildingInfo buildingInfo, PlayerInfo playerInfo, TeamInfo teamInfo) {
        ArrayList<ItemStack> result = new ArrayList<>();
        result.add(InventoryUtils.createShopItem(Material.LAPIS_ORE, 1, 0, Buildings.FARM, "Build a farm!", farmCost, true));

        if (teamInfo.getBuildingCount(Buildings.FARM) > 0) {
            result.add(InventoryUtils
                    .createShopItem(Material.LAPIS_ORE, 1, 0, Buildings.LUMBERMILL, "Build a lumbermill!", lumbermillCost,
                            true));
            result.add(InventoryUtils
                    .createShopItem(Material.LAPIS_ORE, 1, 0, Buildings.BLACKSMITH, "Build a blacksmith!", blacksmithCost,
                            true));
            result.add(InventoryUtils
                    .createShopItem(Material.LAPIS_ORE, 1, 0, Buildings.MAGETOWER, "Build a MageTower!", magetowerCost, true));
            result.add(InventoryUtils
                    .createShopItem(Material.LAPIS_ORE, 1, 0, Buildings.CHURCH, "Build a church!", churchCost, true));
        }
        return result;
    }
}

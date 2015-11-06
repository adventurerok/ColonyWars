package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.data.BuildingInfo;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.strings.Buildings;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by paul on 04/11/15.
 * <p>
 * InventoryHandler for base shops
 */
public class BaseInventory implements InventoryHandler {

    private InventoryHandler noFarm;
    private InventoryHandler withFarm;

    public BaseInventory() {
        int farmCost = 3000;

        noFarm = new BuyableInventory(new BuildingBuyable(Buildings.FARM, Buildings.BASE, farmCost));

        int lumbermillCost = 2000;
        int blacksmithCost = 4000;
        int magetowerCost = 4000;
        int churchCost = 4000;
        int greenhouseCost = 2000;

        withFarm = new BuyableInventory(new BuildingBuyable(Buildings.FARM, Buildings.BASE, farmCost), new
                BuildingBuyable(Buildings.LUMBERMILL, Buildings.BASE, lumbermillCost), new BuildingBuyable(Buildings
                .BLACKSMITH, Buildings.BASE, blacksmithCost), new BuildingBuyable(Buildings.MAGETOWER, Buildings
                .BASE, magetowerCost), new BuildingBuyable(Buildings.CHURCH, Buildings.BASE, churchCost), new
                BuildingBuyable(Buildings.GREENHOUSE, Buildings.BASE, greenhouseCost));
    }

    @Override
    public boolean onInventoryClick(ItemStack item, BuildingInfo buildingInfo, PlayerInfo playerInfo,
                                    TeamInfo teamInfo) {
        if(teamInfo.getBuildingCount(Buildings.FARM) > 0){
            return withFarm.onInventoryClick(item, buildingInfo, playerInfo, teamInfo);
        } else return noFarm.onInventoryClick(item, buildingInfo, playerInfo, teamInfo);
    }

    @Override
    public void addInventoryItems(List<ItemStack> result, BuildingInfo buildingInfo, PlayerInfo playerInfo,
                                  TeamInfo teamInfo) {
        if(teamInfo.getBuildingCount(Buildings.FARM) > 0){
            withFarm.addInventoryItems(result, buildingInfo, playerInfo, teamInfo);
        } else noFarm.addInventoryItems(result, buildingInfo, playerInfo, teamInfo);
    }
}

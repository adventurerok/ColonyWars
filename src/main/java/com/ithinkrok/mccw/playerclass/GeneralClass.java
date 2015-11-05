package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by paul on 05/11/15.
 */
public class GeneralClass implements PlayerClassHandler {
    @Override
    public void addExtraInventoryItems(List<ItemStack> inventory, String buildingName, PlayerInfo playerInfo,
                                       TeamInfo teamInfo) {

    }

    @Override
    public boolean onInventoryClick(ItemStack item, String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo) {
        return false;
    }

    @Override
    public void onBuildingBuilt(String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo) {

    }

    @Override
    public void onGameBegin(PlayerInfo playerInfo, TeamInfo teamInfo) {

    }
}

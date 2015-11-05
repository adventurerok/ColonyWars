package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Created by paul on 05/11/15.
 *
 * Interface to handle operations that depend on the player's class
 */
public interface PlayerClassHandler {

    void addExtraInventoryItems(List<ItemStack> inventory, String buildingName, PlayerInfo playerInfo, TeamInfo
            teamInfo);

    boolean onInventoryClick(ItemStack item, String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo);

    void onBuildingBuilt(String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo);

    void onGameBegin(PlayerInfo playerInfo, TeamInfo teamInfo);
}

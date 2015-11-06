package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.inventory.InventoryHandler;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Created by paul on 05/11/15.
 *
 * Interface to handle operations that depend on the player's class
 */
public interface PlayerClassHandler extends InventoryHandler {

    void onBuildingBuilt(String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo);

    void onGameBegin(PlayerInfo playerInfo, TeamInfo teamInfo);

    void onInteractWorld(PlayerInteractEvent event);

    void onPlayerUpgrade(PlayerInfo playerInfo, String upgradeName, int upgradeLevel);
}

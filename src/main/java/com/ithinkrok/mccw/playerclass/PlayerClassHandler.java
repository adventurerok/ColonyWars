package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.event.UserUpgradeEvent;
import com.ithinkrok.mccw.inventory.InventoryHandler;

/**
 * Created by paul on 05/11/15.
 *
 * Interface to handle operations that depend on the player's class
 */
public interface PlayerClassHandler extends InventoryHandler {

    void onBuildingBuilt(String buildingName, User user, Team team);

    void onGameBegin(User user, Team team);

    void onInteractWorld(UserInteractEvent event);

    void onPlayerUpgrade(UserUpgradeEvent event);
}

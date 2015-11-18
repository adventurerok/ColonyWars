package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.event.*;
import com.ithinkrok.mccw.inventory.InventoryHandler;

/**
 * Created by paul on 05/11/15.
 *
 * Interface to handle operations that depend on the player's class
 */
public interface PlayerClassHandler extends InventoryHandler {

    void onBuildingBuilt(UserTeamBuildingBuiltEvent event);

    void onUserBeginGame(UserBeginGameEvent event);

    boolean onInteractWorld(UserInteractEvent event);

    void onPlayerUpgrade(UserUpgradeEvent event);

    void onUserAttack(UserAttackEvent event);
}

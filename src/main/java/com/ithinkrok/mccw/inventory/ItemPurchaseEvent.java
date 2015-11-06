package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.data.BuildingInfo;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

/**
 * Created by paul on 06/11/15.
 */
public class ItemPurchaseEvent {

    private PlayerInfo playerInfo;
    private TeamInfo getTeamInfo;
    private BuildingInfo getBuildingInfo;

    public ItemPurchaseEvent(BuildingInfo getBuildingInfo, PlayerInfo playerInfo, TeamInfo getTeamInfo) {
        this.getBuildingInfo = getBuildingInfo;
        this.playerInfo = playerInfo;
        this.getTeamInfo = getTeamInfo;
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public BuildingInfo getGetBuildingInfo() {
        return getBuildingInfo;
    }

    public TeamInfo getGetTeamInfo() {
        return getTeamInfo;
    }

    public Player getPlayer(){
        return playerInfo.getPlayer();
    }

    public PlayerInventory getPlayerInventory(){
        return playerInfo.getPlayer().getInventory();
    }

    public PlayerClass getPlayerClass(){
        return playerInfo.getPlayerClass();
    }

    public void recalculateInventory(){
        playerInfo.recalculateInventory();
    }
}

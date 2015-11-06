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
    private TeamInfo teamInfo;
    private BuildingInfo buildingInfo;

    public ItemPurchaseEvent(BuildingInfo buildingInfo, PlayerInfo playerInfo, TeamInfo teamInfo) {
        this.buildingInfo = buildingInfo;
        this.playerInfo = playerInfo;
        this.teamInfo = teamInfo;
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }

    public BuildingInfo getBuildingInfo() {
        return buildingInfo;
    }

    public TeamInfo getTeamInfo() {
        return teamInfo;
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

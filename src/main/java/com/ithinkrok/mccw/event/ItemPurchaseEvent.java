package com.ithinkrok.mccw.event;

import com.ithinkrok.mccw.data.Building;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

/**
 * Created by paul on 06/11/15.
 *
 * An event to handle a User purchasing an item in an inventory
 */
public class ItemPurchaseEvent extends UserEvent{

    private Team team;
    private Building building;

    public ItemPurchaseEvent(Building building, User user, Team team) {
        super(user);
        this.building = building;
        this.team = team;
    }

    public Building getBuilding() {
        return building;
    }

    public Team getTeam() {
        return team;
    }


    public void recalculateInventory(){
        getUser().redoShopInventory();
    }
}

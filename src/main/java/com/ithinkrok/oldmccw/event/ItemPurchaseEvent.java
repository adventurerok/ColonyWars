package com.ithinkrok.oldmccw.event;

import com.ithinkrok.oldmccw.data.Building;
import com.ithinkrok.oldmccw.data.Team;
import com.ithinkrok.oldmccw.data.User;

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

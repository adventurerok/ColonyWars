package com.ithinkrok.mccw.event;

import com.ithinkrok.mccw.data.Building;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

/**
 * Created by paul on 06/11/15.
 */
public class ItemPurchaseEvent {

    private User user;
    private Team team;
    private Building building;

    public ItemPurchaseEvent(Building building, User user, Team team) {
        this.building = building;
        this.user = user;
        this.team = team;
    }

    public User getUser() {
        return user;
    }

    public Building getBuilding() {
        return building;
    }

    public Team getTeam() {
        return team;
    }

    public Player getPlayer(){
        return user.getPlayer();
    }

    public PlayerInventory getPlayerInventory(){
        return user.getPlayer().getInventory();
    }

    public PlayerClass getPlayerClass(){
        return user.getPlayerClass();
    }

    public void recalculateInventory(){
        user.recalculateInventory();
    }
}

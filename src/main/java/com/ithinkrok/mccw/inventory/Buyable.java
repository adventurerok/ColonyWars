package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.event.ItemPurchaseEvent;
import com.ithinkrok.mccw.util.item.InventoryUtils;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by paul on 06/11/15.
 *
 * Represents something that may be purchased from a shop
 */
public abstract class Buyable {

    private final int minFreeSlots;
    private final int cost;
    private final boolean team;
    private ItemStack display;
    private List<String> buildingNames;

    public Buyable(ItemStack display, String buildingName, int cost, boolean team, int minFreeSlots) {
        this.display = display;
        this.buildingNames = new ArrayList<>();
        buildingNames.add(buildingName);
        this.cost = cost;
        this.team = team;
        this.minFreeSlots = minFreeSlots;

        InventoryUtils.addPrice(this.display, cost, team);
    }

    public Buyable withAdditionalBuildings(String...additionalBuildings){
        Collections.addAll(buildingNames, additionalBuildings);
        return this;
    }

    public int getCost() {
        return cost;
    }

    public boolean buyWithTeamMoney() {
        return team;
    }

    public ItemStack getDisplayItemStack() {
        return display;
    }

    public int getMinFreeSlots() {
        return minFreeSlots;
    }

    public List<String> getBuildingNames() {
        return buildingNames;
    }

    public abstract void onPurchase(ItemPurchaseEvent event);
    public abstract boolean canBuy(ItemPurchaseEvent event);

    public void prePurchase(ItemPurchaseEvent event){

    }
}

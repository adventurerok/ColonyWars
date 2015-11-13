package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.event.ItemPurchaseEvent;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.inventory.ItemStack;

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
    private final String buildingName;

    public Buyable(ItemStack display, String buildingName, int cost, boolean team, int minFreeSlots) {
        this.display = display;
        this.buildingName = buildingName;
        this.cost = cost;
        this.team = team;
        this.minFreeSlots = minFreeSlots;

        InventoryUtils.addPrice(this.display, cost, team);
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

    public String getBuildingName() {
        return buildingName;
    }

    public abstract void onPurchase(ItemPurchaseEvent event);
    public abstract boolean canBuy(ItemPurchaseEvent event);

    public void prePurchase(ItemPurchaseEvent event){

    }
}

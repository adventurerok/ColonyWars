package com.ithinkrok.mccw.inventory;

import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 06/11/15.
 */
public abstract class BuyableItem {

    private int minFreeSlots;
    private int cost;
    private boolean team;
    private ItemStack display;

    public BuyableItem(ItemStack display, int cost, boolean team, int minFreeSlots) {
        this.display = display;
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

    public abstract void onPurchase(ItemPurchaseEvent event);
    public abstract boolean canBuy(ItemPurchaseEvent event);
}

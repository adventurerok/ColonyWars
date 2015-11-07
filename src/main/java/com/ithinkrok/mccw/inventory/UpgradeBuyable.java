package com.ithinkrok.mccw.inventory;

import org.bukkit.inventory.ItemStack;

/**
 * Created by paul on 06/11/15.
 *
 * A buyable upgrade
 */
public class UpgradeBuyable extends Buyable {

    private String upgradeName;
    private int upgradeLevel;

    public UpgradeBuyable(ItemStack display, String buildingName, int cost, String upgradeName, int upgradeLevel) {
        super(display, buildingName, cost, false, 1);

        this.upgradeName = upgradeName;
        this.upgradeLevel = upgradeLevel;
    }

    @Override
    public void onPurchase(ItemPurchaseEvent event) {
        event.getPlayerInfo().setUpgradeLevel(upgradeName, upgradeLevel);

        event.recalculateInventory();
    }

    @Override
    public boolean canBuy(ItemPurchaseEvent event) {
        return event.getPlayerInfo().getUpgradeLevel(upgradeName) + 1 == upgradeLevel;
    }
}

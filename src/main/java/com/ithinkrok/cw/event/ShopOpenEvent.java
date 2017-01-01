package com.ithinkrok.cw.event;

import com.ithinkrok.cw.Building;
import com.ithinkrok.minigames.api.event.user.BaseUserEvent;
import com.ithinkrok.minigames.api.inventory.ClickableInventory;
import com.ithinkrok.minigames.api.user.User;

/**
 * Created by paul on 14/01/16.
 */
public class ShopOpenEvent extends BaseUserEvent {

    private final Building building;
    private final ClickableInventory shop;

    public ShopOpenEvent(User user, Building building, ClickableInventory shop) {
        super(user);
        this.building = building;
        this.shop = shop;
    }

    public Building getBuilding() {
        return building;
    }

    public ClickableInventory getShop() {
        return shop;
    }
}

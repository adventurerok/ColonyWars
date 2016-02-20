package com.ithinkrok.cw.event;

import com.ithinkrok.cw.Building;
import com.ithinkrok.minigames.api.User;
import com.ithinkrok.minigames.base.event.user.UserEvent;
import com.ithinkrok.minigames.base.inventory.ClickableInventory;

/**
 * Created by paul on 14/01/16.
 */
public class ShopOpenEvent extends UserEvent {

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
